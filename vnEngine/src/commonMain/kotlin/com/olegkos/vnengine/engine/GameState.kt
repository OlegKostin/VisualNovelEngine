package com.olegkos.vnengine.engine

import com.olegkos.vnengine.engine.variables.GameValue
import com.olegkos.vnengine.GameLoading.NodePointer


data class GameState(
  var pointer: NodePointer,
  val variables: MutableMap<String, GameValue> = mutableMapOf(),
  var diceResult: Int? = null
)