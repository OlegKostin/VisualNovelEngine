package com.olegkos.vnengine.engine.variables


class VariableStore(
  private val map: MutableMap<String, GameValue>
) {

  fun getInt(name: String): Int =
    (map[name] as? GameValue.IntVal)?.value ?: 0

  fun setInt(name: String, value: Int) {
    map[name] = GameValue.IntVal(value)
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
    map[name] = GameValue.FloatVal(value)
  }

  fun addFloat(name: String, value: Float) {
    setFloat(name, getFloat(name) + value)
  }
}