package com.olegkos.vnengine.GameLoading

import com.olegkos.vnengine.scene.Scene

data class Scenario(
  val startSceneId: String,
  val scenes: Map<String, Scene>
)