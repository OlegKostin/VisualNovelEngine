package com.olegkos.vnengine.engine

import com.olegkos.vnengine.engine.variables.GameValue


data class GameState(
  var pointer: NodePointer,
  val variables: MutableMap<String, GameValue> = mutableMapOf(),
  var diceResult: Int? = null,
  var diceModifiedResult: Float? = null,
  var pendingOutput: EngineOutput? = null,
  var waitingForUi: Boolean = false,
  val scenarioStack: ArrayDeque<NodePointer> = ArrayDeque(),
  var isGameInitialized: Boolean = false,
)