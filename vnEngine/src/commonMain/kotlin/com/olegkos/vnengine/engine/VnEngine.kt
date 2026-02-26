package com.olegkos.vnengine.engine

import com.olegkos.vnengine.scene.Scene
import com.olegkos.vnengine.scene.SceneNode
import com.olegkos.vnengine.scene.Option

class VnEngine(
  val state: GameState
) {

  private val scenes = mutableMapOf<String, Scene>()

  fun addScene(id: String, scene: Scene) {
    scenes[id] = scene
  }

  fun addScenes(newScenes: Map<String, Scene>) {
    scenes.putAll(newScenes)
  }

  fun currentNode(): SceneNode {
    val scene = scenes[state.currentSceneId]
      ?: error("Сцена '${state.currentSceneId}' не найдена")

    return scene.nodes.getOrNull(state.nodeIndex)
      ?: error("Узел ${state.nodeIndex} не найден")
  }

  fun next(selectedOption: Option? = null) {
    val node = currentNode()
    when (node) {
      is SceneNode.Text -> state.nodeIndex++
      is SceneNode.Choice -> selectedOption?.let {
        jumpToScene(it.nextSceneId)
      }
      is SceneNode.Jump -> jumpToScene(node.targetSceneId)
      is SceneNode.DiceRoll -> {
        if (node.result == null) {
          val value = Dice(state).roll(node.name, node.sides)
          node.result = value
        } else {
          state.nodeIndex++
        }
      }
    }
  }

  fun jumpToScene(sceneId: String) {
    if (!scenes.containsKey(sceneId)) {
      error("Сцена '$sceneId' не найдена")
    }
    state.currentSceneId = sceneId
    state.nodeIndex = 0
  }

  fun currentOutput(): EngineOutput =
    when (val node = currentNode()) {
      is SceneNode.Text -> EngineOutput.ShowText(node.text)
      is SceneNode.Choice -> EngineOutput.ShowChoices(node.options)
      is SceneNode.DiceRoll -> {
        EngineOutput.ShowDice(
          name = node.name,
          sides = node.sides,
          result = node.result
        )
      }
      is SceneNode.Jump -> EngineOutput.ShowText("")
    }
}