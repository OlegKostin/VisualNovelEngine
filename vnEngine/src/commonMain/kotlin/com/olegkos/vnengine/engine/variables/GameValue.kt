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

/** Short text for UI (e.g. class selection stats block, menu «Характеристики»). */
fun GameValue.forStatPreview(): String {
  val r = resolve()
  return when (r) {
    is GameValue.Bool -> r.value.toString()
    is GameValue.IntVal -> r.value.toString()
    is GameValue.StringVal -> r.value
    is GameValue.FloatVal -> formatStatDisplayNumber(r.value)
    else -> r.toString()
  }
}

/** До 2 знаков после запятой; целые без «.0». */
fun formatStatDisplayNumber(value: Float): String {
  val rounded = value.round2()
  if (rounded == rounded.toInt().toFloat()) {
    return rounded.toInt().toString()
  }
  return "%.2f".format(rounded).trimEnd('0').trimEnd('.')
}