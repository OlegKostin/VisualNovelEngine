package com.olegkos.vnengine.engine.variables

import com.olegkos.vnengine.engine.round2
import kotlin.random.Random

sealed interface GameValue {

  data class Bool(val value: Boolean) : GameValue
  data class IntVal(val value: Int) : GameValue
  data class StringVal(val value: String) : GameValue
  data class FloatVal(val value: Float) : GameValue
  data class RandomInt(val min: Int, val max: Int) : GameValue
  data class RandomFloat(val min: Float, val max: Float) : GameValue
}

fun GameValue.resolve(): GameValue {
  return when (this) {

    is GameValue.RandomInt ->
      GameValue.IntVal(Random.nextInt(min, max + 1))

    is GameValue.RandomFloat ->
      GameValue.FloatVal(min + Random.nextFloat().round2() * (max - min))

    else -> this
  }
}