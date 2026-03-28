package com.olegkos.vnengine.engine

import com.olegkos.vnengine.GameLoading.DiceRoller
import com.olegkos.vnengine.engine.EngineOutput.EndOfScene
import com.olegkos.vnengine.engine.EngineOutput.JumpScenarioOutput
import com.olegkos.vnengine.engine.EngineOutput.ShowBackground
import com.olegkos.vnengine.engine.EngineOutput.ShowChoices
import com.olegkos.vnengine.engine.EngineOutput.ShowDice
import com.olegkos.vnengine.engine.EngineOutput.ShowImage
import com.olegkos.vnengine.engine.EngineOutput.ShowText
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
          variables.modify(node.varName, node.value.resolve())
          advance()
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
          return ShowText(
            speaker = node.speaker,
            text = node.text)
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

        is SceneNode.Character -> {
          advance()
          return ShowImage(node.image)
        }
        is SceneNode.Effect -> {
          advance()
          return ShowImage(node.image)
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

}
fun Float.round2(): Float = (this * 100).toInt() / 100f