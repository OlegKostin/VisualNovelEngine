package com.olegkos.vnengine.scene

data class Option(
  val text: String,
  val nextSceneId: String? = null,
  val nextScenarioFile: String? = null
)