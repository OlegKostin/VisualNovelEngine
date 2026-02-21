package com.olegkos.vnengine.engine


data class GameState(
  var currentSceneId: String,
  var nodeIndex: Int = 0,
  val flags: MutableMap<String, Boolean> = mutableMapOf()
)