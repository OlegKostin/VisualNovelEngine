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

  data class ModifyVar(val varName: String, val value: GameValue) : SceneNode

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

  data class Background(
    val image: String
  ) : SceneNode

  data class Image(
    val image: String
  ) : SceneNode

  data class Character(
    val image: String
  ) : SceneNode

  data class Effect(
    val image: String
  ) : SceneNode


}