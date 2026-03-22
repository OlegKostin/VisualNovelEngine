package com.olegkos.vnengine.game

import kotlinx.serialization.Serializable

@Serializable
data class GameConfig(
  val startScenario: String,
  val assetsRoot: String,
  val variables: String,
)