package com.olegkos.vnengine.engine

import com.olegkos.vnengine.scene.Scene
import com.olegkos.vnengine.scene.SceneNode

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

  fun next(choice: String? = null) {
    val node = currentNode()
    when (node) {
      is SceneNode.Text -> {
        // Переход к следующему узлу
        if (node.next != null) {
          state.currentSceneId = node.next
          state.nodeIndex = 0
        } else {
          state.nodeIndex++
        }
      }
      is SceneNode.Choice -> {
        if (choice != null && choice == node.text) {
          state.currentSceneId = node.next
          state.nodeIndex = 0
        }
      }
    }
  }

  fun jumpToScene(sceneId: String) {
    if (!scenes.containsKey(sceneId)) error("Сцена '$sceneId' не найдена")
    state.currentSceneId = sceneId
    state.nodeIndex = 0
  }
}