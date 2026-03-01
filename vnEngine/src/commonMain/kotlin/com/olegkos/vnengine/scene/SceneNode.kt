package com.olegkos.vnengine.scene

sealed interface SceneNode {

  data class Text(val text: String) : SceneNode

  data class Choice(val options: List<Option>) : SceneNode

  data class Jump(val targetSceneId: String) : SceneNode

  data class DiceRoll(
    val name: String,
    val sides: Int,
    val difficulty: Int,
    val modifier: Int,
    val successScene: String,
    val failScene: String,
    val critSuccessScene: String?,
    val critFailScene: String?
  ) : SceneNode
}