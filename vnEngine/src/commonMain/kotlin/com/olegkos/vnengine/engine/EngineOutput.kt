package com.olegkos.vnengine.engine

import com.olegkos.vnengine.scene.Option

sealed interface EngineOutput {
  object Loading : EngineOutput
  data class ShowText(val text: String) : EngineOutput
  data class ShowChoices(val options: List<Option>) : EngineOutput
  data class ShowDice(
    val name: String,
    val sides: Int,
    val result: Int?,
    val modifier: Float,
    val difficulty: Int
  ) : EngineOutput
  data class JumpScenarioOutput(val scenarioFile: String) : EngineOutput
}