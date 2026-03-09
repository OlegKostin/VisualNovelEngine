package com.olegkos.vnengine.scene

import com.olegkos.vnengine.engine.variables.GameValue

sealed interface SceneNode {

  data class Text(val text: String) : SceneNode

  data class Choice(val options: List<Option>) : SceneNode

  data class Jump(val targetSceneId: String) : SceneNode

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
  data class JumpScenario(val scenarioFile: String) : SceneNode
}