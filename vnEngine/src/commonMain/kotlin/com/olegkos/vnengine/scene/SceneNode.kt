package com.olegkos.vnengine.scene

sealed interface SceneNode {

  data class Text(
    val text: String
  ) : SceneNode

  data class Choice(
    val options: List<Option>
  ) : SceneNode

  data class Jump(
    val targetSceneId: String
  ) : SceneNode
}