package com.olegkos.vnengine.engine

import com.olegkos.vnengine.scene.Scene
import com.olegkos.vnengine.scene.SceneNode
import com.olegkos.vnengine.scene.Option

class VnEngine(
  private val scenes: Map<String, Scene>,
  val state: GameState
) {

  fun currentNode(): SceneNode {
    val scene = scenes[state.currentSceneId]
      ?: error("Сцена '${state.currentSceneId}' не найдена")
    return scene.nodes.getOrNull(state.nodeIndex)
      ?: error("Узел с индексом ${state.nodeIndex} в сцене '${state.currentSceneId}' не найден")
  }

  fun next(selectedOption: Option? = null) {
    val node = currentNode()
    when (node) {
      is SceneNode.Text -> state.nodeIndex++
      is SceneNode.Choice -> {
        selectedOption?.let { option ->
          jumpToScene(option.nextSceneId)
        }
      }
      is SceneNode.Jump -> jumpToScene(node.targetSceneId)
      is SceneNode.DiceRoll -> {
        val value = Dice(state).roll(node.name, node.sides)
        node.result = value
        state.nodeIndex++
      }
    }
  }

  fun jumpToScene(sceneId: String) {
    if (!scenes.containsKey(sceneId)) error("Сцена '$sceneId' не найдена")
    state.currentSceneId = sceneId
    state.nodeIndex = 0
  }
}