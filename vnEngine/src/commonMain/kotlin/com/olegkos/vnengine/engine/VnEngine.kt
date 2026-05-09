package com.olegkos.vnengine.engine

import com.olegkos.vnengine.GameLoading.DiceRoller
import com.olegkos.vnengine.engine.EngineOutput.*
import com.olegkos.vnengine.engine.EngineOutput.EndOfScene
import com.olegkos.vnengine.engine.EngineOutput.JumpScenarioOutput
import com.olegkos.vnengine.engine.EngineOutput.ShowBackground
import com.olegkos.vnengine.engine.EngineOutput.ShowChoices
import com.olegkos.vnengine.engine.EngineOutput.ShowDice
import com.olegkos.vnengine.engine.EngineOutput.ShowImage
import com.olegkos.vnengine.engine.EngineOutput.ShowText
import com.olegkos.vnengine.engine.variables.GameValue
import com.olegkos.vnengine.engine.variables.GameValue.FloatVal
import com.olegkos.vnengine.engine.variables.GameValue.IntVal
import com.olegkos.vnengine.engine.variables.VariableStore
import com.olegkos.vnengine.engine.variables.resolve
import com.olegkos.vnengine.scene.Option
import com.olegkos.vnengine.scene.Scene
import com.olegkos.vnengine.scene.SceneNode

class VnEngine(
  val state: GameState,
  val dice: DiceRoller
) {
  val variables = VariableStore(state.variables)
  private val scenes = mutableMapOf<String, Scene>()

  fun addScene(id: String, scene: Scene) {
    scenes[id] = scene
  }

  fun addScenes(newScenes: Map<String, Scene>) {
    scenes.putAll(newScenes)
  }

  fun currentNode(): SceneNode? {
    val scene = scenes[state.pointer.sceneId]
      ?: error("Scene not found: ${state.pointer.sceneId}")

    return scene.nodes.getOrNull(state.pointer.nodeIndex)
  }

  private tailrec fun resolveNode(): SceneNode? {
    val node = currentNode() ?: return null

    if (node is SceneNode.Jump) {
      jumpToScene(node.targetSceneId)
      return resolveNode()
    }

    return node
  }

  // =========================
  // ЕДИНАЯ ТОЧКА ВХОДА
  // =========================
  fun tick(option: Option? = null): EngineOutput {
    println("STEP: ${state.pointer}")
    println("CURRENT NODE: ${currentNode()}")
    // обработка выбора игрока — оставляем как у тебя было (inline логика)
    if (option != null) {
      val node = currentNode()

      if (node is SceneNode.Choice) {
        option.nextSceneId?.let {
          jumpToScene(it)
        }
      }
    }

    while (true) {

      val node = resolveNode()
        ?: return EndOfScene

      when (node) {

        // =========================
        // AUTO NODES
        // =========================

        is SceneNode.SetVar -> {
          variables.set(node.varName, node.value.resolve())
          advance()
          continue
        }

        is SceneNode.ModifyVar -> {
          val resolved = node.value.resolve()
          variables.modify(node.varName, resolved)
          advance()
          continue
        }

        is SceneNode.Jump -> {
          jumpToScene(node.targetSceneId)
          continue
        }

        is SceneNode.If -> {
          val value = state.variables[node.variable]
          val compareValue = node.equals.resolve()

          val condition = when {
            value is IntVal && compareValue is IntVal ->
              value.value >= compareValue.value

            value is FloatVal && compareValue is FloatVal ->
              value.value.round2() >= compareValue.value.round2()

            value is IntVal && compareValue is FloatVal ->
              value.value.toFloat().round2() >= compareValue.value.round2()

            value is FloatVal && compareValue is IntVal ->
              value.value.round2() >= compareValue.value.toFloat().round2()

            value is GameValue.Bool && compareValue is GameValue.Bool ->
              value.value == compareValue.value

            value is GameValue.StringVal && compareValue is GameValue.StringVal ->
              value.value == compareValue.value

            else -> false
          }

          jumpToScene(
            if (condition) node.successScene else node.failScene
          )
          continue
        }

        is SceneNode.Switch -> {
          val key = when (val value = state.variables[node.variable]) {
            is IntVal -> value.value.toString()
            is FloatVal -> value.value.toInt().toString()
            is GameValue.StringVal -> value.value
            else -> null
          }

          val targetScene = node.cases[key] ?: node.default
          jumpToScene(targetScene)
          continue
        }

        is SceneNode.SwitchRange -> {

          val value = variables.getModifier(node.variable)

          val targetScene = node.ranges.firstOrNull {
            value >= it.min && value <= it.max
          }?.scene ?: node.default

          jumpToScene(targetScene)
          continue
        }

        // =========================
        // UI OUTPUT
        // =========================

        is SceneNode.Text -> {
          val speakerName =
            node.speakerVar?.let { variables.getString(it) }
              ?: node.speaker

          return ShowText(
            speaker = speakerName,
            speakerVar = node.speakerVar,
            text = resolveTextVariables(node.text)
          )
        }

        is SceneNode.Choice -> {
          return ShowChoices(node.options)
        }

        is SceneNode.Background -> {
          return ShowBackground(node.image)
        }

        is SceneNode.Image -> {
          return ShowImage(node.image)
        }

        is SceneNode.ShowCharacter -> {

          val finalImage = when {
            node.flagVar != null -> {
              val value = state.variables[node.flagVar]

              if (value is GameValue.Bool && value.value) {
                node.trueImage ?: node.image
              } else {
                node.falseImage ?: node.image
              }
            }
            else -> node.image
          }

          return ShowCharacter(
            id = node.id,
            image = finalImage ?: "",
            position = node.position,
            scale = node.scale,
          )
        }

        is SceneNode.HideCharacter -> {
          return HideCharacter(node.id)
        }

        is SceneNode.DiceRoll -> {

          val roll = state.diceResult
          val baseMod = variables.getModifier(node.modifierVar).round2()
          val modified = state.diceModifiedResult

          if (roll == null) {
            return ShowDice(
              name = node.name,
              sides = node.sides,
              result = null,
              modifier = baseMod,
              difficulty = node.difficulty,
              phase = DicePhase.ROLL
            )
          }

          if (modified == null) {
            return ShowDice(
              name = node.name,
              sides = node.sides,
              result = roll,
              modifier = baseMod,
              difficulty = node.difficulty,
              phase = DicePhase.RESULT
            )
          }

          val total = modified.round2()
          val finalModifier = (total - roll).round2()

          val resultOutput = ShowDice(
            name = node.name,
            sides = node.sides,
            result = roll,
            modifier = finalModifier,
            difficulty = node.difficulty,
            phase = DicePhase.FINAL
          )

          when {
            roll == 1 && node.critFailScene != null ->
              jumpToScene(node.critFailScene)

            roll == node.sides && node.critSuccessScene != null ->
              jumpToScene(node.critSuccessScene)

            total >= node.difficulty ->
              jumpToScene(node.successScene)

            else ->
              jumpToScene(node.failScene)
          }

          state.diceResult = null
          state.diceModifiedResult = null

          return resultOutput
        }

        is SceneNode.DrawCard -> {
          return DrawCardRequest(
            random = node.random,
            value = node.value,
            image = node.image
          )
        }

        is SceneNode.InitGame -> {
          return if (!state.isGameInitialized) {
            ShowInitGame(
              playerNameVar = node.playerNameVar,
              classVar = node.classVar,
              classes = node.classes
            )
          } else {
            jumpToScene(node.nextSceneId)
            continue
          }
        }

        is SceneNode.HideImage -> {
          return HideImage
        }

        is SceneNode.SceneView -> {
          return ShowSceneView(
            background = node.background,
            navigation = node.navigation,
            hotspots = node.hotspots
          )
        }

        is SceneNode.Effect -> {
          return ShowImage(node.image)
        }
        is SceneNode.JumpScenario -> {

          state.scenarioStack.addLast(state.pointer.copy())
          advance()

          return JumpScenarioOutput(node.scenarioFile)
        }

        is SceneNode.Battle -> {
          return handleBattleNode(node)
        }
      }
    }
  }

  private fun handleBattleNode(node: SceneNode.Battle): EngineOutput {
    val bs = state.battle ?: BattleState(
      battleId = node.id,
      monsterHp = node.monster.health,
      monsterMaxHp = node.monster.health
    ).also { state.battle = it }

    val health = variables.getModifier(node.player.healthVar).toInt()
    val sanity = variables.getModifier(node.player.sanityVar).toInt()

    when (bs.phase) {
      BattlePhase.START -> {
        bs.phase = if (node.phases.horror?.enabled == true) BattlePhase.HORROR else BattlePhase.ACTION
      }

      BattlePhase.HORROR -> {
        val h = node.phases.horror!!
        return buildBattleDiceOutput(node, bs, health, sanity, h.name, h.sides, h.difficulty, h.modifierVar, canEscape = false)
      }

      BattlePhase.ACTION -> {
        return EngineOutput.ShowBattle(
          battleId = node.id,
          title = node.title,
          monsterName = node.monster.name,
          monsterImage = node.monster.image,
          monsterHp = bs.monsterHp,
          monsterMaxHp = bs.monsterMaxHp,
          playerHealth = health,
          playerSanity = sanity,
          phase = BattlePhase.ACTION,
          canEscape = node.escape?.allowed == true
        )
      }

      BattlePhase.COMBAT -> {
        val c = node.phases.combat
        return buildBattleDiceOutput(node, bs, health, sanity, c.name, c.sides, c.difficulty, c.modifierVar, canEscape = false)
      }

      BattlePhase.ESCAPE -> {
        val e = node.escape ?: error("Escape config missing")
        return buildBattleDiceOutput(node, bs, health, sanity, e.name, e.sides, e.difficulty, e.modifierVar, canEscape = true)
      }

      BattlePhase.RESOLVE -> {
        when {
          bs.monsterHp <= 0 -> {
            state.battle = null
            jumpToScene(node.transitions.winScene)
            return tick()
          }
          health <= 0 || sanity <= 0 -> {
            state.battle = null
            jumpToScene(node.transitions.loseScene)
            return tick()
          }
          else -> {
            bs.phase = BattlePhase.ACTION
            return handleBattleNode(node)
          }
        }
      }

      else -> Unit
    }

    return handleBattleNode(node)
  }
  private fun buildBattleDiceOutput(
    node: SceneNode.Battle,
    bs: BattleState,
    health: Int,
    sanity: Int,
    checkName: String,
    sides: Int,
    difficulty: Int,
    modifierVar: String,
    canEscape: Boolean
  ): EngineOutput {
    val roll = state.diceResult
    val baseMod = variables.getModifier(modifierVar).round2()
    val modified = state.diceModifiedResult

    // 1) Ждем бросок
    if (roll == null) {
      return EngineOutput.ShowBattle(
        battleId = node.id,
        title = node.title,
        monsterName = node.monster.name,
        monsterImage = node.monster.image,
        monsterHp = bs.monsterHp,
        monsterMaxHp = bs.monsterMaxHp,
        playerHealth = health,
        playerSanity = sanity,
        phase = bs.phase,
        diceName = checkName,
        sides = sides,
        difficulty = difficulty,
        result = null,
        modifier = baseMod,
        canUseCards = false,
        canEscape = canEscape
      )
    }

    // 2) Бросок есть, ждем модификатор (карты/бафы)
    if (modified == null) {
      return EngineOutput.ShowBattle(
        battleId = node.id,
        title = node.title,
        monsterName = node.monster.name,
        monsterImage = node.monster.image,
        monsterHp = bs.monsterHp,
        monsterMaxHp = bs.monsterMaxHp,
        playerHealth = health,
        playerSanity = sanity,
        phase = bs.phase,
        diceName = checkName,
        sides = sides,
        difficulty = difficulty,
        result = roll,
        modifier = baseMod,
        canUseCards = true,
        canEscape = canEscape
      )
    }

    // 3) Финал проверки
    val total = modified.round2()
    val finalModifier = (total - roll).round2()

    val output = EngineOutput.ShowBattle(
      battleId = node.id,
      title = node.title,
      monsterName = node.monster.name,
      monsterImage = node.monster.image,
      monsterHp = bs.monsterHp,
      monsterMaxHp = bs.monsterMaxHp,
      playerHealth = health,
      playerSanity = sanity,
      phase = bs.phase,
      diceName = checkName,
      sides = sides,
      difficulty = difficulty,
      result = roll,
      modifier = finalModifier,
      canUseCards = false,
      canEscape = canEscape
    )

    when (bs.phase) {
      BattlePhase.HORROR -> {
        val horror = node.phases.horror
        if (horror != null && total < horror.difficulty) {
          val dmg = horror.onFailSanityDamage
          if (dmg > 0) applyDeltaToVar(node.player.sanityVar, -dmg)
        }
        bs.phase = BattlePhase.ACTION
      }

      BattlePhase.COMBAT -> {
        val combat = node.phases.combat

        val monsterDamage = when {
          roll == sides -> combat.damageOnCritSuccess
          total >= combat.difficulty -> combat.damageOnSuccess
          else -> 0
        }

        if (monsterDamage > 0) {
          bs.monsterHp = (bs.monsterHp - monsterDamage).coerceAtLeast(0)
        } else {
          val playerDmg = combat.damageToPlayerOnFail
          if (playerDmg > 0) applyDeltaToVar(node.player.healthVar, -playerDmg)
        }

        bs.phase = BattlePhase.RESOLVE
      }

      BattlePhase.ESCAPE -> {
        val escapeCfg = node.escape ?: error("Escape config missing")
        val escaped = total >= escapeCfg.difficulty

        if (escaped && node.transitions.escapeScene != null) {
          state.battle = null
          jumpToScene(node.transitions.escapeScene)
        } else {
          if (!escaped && escapeCfg.onFailPlayerDamage > 0) {
            applyDeltaToVar(node.player.healthVar, -escapeCfg.onFailPlayerDamage)
          }
          bs.phase = BattlePhase.COMBAT
        }
      }

      else -> Unit
    }

    // Сброс кубика после резолва проверки
    state.diceResult = null
    state.diceModifiedResult = null

    return output
  }

  private fun applyDeltaToVar(varName: String, delta: Int) {
    val current = state.variables[varName]
    when (current) {
      is GameValue.IntVal -> state.variables[varName] = GameValue.IntVal(current.value + delta)
      is GameValue.FloatVal -> state.variables[varName] = GameValue.FloatVal(current.value + delta)
      null -> state.variables[varName] = GameValue.IntVal(delta)
      else -> Unit
    }
  }

  fun battleChooseFight() {
    state.battle?.phase = BattlePhase.COMBAT
  }
  fun battleChooseEscape() {
    state.battle?.phase = BattlePhase.ESCAPE
  }
  // =========================
  // BACKWARD COMPAT (НЕ ЛОМАЕМ APP)
  // =========================
  fun step(option: Option? = null): EngineOutput = tick(option)

  fun advanceExternal(option: Option?) {
    val node = currentNode() ?: return

    when (node) {
      is SceneNode.Choice -> {
        option?.nextSceneId?.let {
          jumpToScene(it)
          return
        }
      }
      else -> advance()
    }
  }

  fun jumpToScene(sceneId: String) {
    require(scenes.containsKey(sceneId)) {
      "Scene '$sceneId' not found"
    }

    state.pointer = NodePointer(sceneId, 0)
    state.diceResult = null
  }

  private fun advance() {
    state.pointer = state.pointer.copy(
      nodeIndex = state.pointer.nodeIndex + 1
    )
  }

  private fun resolveTextVariables(rawText: String): String {
    val regex = "\\{([a-zA-Z0-9_]+)\\}".toRegex()
    return regex.replace(rawText) {
      variables.getString(it.groupValues[1])
    }
  }
}
fun Float.round2(): Float = (this * 100).toInt() / 100f

data class UiCard(
  val id: String,
  val image: String,
  val value: Int
)