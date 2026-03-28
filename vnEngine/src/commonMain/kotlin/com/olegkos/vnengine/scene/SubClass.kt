package com.olegkos.vnengine.scene

sealed interface  SubClass {
  data class RangeCase(
    val min: Float,
    val max: Float,
    val scene: String
  )

}