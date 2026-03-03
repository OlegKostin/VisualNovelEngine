package com.olegkos.vnengine.GameLoading

sealed interface GameValue {

  data class Bool(val value: Boolean) : GameValue
  data class IntVal(val value: Int) : GameValue
  data class StringVal(val value: String) : GameValue
}