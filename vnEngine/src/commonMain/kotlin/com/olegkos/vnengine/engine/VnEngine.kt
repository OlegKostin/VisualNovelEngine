package com.olegkos.vnengine.engine

import com.olegkos.vnengine.GameLoading.DiceRoller
import com.olegkos.vnengine.GameLoading.NodePointer
import com.olegkos.vnengine.engine.EngineOutput.ShowChoices
import com.olegkos.vnengine.engine.EngineOutput.ShowDice
import com.olegkos.vnengine.engine.EngineOutput.ShowText
import com.olegkos.vnengine.engine.variables.GameValue
import com.olegkos.vnengine.engine.variables.GameValue.*
import com.olegkos.vnengine.engine.variables.VariableStore
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

  fun currentNode(): SceneNode {

    val scene = scenes[state.pointer.sceneId]
      ?: error("Scene not found")

    return scene.nodes
      .getOrNull(state.pointer.nodeIndex)
      ?: error("Node not found")
  }

  private tailrec fun resolveNode(): SceneNode {

    val node = currentNode()

    if (node is SceneNode.Jump) {
      jumpToScene(node.targetSceneId)
      return resolveNode()
    }

    return node
  }

  fun step(selectedOption: Option? = null): EngineOutput {

    while (true) {

      val node = resolveNode()

      when (node) {

        is SceneNode.SetVar -> {
          variables.set(node.varName, node.value)
          advance()
        }

        is SceneNode.ModifyVar -> {
          variables.modify(node.varName, node.value)
          advance()
        }

        is SceneNode.If -> {
          val value = state.variables[node.variable]

          if (value == node.equals)
            jumpToScene(node.successScene)
          else
            jumpToScene(node.failScene)
        }

        is SceneNode.Text -> {
          advance()
          return ShowText(node.text)
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
            // Кубик ещё не бросан
            return EngineOutput.ShowDice(
              name = node.name,
              sides = node.sides,
              result = null, // сигнал UI показать кнопку броска
              modifier = variables.getInt(node.modifierVar),
              difficulty = node.difficulty
            )
          }

          // Кубик уже бросан — продолжаем сцену
          val roll = state.diceResult!!
          val mod = variables.getInt(node.modifierVar)
          val total = roll + mod

          val resultOutput = EngineOutput.ShowDice(
            name = node.name,
            sides = node.sides,
            result = roll,
            modifier = mod,
            difficulty = node.difficulty
          )

          // Сразу переходим по сценам
          when {
            roll == 1 && node.critFailScene != null -> jumpToScene(node.critFailScene)
            roll == node.sides && node.critSuccessScene != null -> jumpToScene(node.critSuccessScene)
            total >= node.difficulty -> jumpToScene(node.successScene)
            else -> jumpToScene(node.failScene)
          }

          state.diceResult = null
          return resultOutput
        }        is SceneNode.Jump -> {
          jumpToScene(node.targetSceneId)
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
    state.pointer = state.pointer.copy(
      nodeIndex = state.pointer.nodeIndex + 1
    )
  }

}