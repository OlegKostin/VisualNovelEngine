package com.olegkos.vnengine.dsl

import com.olegkos.vnengine.scene.Scene
import com.olegkos.vnengine.scene.SceneNode
import com.olegkos.vnengine.scene.Option

@DslMarker
annotation class ScenarioDslMarker

@ScenarioDslMarker
class SceneBuilder(val id: String) {
  private val _nodes = mutableListOf<SceneNode>()
  val nodes: List<SceneNode> get() = _nodes

  fun text(content: String) {
    _nodes += SceneNode.Text(content)
  }

  fun choice(vararg opts: Pair<String, String>) {
    val options = opts.map { Option(it.first, it.second) }
    _nodes += SceneNode.Choice(options)
  }

  fun jump(targetSceneId: String) {
    _nodes += SceneNode.Jump(targetSceneId)
  }

  fun dice(
    name: String,
    sides: Int = 20,
    difficulty: Int,
    modifier: Int = 0,
    success: String,
    fail: String,
    critSuccess: String? = null,
    critFail: String? = null
  ) {
    _nodes += SceneNode.DiceRoll(
      name = name,
      sides = sides,
      difficulty = difficulty,
      modifier = modifier,
      successScene = success,
      failScene = fail,
      critSuccessScene = critSuccess,
      critFailScene = critFail
    )
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