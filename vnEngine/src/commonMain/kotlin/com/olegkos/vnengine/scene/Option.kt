package com.olegkos.vnengine.scene

data class Option(
  val text: String,
  val nextSceneId: String? = null,
  val nextScenarioFile: String? = null,
  /** Пусто = опция всегда видна. Иначе все условия должны выполниться. */
  val requires: List<SceneNode.WeightedRandomJump.Requirement> = emptyList(),
)
