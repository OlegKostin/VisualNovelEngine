package com.olegkos.vnengine.engine.academy

import com.olegkos.vnengine.engine.variables.GameValue
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

fun AcademyLawConfig.resolvedOnEnact(): List<AcademyLawOnEnactEffectJson> =
  onEnact.ifEmpty { effects }

fun AcademyStatConfig.isFloatStat(): Boolean =
  type.equals("float", ignoreCase = true)

fun formatAcademyStatNumber(value: Float, isFloat: Boolean): String =
  if (isFloat) {
    val rounded = (value * 100).roundToInt() / 100f
    if (rounded == rounded.toInt().toFloat()) {
      rounded.toInt().toString()
    } else {
      "%.2f".format(rounded).trimEnd('0').trimEnd('.')
    }
  } else {
    value.roundToInt().toString()
  }

fun formatLawOnEnactLine(
  effect: AcademyLawOnEnactEffectJson,
  label: String,
  isFloat: Boolean,
): String {
  val delta = effect.delta
  return when {
    delta > 0 -> "$label +${formatAcademyStatNumber(delta.toFloat(), isFloat)}"
    delta < 0 -> "$label ${formatAcademyStatNumber(delta.toFloat(), isFloat)}"
    else -> label
  }
}

fun formatLawDailyLine(
  effect: AcademyLawDailyEffectJson,
  label: String,
): String {
  val a = effect.deltaMin
  val b = effect.deltaMax
  val lo = min(a, b)
  val hi = max(a, b)
  return if (lo == hi) {
    "$label ${formatSignedFloat(lo)} / день"
  } else {
    "$label ${formatSignedFloat(lo)}…${formatSignedFloat(hi)} / день"
  }
}

private fun formatSignedFloat(value: Float): String {
  val text = formatAcademyStatNumber(abs(value), isFloat = true)
  return if (value > 0) "+$text" else if (value < 0) "-$text" else "0"
}

fun randomDailyDelta(min: Float, max: Float): Float {
  val lo = min(min, max)
  val hi = max(min, max)
  return Random.nextFloat() * (hi - lo) + lo
}

fun readAcademyStatFloat(variables: Map<String, GameValue>, varName: String): Float =
  when (val v = variables[varName]) {
    is GameValue.IntVal -> v.value.toFloat()
    is GameValue.FloatVal -> v.value
    else -> 0f
  }
