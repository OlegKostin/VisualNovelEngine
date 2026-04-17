package com.olegkos.vnengine.scene

import com.olegkos.vnengine.engine.variables.GameValue

sealed interface SceneNode {

  data class Text(
    val speaker: String? = null,
    val speakerVar: String? = null,
    val text: String
  ) : SceneNode

  data class Choice(val options: List<Option>) : SceneNode

  data class DiceRoll(
    val name: String,
    val sides: Int,
    val difficulty: Int,
    val modifierVar: String,
    val successScene: String,
    val failScene: String,
    val critSuccessScene: String?,
    val critFailScene: String?
  ) : SceneNode

  data class SetVar(val varName: String, val value: GameValue) : SceneNode
  data class DrawCard(
    val random: Boolean? = null,
    val value: Int? = null,
    val image: String? = null
  ) : SceneNode

  data class ModifyVar(val varName: String, val value: GameValue, val text: String) : SceneNode

  data class If(
    val variable: String,
    val equals: GameValue,
    val successScene: String,
    val failScene: String
  ) : SceneNode

  data class Switch(
    val variable: String,
    val cases: Map<String, String>,
    val default: String
  ) : SceneNode

  data class SwitchRange(
    val variable: String,
    val ranges: List<SubClass.RangeCase>,
    val default: String
  ) : SceneNode

  data class Jump(val targetSceneId: String) : SceneNode

  data class JumpScenario(val scenarioFile: String) : SceneNode

  data class SceneView(
    val background: String,
    val navigation: Navigation?,
    val hotspots: List<Hotspot>
  ) : SceneNode

  data class Navigation(
    val up: String?,
    val down: String?,
    val left: String?,
    val right: String?
  )

  data class Hotspot(
    val xPercent: Float,
    val yPercent: Float,
    val widthPercent: Float,
    val heightPercent: Float,
    val targetScenarioFile: String
  )

  data class Background(
    val image: String
  ) : SceneNode

  data class Image(
    val image: String
  ) : SceneNode

  data class ShowCharacter(
    val id: String,
    val image: String,
    val position: String,
    val scale: Float = 1f,
  ) : SceneNode
  data class HideCharacter(
    val id: String
  ) : SceneNode

  data class Effect(
    val image: String
  ) : SceneNode

  data class InitGame(
    val playerNameVar: String,
    val classVar: String?,
    val classes: List<SubClass.GameClass>,
    val nextSceneId: String
  ) : SceneNode
  data object HideImage : SceneNode

}