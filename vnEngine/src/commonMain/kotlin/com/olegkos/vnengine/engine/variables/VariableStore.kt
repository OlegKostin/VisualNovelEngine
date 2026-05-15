package com.olegkos.vnengine.engine.variables


class VariableStore(
  private val map: MutableMap<String, GameValue>,
  private val intCaps: () -> Map<String, Int> = { emptyMap() }
) {

  private fun applyCapIfNeeded(name: String) {
    val cap = intCaps()[name] ?: return
    when (val v = map[name]) {
      is GameValue.IntVal -> {
        val nv = v.value.coerceIn(0, cap)
        if (nv != v.value) map[name] = GameValue.IntVal(nv)
      }
      is GameValue.FloatVal -> {
        val maxF = cap.toFloat()
        val nv = v.value.coerceIn(0f, maxF)
        if (nv != v.value) map[name] = GameValue.FloatVal(nv)
      }
      else -> Unit
    }
  }

  fun reapplyCap(name: String) {
    applyCapIfNeeded(name)
  }

  fun getInt(name: String): Int =
    (map[name] as? GameValue.IntVal)?.value ?: 0

  fun setInt(name: String, value: Int) {
    val cap = intCaps()[name]
    val v = if (cap != null) value.coerceIn(0, cap) else value
    map[name] = GameValue.IntVal(v)
  }

  fun addInt(name: String, value: Int) {
    setInt(name, getInt(name) + value)
  }

  fun getBool(name: String): Boolean =
    (map[name] as? GameValue.Bool)?.value ?: false

  fun setBool(name: String, value: Boolean) {
    map[name] = GameValue.Bool(value)
  }

  fun getString(name: String): String =
    (map[name] as? GameValue.StringVal)?.value ?: ""

  fun setString(name: String, value: String) {
    map[name] = GameValue.StringVal(value)
  }

  fun getFloat(name: String): Float =
    (map[name] as? GameValue.FloatVal)?.value ?: 0f

  fun setFloat(name: String, value: Float) {
    val cap = intCaps()[name]
    val v = if (cap != null) value.coerceIn(0f, cap.toFloat()) else value
    map[name] = GameValue.FloatVal(v)
  }

  fun addFloat(name: String, value: Float) {
    setFloat(name, getFloat(name) + value)
  }

  fun getModifier(name: String): Float {
    val v = map[name] ?: return 0f
    return when (v) {
      is GameValue.IntVal -> v.value.toFloat()
      is GameValue.FloatVal -> v.value
      else -> 0f
    }
  }
  fun set(name: String, value: GameValue) {
    map[name] = value
    applyCapIfNeeded(name)
  }

  fun modify(name: String, value: GameValue) {

    val old = map[name]

    map[name] = when {
      old is GameValue.IntVal && value is GameValue.IntVal ->
        GameValue.IntVal(old.value + value.value)

      old is GameValue.FloatVal && value is GameValue.FloatVal ->
        GameValue.FloatVal(old.value + value.value)

      old is GameValue.FloatVal && value is GameValue.IntVal ->
        GameValue.FloatVal(old.value + value.value.toFloat())

      old is GameValue.IntVal && value is GameValue.FloatVal ->
        GameValue.FloatVal(old.value.toFloat() + value.value)

      else -> value
    }
    applyCapIfNeeded(name)
  }
}