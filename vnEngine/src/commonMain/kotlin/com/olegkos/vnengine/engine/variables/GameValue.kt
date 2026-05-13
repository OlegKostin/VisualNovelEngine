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

/** Short text for UI (e.g. class selection stats block). */
fun GameValue.forStatPreview(): String {
  val r = resolve()
  return when (r) {
    is GameValue.Bool -> r.value.toString()
    is GameValue.IntVal -> r.value.toString()
    is GameValue.StringVal -> r.value
    is GameValue.FloatVal -> r.value.toString()
    else -> r.toString()
  }
}