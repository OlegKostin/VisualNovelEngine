package com.olegkos.virtualnovelapp

data class PlayerStatsUi(
  val health: Int,
  val mentalHealth: Int,
  val optStr: String,
  val optWisdom: String,
  val optWill: String,
  val optLuck: String,
  val extraOptVars: List<Pair<String, String>> = emptyList()
) {
  companion object {
    fun empty() = PlayerStatsUi(0, 0, "0", "0", "0", "0")
  }
}
