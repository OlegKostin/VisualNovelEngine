package com.olegkos.vnengine.engine

import com.olegkos.vnengine.GameLoading.DiceRoller
import com.olegkos.vnengine.engine.cards.CardManager
import com.olegkos.vnengine.engine.EngineOutput.*
import com.olegkos.vnengine.engine.EngineOutput.EndOfScene
import com.olegkos.vnengine.engine.EngineOutput.JumpScenarioOutput
import com.olegkos.vnengine.engine.EngineOutput.ShowBackground
import com.olegkos.vnengine.engine.EngineOutput.ShowChoices
import com.olegkos.vnengine.engine.EngineOutput.ShowDice
import com.olegkos.vnengine.engine.EngineOutput.ShowDiceDuel
import com.olegkos.vnengine.engine.EngineOutput.ShowImage
import com.olegkos.vnengine.engine.EngineOutput.ShowPanImage
import com.olegkos.vnengine.engine.EngineOutput.ShowText
import com.olegkos.vnengine.engine.EngineOutput.ShowTimeSkip
import com.olegkos.vnengine.engine.EngineOutput.ShowVar
import com.olegkos.vnengine.engine.variables.GameValue
import com.olegkos.vnengine.engine.variables.GameValue.FloatVal
import com.olegkos.vnengine.engine.variables.GameValue.IntVal
import com.olegkos.vnengine.engine.variables.VariableStore
import com.olegkos.vnengine.engine.variables.formatModifyVarDelta
import com.olegkos.vnengine.engine.variables.resolve
import com.olegkos.vnengine.engine.variables.shouldShowModifyVarUi
import com.olegkos.vnengine.scene.Option
import com.olegkos.vnengine.scene.Scene
import com.olegkos.vnengine.scene.SceneNode

class VnEngine(
  val state: GameState,
  val dice: DiceRoller,
  internal val cards: CardManager? = null
) {
  companion object {
    /** Включайте только при отладке сценария — синхронный вывод на каждый step сильно тормозит UI. */
    private const val TRACE_STEPS = false
  }
  val variables = VariableStore(state.variables) { state.statCapsInt }
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

  fun tick(option: Option? = null): EngineOutput {
    if (TRACE_STEPS) {
      println("STEP: ${state.pointer}")
      println("CURRENT NODE: ${currentNode()}")
    }
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

        is SceneNode.SetVar -> {
          variables.set(node.varName, node.value.resolve())
          advance()
          continue
        }

        is SceneNode.ModifyVar -> {
          val resolved = node.value.resolve()
          variables.modify(node.varName, resolved)
          if (shouldShowModifyVarUi(node.varName, node.text)) {
            return ShowVar(
              name = node.varName,
              value = resolved.formatModifyVarDelta(),
              text = node.text!!.trim(),
            )
          }
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

        is SceneNode.IfAll -> {
          val ok = node.requires.isNotEmpty() && node.requires.all { checkRequirement(it) }
          jumpToScene(if (ok) node.successScene else node.failScene)
          continue
        }

        is SceneNode.IfAny -> {
          val ok = node.requires.any { checkRequirement(it) }
          jumpToScene(if (ok) node.successScene else node.failScene)
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

        is SceneNode.WeightedRandomJump -> {
          val target = pickWeightedRandomJump(node)
          jumpToScene(target)
          continue
        }

        is SceneNode.Text -> {
          val speakerName =
            node.speakerVar?.let { variables.getString(it) }
              ?: node.speaker

          return ShowText(
            speaker = speakerName,
            speakerVar = node.speakerVar,
            text = resolveTextVariables(node.text),
            long = node.long,
            light = node.light,
          )
        }

        is SceneNode.TimeSkip -> {
          return ShowTimeSkip(
            durationMs = node.durationMs.coerceIn(300L, 10_000L),
            text = node.text?.trim()?.takeIf { it.isNotEmpty() }?.let(::resolveTextVariables),
          )
        }

        is SceneNode.Choice -> {
          return ShowChoices(node.options)
        }

        is SceneNode.Background -> {
          state.currentBackground = node.image
          return ShowBackground(node.image)
        }

        is SceneNode.Image -> {
          return ShowImage(node.image)
        }

        is SceneNode.SpriteAnimation -> {
          val speakerName =
            node.speakerVar?.let { variables.getString(it) }
              ?: node.speaker
          return ShowSpriteAnimation(
            layers = node.layers.map { layer ->
              EngineOutput.SpriteAnimationLayerOutput(
                image = layer.image,
                columns = layer.columns,
                rows = layer.rows,
                frameDurationMs = layer.frameDurationMs,
                loop = layer.loop,
                scale = layer.scale,
              )
            },
            text = node.text?.let { resolveTextVariables(it) },
            speaker = speakerName,
          )
        }

        is SceneNode.PanImage -> {
          val speakerName =
            node.speakerVar?.let { variables.getString(it) }
              ?: node.speaker
          return ShowPanImage(
            image = node.image,
            direction = node.direction,
            durationMs = node.durationMs.coerceAtLeast(0L),
            endAtCenter = node.endAtCenter,
            clicksToAdvance = node.clicksToAdvance,
            text = node.text?.let { resolveTextVariables(it) },
            speaker = speakerName,
          )
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

          val imagePath = finalImage ?: ""
          state.visibleCharacters =
            state.visibleCharacters.filter { it.id != node.id } +
              VisibleCharacter(
                id = node.id,
                image = imagePath,
                position = node.position,
                scale = node.scale
              )
          advance()
          continue
        }

        is SceneNode.HideCharacter -> {
          state.visibleCharacters = state.visibleCharacters.filter { it.id != node.id }
          advance()
          continue
        }

        is SceneNode.DiceRoll -> {

          val pending = state.pendingDiceJumpScene
          if (pending != null) {
            state.pendingDiceJumpScene = null
            state.diceResult = null
            state.diceModifiedResult = null
            jumpToScene(pending)
            continue
          }

          val roll = state.diceResult
          val baseMod = variables.getCheckModifier(node.modifierVar).round2()
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

          state.pendingDiceJumpScene = when {
            roll == 1 && node.critFailScene != null ->
              node.critFailScene

            roll == node.sides && node.critSuccessScene != null ->
              node.critSuccessScene

            total >= node.difficulty ->
              node.successScene

            else ->
              node.failScene
          }

          return resultOutput
        }

        is SceneNode.DrawCard -> {
          return DrawCardRequest(
            random = node.random,
            value = node.value,
            image = node.image,
            addToMeta = node.addToMeta,
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
          clearSceneLayers()
          state.scenarioStack.addLast(state.pointer.copy())
          advance()
          return JumpScenarioOutput(node.scenarioFile)
        }

        is SceneNode.Battle -> {
          return handleBattleNode(node)
        }

        is SceneNode.DiceDuel -> {
          return handleDiceDuelNode(node)
        }

        is SceneNode.CardGame -> {
          return handleCardGameNode(node)
        }

        is SceneNode.TargetTap -> {
          return handleTargetTapNode(node)
        }

        is SceneNode.AcademyHub -> {
          return handleAcademyHubNode(node)
        }
      }
    }
  }

  private fun battlePlayerDisplayName(player: SceneNode.PlayerRefs): String? {
    val varName = player.playerNameVar?.trim()?.takeIf { it.isNotEmpty() } ?: "my_name"
    return variables.getString(varName).trim().takeIf { it.isNotEmpty() }
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
          playerName = battlePlayerDisplayName(node.player),
          monsterName = node.monster.name,
          monsterImage = node.monster.image,
          monsterHp = bs.monsterHp,
          monsterMaxHp = bs.monsterMaxHp,
          monsterCombatDamage = node.monster.combatDamage,
          monsterHorrorDamage = node.monster.horrorDamage,
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

      BattlePhase.POST_COMBAT_VN -> {
        val line = bs.postCombatVnLines.getOrNull(bs.postCombatVnIndex)
        if (line == null) {
          bs.postCombatVnLines = emptyList()
          bs.postCombatVnIndex = 0
          bs.phase = BattlePhase.RESOLVE
          return handleBattleNode(node)
        }
        val healthNow = variables.getModifier(node.player.healthVar).toInt()
        val sanityNow = variables.getModifier(node.player.sanityVar).toInt()
        val speakerResolved = line.speaker?.trim()?.takeIf { it.isNotEmpty() }?.let(::resolveTextVariables)
        return EngineOutput.ShowBattle(
          battleId = node.id,
          title = node.title,
          playerName = battlePlayerDisplayName(node.player),
          monsterName = node.monster.name,
          monsterImage = node.monster.image,
          monsterHp = bs.monsterHp,
          monsterMaxHp = bs.monsterMaxHp,
          monsterCombatDamage = node.monster.combatDamage,
          monsterHorrorDamage = node.monster.horrorDamage,
          playerHealth = healthNow,
          playerSanity = sanityNow,
          phase = BattlePhase.POST_COMBAT_VN,
          diceName = node.phases.combat.name,
          sides = bs.combatSummarySides,
          difficulty = bs.combatSummaryDifficulty,
          result = bs.combatSummaryRoll,
          modifier = bs.combatSummaryModifier ?: 0f,
          canUseCards = false,
          canEscape = false,
          postCombatVnSpeaker = speakerResolved,
          postCombatVnText = resolveTextVariables(line.text)
        )
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
            bs.combatSummaryRoll = null
            bs.combatSummarySides = null
            bs.combatSummaryDifficulty = null
            bs.combatSummaryModifier = null
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
    val baseMod = variables.getCheckModifier(modifierVar).round2()
    val modified = state.diceModifiedResult

    if (roll == null) {
      return EngineOutput.ShowBattle(
        battleId = node.id,
        title = node.title,
        playerName = battlePlayerDisplayName(node.player),
        monsterName = node.monster.name,
        monsterImage = node.monster.image,
        monsterHp = bs.monsterHp,
        monsterMaxHp = bs.monsterMaxHp,
        monsterCombatDamage = node.monster.combatDamage,
        monsterHorrorDamage = node.monster.horrorDamage,
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

    if (modified == null) {
      return EngineOutput.ShowBattle(
        battleId = node.id,
        title = node.title,
        playerName = battlePlayerDisplayName(node.player),
        monsterName = node.monster.name,
        monsterImage = node.monster.image,
        monsterHp = bs.monsterHp,
        monsterMaxHp = bs.monsterMaxHp,
        monsterCombatDamage = node.monster.combatDamage,
        monsterHorrorDamage = node.monster.horrorDamage,
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

    val total = modified.round2()
    val finalModifier = (total - roll).round2()

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

        val queuedLines = when {
          monsterDamage > 0 -> {
            bs.monsterHp = (bs.monsterHp - monsterDamage).coerceAtLeast(0)
            combat.vnAfterMonsterHit[bs.monsterHp] ?: emptyList()
          }
          else -> {
            val playerDmg = combat.damageToPlayerOnFail
            if (playerDmg > 0) {
              applyDeltaToVar(node.player.healthVar, -playerDmg)
            }
            if (playerDmg > 0) {
              val newPlayerHp = variables.getModifier(node.player.healthVar).toInt()
              combat.vnAfterPlayerHit[newPlayerHp] ?: emptyList()
            } else {
              emptyList()
            }
          }
        }

        if (queuedLines.isNotEmpty()) {
          bs.postCombatVnLines = queuedLines
          bs.postCombatVnIndex = 0
          bs.phase = BattlePhase.POST_COMBAT_VN
          bs.combatSummaryRoll = roll
          bs.combatSummarySides = sides
          bs.combatSummaryDifficulty = difficulty
          bs.combatSummaryModifier = finalModifier
        } else {
          bs.phase = BattlePhase.RESOLVE
          bs.combatSummaryRoll = null
          bs.combatSummarySides = null
          bs.combatSummaryDifficulty = null
          bs.combatSummaryModifier = null
        }
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

    state.diceResult = null
    state.diceModifiedResult = null

    val healthNow = variables.getModifier(node.player.healthVar).toInt()
    val sanityNow = variables.getModifier(node.player.sanityVar).toInt()

    val vnLine = if (bs.phase == BattlePhase.POST_COMBAT_VN) {
      bs.postCombatVnLines.getOrNull(bs.postCombatVnIndex)
    } else {
      null
    }

    return EngineOutput.ShowBattle(
      battleId = node.id,
      title = node.title,
      playerName = battlePlayerDisplayName(node.player),
      monsterName = node.monster.name,
      monsterImage = node.monster.image,
      monsterHp = bs.monsterHp,
      monsterMaxHp = bs.monsterMaxHp,
      monsterCombatDamage = node.monster.combatDamage,
      monsterHorrorDamage = node.monster.horrorDamage,
      playerHealth = healthNow,
      playerSanity = sanityNow,
      phase = bs.phase,
      diceName = checkName,
      sides = sides,
      difficulty = difficulty,
      result = roll,
      modifier = finalModifier,
      canUseCards = false,
      canEscape = canEscape,
      postCombatVnSpeaker = vnLine?.speaker?.trim()?.takeIf { it.isNotEmpty() }?.let(::resolveTextVariables),
      postCombatVnText = vnLine?.let { resolveTextVariables(it.text) }
    )
  }

  private fun applyDeltaToVar(varName: String, delta: Int) {
    val current = state.variables[varName]
    when (current) {
      is GameValue.IntVal -> state.variables[varName] = GameValue.IntVal(current.value + delta)
      is GameValue.FloatVal -> state.variables[varName] = GameValue.FloatVal(current.value + delta)
      null -> state.variables[varName] = GameValue.IntVal(delta)
      else -> Unit
    }
    variables.reapplyCap(varName)
  }

  fun battleChooseFight() {
    state.battle?.phase = BattlePhase.COMBAT
  }

  fun battleChooseEscape() {
    state.battle?.phase = BattlePhase.ESCAPE
  }

  fun battlePostCombatVnNext() {
    val bs = state.battle ?: return
    if (bs.phase != BattlePhase.POST_COMBAT_VN) return
    bs.postCombatVnIndex++
    if (bs.postCombatVnIndex >= bs.postCombatVnLines.size) {
      bs.postCombatVnLines = emptyList()
      bs.postCombatVnIndex = 0
      bs.phase = BattlePhase.RESOLVE
    }
  }

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
      is SceneNode.InitGame,
      is SceneNode.DiceRoll,
      is SceneNode.Battle,
      is SceneNode.DiceDuel,
      is SceneNode.CardGame,
      is SceneNode.TargetTap,
      is SceneNode.AcademyHub -> {
        return
      }
      else -> Unit
    }

    advance()
  }

  fun jumpToScene(sceneId: String) {
    require(scenes.containsKey(sceneId)) {
      "Scene '$sceneId' not found"
    }

    state.pointer = NodePointer(sceneId, 0)
    state.diceResult = null
    state.diceModifiedResult = null
    state.diceDuel = null
    state.cardGame = null
    state.targetTap = null
    state.pendingDiceJumpScene = null
    // academy сохраняем между jumpScenario
  }

  internal fun advance() {
    state.pointer = state.pointer.copy(
      nodeIndex = state.pointer.nodeIndex + 1
    )
  }

  private fun handleDiceDuelNode(node: SceneNode.DiceDuel): EngineOutput {
    val ds = state.diceDuel ?: DiceDuelState(
      duelId = node.id,
      phase = DiceDuelPhase.START
    ).also { state.diceDuel = it }

    val titleResolved = resolveTextVariables(node.title)
    val opponentNameResolved = resolveTextVariables(node.opponent.name)
    val opponentImageResolved = resolveTextVariables(node.opponent.image)

    val playerName = variables.getString("my_name")
    val playerBaseModifier = variables.getCheckModifier(node.playerModifierVar).round2()
    val opponentBaseModifier = resolveOpponentModifier(node).round2()

    when (ds.phase) {
      DiceDuelPhase.START -> {
        ds.phase = DiceDuelPhase.PLAYER_ROLL
        return handleDiceDuelNode(node)
      }

      DiceDuelPhase.PLAYER_ROLL -> {
        return ShowDiceDuel(
          duelId = node.id,
          title = titleResolved,
          sides = node.sides,
          playerName = playerName,
          playerModifier = playerBaseModifier,
          playerRoll = null,
          playerTotal = null,
          opponentName = opponentNameResolved,
          opponentImage = opponentImageResolved,
          opponentModifier = opponentBaseModifier,
          opponentRoll = null,
          opponentTotal = null,
          phase = DiceDuelPhase.PLAYER_ROLL,
          canUseCards = false
        )
      }

      DiceDuelPhase.PLAYER_MODIFY -> {
        return ShowDiceDuel(
          duelId = node.id,
          title = titleResolved,
          sides = node.sides,
          playerName = playerName,
          playerModifier = playerBaseModifier,
          playerRoll = ds.playerRoll,
          playerTotal = ds.playerModified,
          opponentName = opponentNameResolved,
          opponentImage = opponentImageResolved,
          opponentModifier = opponentBaseModifier,
          opponentRoll = ds.opponentRoll,
          opponentTotal = ds.opponentModified,
          phase = DiceDuelPhase.PLAYER_MODIFY,
          canUseCards = node.cards.allowCards
        )
      }

      DiceDuelPhase.OPPONENT_ROLL -> {
        ds.phase = DiceDuelPhase.RESOLVE
        return handleDiceDuelNode(node)
      }

      DiceDuelPhase.RESOLVE -> {
        if (ds.winner == null) {
          val playerTotal = ds.playerModified ?: 0f
          val opponentTotal = ds.opponentModified ?: 0f

          ds.winner = when {
            playerTotal > opponentTotal -> DiceDuelWinner.PLAYER
            playerTotal < opponentTotal -> DiceDuelWinner.OPPONENT
            else -> DiceDuelWinner.DRAW
          }
        }

        val resultText = when (ds.winner) {
          DiceDuelWinner.PLAYER -> "Победа"
          DiceDuelWinner.OPPONENT -> "Поражение"
          DiceDuelWinner.DRAW -> "Ничья"
          null -> null
        }

        return ShowDiceDuel(
          duelId = node.id,
          title = titleResolved,
          sides = node.sides,
          playerName = playerName,
          playerModifier = playerBaseModifier,
          playerRoll = ds.playerRoll,
          playerTotal = ds.playerModified,
          opponentName = opponentNameResolved,
          opponentImage = opponentImageResolved,
          opponentModifier = opponentBaseModifier,
          opponentRoll = ds.opponentRoll,
          opponentTotal = ds.opponentModified,
          phase = DiceDuelPhase.RESOLVE,
          canUseCards = false,
          resultText = resultText
        )
      }
    }
  }

  private fun resolveOpponentModifier(node: SceneNode.DiceDuel): Float {
    val varMod = node.opponent.modifierVar?.let { variables.getModifier(it) } ?: 0f
    return (node.opponent.modifier + varMod).round2()
  }

  fun diceDuelRollBoth() {
    val node = currentNode() as? SceneNode.DiceDuel ?: return
    val ds = state.diceDuel ?: return
    if (ds.phase != DiceDuelPhase.PLAYER_ROLL) return

    ds.playerRoll = dice.roll(node.sides)
    ds.opponentRoll = dice.roll(node.sides)
    val oppMod = resolveOpponentModifier(node)
    ds.opponentModified = ((ds.opponentRoll ?: 0) + oppMod).round2()
    ds.playerModified = null
    ds.phase = DiceDuelPhase.PLAYER_MODIFY
  }

  fun diceDuelApplyModifier(extra: Float) {
    val node = currentNode() as? SceneNode.DiceDuel ?: return
    val ds = state.diceDuel ?: return
    val roll = ds.playerRoll ?: return
    if (ds.phase != DiceDuelPhase.PLAYER_MODIFY) return

    val base = variables.getCheckModifier(node.playerModifierVar)
    ds.playerModified = (roll + base + extra).round2()
    ds.phase = DiceDuelPhase.RESOLVE
  }

  fun diceDuelContinue() {
    val node = currentNode() as? SceneNode.DiceDuel ?: return
    val ds = state.diceDuel ?: return
    if (ds.phase != DiceDuelPhase.RESOLVE) return

    val target = when (ds.winner) {
      DiceDuelWinner.PLAYER -> node.transitions.winScene
      DiceDuelWinner.OPPONENT -> node.transitions.loseScene
      DiceDuelWinner.DRAW -> node.transitions.drawScene ?: node.transitions.loseScene
      null -> node.transitions.loseScene
    }

    state.diceDuel = null
    jumpToScene(target)
  }

  private fun pickWeightedRandomJump(node: SceneNode.WeightedRandomJump): String {
    val eligible = node.entries
      .filter { entry -> entry.requires.all { req -> checkRequirement(req) } }
      .map { it.scene to it.weight.coerceAtLeast(0) }
      .filter { it.second > 0 }

    if (eligible.isEmpty()) return node.defaultScene

    val defaultChance = node.defaultChance.coerceIn(0f, 1f)
    if (defaultChance > 0f && kotlin.random.Random.nextFloat() < defaultChance) {
      return node.defaultScene
    }

    return pickWeightedScene(eligible)
  }

  private fun pickWeightedScene(pool: List<Pair<String, Int>>): String {
    val total = pool.sumOf { it.second }
    if (total <= 0) return pool.last().first

    val roll = (1..total).random()
    var acc = 0
    for ((scene, weight) in pool) {
      acc += weight
      if (roll <= acc) return scene
    }
    return pool.last().first
  }

  private fun checkRequirement(req: SceneNode.WeightedRandomJump.Requirement): Boolean {
    val current = state.variables[req.variable] ?: return false
    val expected = req.value

    fun cmpNumbers(a: Float, b: Float): Boolean = when (req.op) {
      SceneNode.WeightedRandomJump.Op.EQ -> a == b
      SceneNode.WeightedRandomJump.Op.NEQ -> a != b
      SceneNode.WeightedRandomJump.Op.GTE -> a >= b
      SceneNode.WeightedRandomJump.Op.LTE -> a <= b
      SceneNode.WeightedRandomJump.Op.GT -> a > b
      SceneNode.WeightedRandomJump.Op.LT -> a < b
    }

    return when {
      current is GameValue.IntVal && expected is GameValue.IntVal ->
        cmpNumbers(current.value.toFloat(), expected.value.toFloat())

      current is GameValue.FloatVal && expected is GameValue.FloatVal ->
        cmpNumbers(current.value.round2(), expected.value.round2())

      current is GameValue.IntVal && expected is GameValue.FloatVal ->
        cmpNumbers(current.value.toFloat().round2(), expected.value.round2())

      current is GameValue.FloatVal && expected is GameValue.IntVal ->
        cmpNumbers(current.value.round2(), expected.value.toFloat().round2())

      current is GameValue.Bool && expected is GameValue.Bool -> when (req.op) {
        SceneNode.WeightedRandomJump.Op.EQ -> current.value == expected.value
        SceneNode.WeightedRandomJump.Op.NEQ -> current.value != expected.value
        else -> false
      }

      current is GameValue.StringVal && expected is GameValue.StringVal -> when (req.op) {
        SceneNode.WeightedRandomJump.Op.EQ -> current.value == expected.value
        SceneNode.WeightedRandomJump.Op.NEQ -> current.value != expected.value
        else -> false
      }

      else -> false
    }
  }

  internal fun resolveTextVariables(rawText: String): String {
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