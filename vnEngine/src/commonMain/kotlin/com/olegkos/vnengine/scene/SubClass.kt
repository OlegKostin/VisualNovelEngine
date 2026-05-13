package com.olegkos.vnengine.scene

import com.olegkos.vnengine.engine.variables.GameValue

sealed interface  SubClass {
  data class RangeCase(
    val min: Float,
    val max: Float,
    val scene: String
  )

  data class GameClass(
    val id: String,
    val name: String,
    val stats: Map<String, GameValue>
  )
}