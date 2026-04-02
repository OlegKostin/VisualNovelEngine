package com.olegkos.vnengine.engine

import com.olegkos.vnengine.scene.Option
import com.olegkos.vnengine.scene.SubClass

sealed interface EngineOutput {
  object Loading : EngineOutput
  data class ShowText(
    val speaker: String?,
    val speakerVar: String?,
    val text: String
  ) : EngineOutput
  data class ShowChoices(val options: List<Option>) : EngineOutput
  data class ShowDice(
    val name: String,
    val sides: Int,
    val result: Int?,
    val modifier: Float,
    val difficulty: Int
  ) : EngineOutput
  data class ShowVar(val name: String, val value: String, val text: String): EngineOutput
  data class JumpScenarioOutput(val scenarioFile: String) : EngineOutput
  data class ShowBackground(
    val image: String
  ) : EngineOutput
  object EndOfScene : EngineOutput
  data class ShowImage(
    val image: String
  ) : EngineOutput
  data class ShowCharacter(
    val id: String,
    val image: String,
    val position: String,
    val scale: Float = 1f,
  ) : EngineOutput

  data class HideCharacter(
    val id: String
  ) : EngineOutput

  data class ShowInitGame(
    val playerNameVar: String,
    val classVar: String?,
    val classes: List<SubClass.GameClass>
  ) : EngineOutput
  data object HideImage : EngineOutput
  data class ShowEffect(
    val image: String
  ) : EngineOutput
}