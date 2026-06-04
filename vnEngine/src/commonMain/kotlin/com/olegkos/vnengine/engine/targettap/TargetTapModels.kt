package com.olegkos.vnengine.engine.targettap

data class TargetTapActiveTarget(
  val id: String,
  val image: String,
  val xPercent: Float,
  val yPercent: Float,
)

data class TargetTapState(
  val gameId: String,
  var started: Boolean = false,
  var spawnedTotal: Int = 0,
  var caughtTotal: Int = 0,
  var missCount: Int = 0,
  val activeTargets: MutableList<TargetTapActiveTarget> = mutableListOf(),
  var awaitingSpawn: Boolean = false,
  var targetSeq: Int = 0,
)
