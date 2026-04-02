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

    println("CURRENT NODE → scene=${state.pointer.sceneId}, index=${state.pointer.nodeIndex}, size=${scene.nodes.size}")

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
  fun step(selectedOption: Option? = null): EngineOutput {

    while (true) {

      val scene = scenes[state.pointer.sceneId]
        ?: error("Scene not found")

      if (state.pointer.nodeIndex >= scene.nodes.size) {
        return EndOfScene
      }

      val node = resolveNode() ?: return EndOfScene

      when (node) {

        is SceneNode.SetVar -> {
          variables.set(node.varName, node.value.resolve())
          advance()
        }

        is SceneNode.ModifyVar -> {
          val resolved = node.value.resolve()
          variables.modify(node.varName, resolved)
          advance()

          val valueString = when (resolved) {
            is IntVal -> resolved.value.toString()
            is FloatVal -> resolved.value.round2().toString()
            is GameValue.Bool -> resolved.value.toString()
            is GameValue.StringVal -> resolved.value
            else -> "0"
          }

          return ShowVar(
            node.varName,
            valueString,
            node.text
          )
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
            else -> false
          }

          if (condition)
            jumpToScene(node.successScene)
          else
            jumpToScene(node.failScene)
        }        is SceneNode.Text -> {
          advance()
        val speakerName = node.speakerVar?.let { variables.getString(it) } ?: node.speaker
        val resolvedText = resolveTextVariables(node.text)
          return ShowText(
            speaker = speakerName,
            speakerVar = node.speakerVar,
            text = resolvedText)
        }

        is SceneNode.Choice -> {
          if (selectedOption != null) {
            jumpToScene(selectedOption.nextSceneId)
            continue
          }
          return ShowChoices(node.options)
        }

        is SceneNode.DiceRoll -> {
          if (state.diceResult == null) {
            return ShowDice(
              name = node.name,
              sides = node.sides,
              result = null,
              modifier = variables.getModifier(node.modifierVar),
              difficulty = node.difficulty
            )
          }

          // Кубик уже бросан — продолжаем сцену
          val roll = state.diceResult!!
          val mod = variables.getModifier(node.modifierVar).round2()
          val total = (roll.toFloat() + mod).round2()

          val resultOutput = ShowDice(
            name = node.name,
            sides = node.sides,
            result = roll,
            modifier = mod,
            difficulty = node.difficulty
          )

          when {
            roll == 1 && node.critFailScene != null -> jumpToScene(node.critFailScene)
            roll == node.sides && node.critSuccessScene != null -> jumpToScene(node.critSuccessScene)
            total >= node.difficulty -> jumpToScene(node.successScene)
            else -> jumpToScene(node.failScene)
          }

          state.diceResult = null
          return resultOutput
        }

        is SceneNode.JumpScenario -> {
          state.scenarioStack.addLast(state.pointer.copy())
          advance()
          return JumpScenarioOutput(node.scenarioFile)
        }

        is SceneNode.Jump -> {
          jumpToScene(node.targetSceneId)
        }
        is SceneNode.Background -> {
          advance()
          return ShowBackground(node.image)
        }

        is SceneNode.Image -> {
          advance()
          return ShowImage(node.image)
        }


        is SceneNode.Effect -> {
          advance()
          return ShowImage(node.image)
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
        }

        is SceneNode.SwitchRange -> {

          val value = variables.getModifier(node.variable)

          val found = node.ranges.firstOrNull {
            value >= it.min && value <= it.max
          }

          val targetScene = found?.scene ?: node.default
          jumpToScene(targetScene)
        }
        is SceneNode.ShowCharacter -> {
          advance()
          return ShowCharacter(
            id = node.id,
            image = node.image,
            position = node.position,
            scale = node.scale,
          )
        }

        is SceneNode.HideCharacter -> {
          advance()
          return HideCharacter(node.id)
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
          advance()
          return HideImage
        }
      }
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
    println("ADVANCE from ${state.pointer}")
    state.pointer = state.pointer.copy(
      nodeIndex = state.pointer.nodeIndex + 1
    )
  }

  private fun resolveTextVariables(rawText: String): String {
    val regex = "\\{([a-zA-Z0-9_]+)\\}".toRegex()
    return regex.replace(rawText) { matchResult ->
      val varName = matchResult.groupValues[1]
      variables.getString(varName)
    }
  }

}
fun Float.round2(): Float = (this * 100).toInt() / 100f