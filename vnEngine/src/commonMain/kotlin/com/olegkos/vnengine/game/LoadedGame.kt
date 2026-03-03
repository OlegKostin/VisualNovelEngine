package com.olegkos.vnengine.game

import com.olegkos.vnengine.GameLoading.Scenario

data class LoadedGame(
  val scenario: Scenario,
  val assetsRoot: String
)