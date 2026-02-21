package com.olegkos.vnengine.scene


sealed class SceneNode {
  data class Text(
    val text: String,
    val next: String? = null   // куда идти после текста, null — следующий узел той же сцены
  ) : SceneNode()

  data class Choice(
    val text: String,
    val next: String          // идентификатор сцены, куда вести выбор
  ) : SceneNode()
}