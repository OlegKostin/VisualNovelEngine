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
      }
    }
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