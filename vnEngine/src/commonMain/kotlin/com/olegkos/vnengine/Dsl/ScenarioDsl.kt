package com.olegkos.vnengine.dsl

import com.olegkos.vnengine.scene.Scene
import com.olegkos.vnengine.scene.SceneNode

@DslMarker
annotation class ScenarioDslMarker

@ScenarioDslMarker
class SceneBuilder(val id: String) {
  private val _nodes = mutableListOf<SceneNode>()
  val nodes: List<SceneNode> get() = _nodes

  fun text(content: String, next: String? = null) {
    _nodes += SceneNode.Text(content, next)
  }

  fun choice(text: String, next: String) {
    _nodes += SceneNode.Choice(text, next)
  }
}

class ScenarioBuilder {
  private val _scenes = mutableMapOf<String, Scene>()
  val scenes: Map<String, Scene> get() = _scenes

  fun scene(id: String, block: SceneBuilder.() -> Unit) {
    val builder = SceneBuilder(id)
    builder.block()
    _scenes[id] = Scene(id, builder.nodes)
  }
}

fun scenario(block: ScenarioBuilder.() -> Unit): Map<String, Scene> {
  val builder = ScenarioBuilder()
  builder.block()
  return builder.scenes
}