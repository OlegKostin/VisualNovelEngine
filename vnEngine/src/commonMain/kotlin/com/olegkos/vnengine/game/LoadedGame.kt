package com.olegkos.vnengine.game

import com.olegkos.vnengine.GameLoading.Scenario
import com.olegkos.vnengine.engine.asserts.AssetPathResolver

data class LoadedGame(
  val scenario: Scenario,
  val variables: String,
  val assetsRoot: String
){
  val assets = AssetPathResolver(assetsRoot)
}