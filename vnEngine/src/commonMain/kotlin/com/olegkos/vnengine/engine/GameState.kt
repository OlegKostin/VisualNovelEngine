package com.olegkos.vnengine.engine

import com.olegkos.vnengine.engine.variables.GameValue


data class GameState(
  var pointer: NodePointer,
  val variables: MutableMap<String, GameValue> = mutableMapOf(),
  var diceResult: Int? = null,
  val scenarioStack: ArrayDeque<NodePointer> = ArrayDeque(),
  var isGameInitialized: Boolean = false,
)