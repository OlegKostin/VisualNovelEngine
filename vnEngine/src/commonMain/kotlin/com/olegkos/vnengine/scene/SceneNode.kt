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

  data class DiceRoll(
    val name: String,
    val sides: Int = 20,
    val difficulty: Int = 10,
    val modifier: Int = 0,

    val successScene: String? = null,
    val failScene: String? = null,

    val critSuccessScene: String? = null,
    val critFailScene: String? = null,

    var result: Int? = null
  ) : SceneNode
}