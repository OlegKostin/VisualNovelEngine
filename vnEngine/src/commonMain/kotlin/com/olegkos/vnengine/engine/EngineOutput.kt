package com.olegkos.vnengine.engine

import com.olegkos.vnengine.scene.Option

sealed interface EngineOutput {
  data class ShowText(val text: String) : EngineOutput
  data class ShowChoices(val options: List<Option>) : EngineOutput
  data class ShowDice(val name: String, val sides: Int, val result: Int?) : EngineOutput
}