package com.olegkos.vnengine.engine

import com.olegkos.virtualnoveltesttwo.GameLoading.GameValue
import com.olegkos.virtualnoveltesttwo.GameLoading.NodePointer


data class GameState(
  var pointer: NodePointer,
  val variables: MutableMap<String, GameValue> = mutableMapOf(),
  var diceResult: Int? = null
)