package com.olegkos.vnengine.engine


data class GameState(
  var currentSceneId: String,
  var nodeIndex: Int = 0,
  val flags: MutableMap<String, Boolean> = mutableMapOf(),
  val diceResults: MutableMap<String, List<Int>> = mutableMapOf() // для предопределённых бросков
) {
  fun setFlag(key: String, value: Boolean) {
    flags[key] = value
  }

  fun getFlag(key: String) = flags[key] ?: false

  fun setDiceResults(diceName: String, results: List<Int>) {
    diceResults[diceName] = results.toList()
  }

  fun getNextDiceResult(diceName: String): Int? {
    val results = diceResults[diceName] ?: return null
    return if (results.isNotEmpty()) results[0] else null
  }

  fun consumeDiceResult(diceName: String): Int? {
    val next = getNextDiceResult(diceName) ?: return null
    diceResults[diceName] = diceResults[diceName]!!.drop(1)
    return next
  }
}