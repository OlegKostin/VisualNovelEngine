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

  data class ModifyVar(val varName: String, val value: GameValue, val text: String? = null) : SceneNode

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

  data class NavLink(
    val scenarioFile: String,
    val label: String? = null,
    val icon: String? = null
  )

  data class Navigation(
    val up: NavLink? = null,
    val down: NavLink? = null,
    val left: NavLink? = null,
    val right: NavLink? = null
  )

  data class Hotspot(
    val xPercent: Float,
    val yPercent: Float,
    val widthPercent: Float,
    val heightPercent: Float,
    val targetScenarioFile: String
  )

  data class WeightedRandomJump(
    val entries: List<Entry>,
    val defaultScene: String
  ) : SceneNode {
    data class Entry(
      val scene: String,
      val weight: Int = 1,
      val requires: List<Requirement> = emptyList()
    )

    data class Requirement(
      val variable: String,
      val op: Op,
      val value: GameValue
    )

    enum class Op { EQ, NEQ, GTE, LTE, GT, LT }
  }

  data class Background(
    val image: String
  ) : SceneNode

  data class Image(
    val image: String
  ) : SceneNode

  data class ShowCharacter(
    val id: String,
    val image: String? = null,

    val flagVar: String? = null,
    val trueImage: String? = null,
    val falseImage: String? = null,

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

  data class Battle(
    val id: String,
    val title: String,
    val monster: Monster,
    val player: PlayerRefs,
    val phases: BattlePhases,
    val transitions: BattleTransitions,
    val escape: EscapeConfig? = null
  ) : SceneNode

  data class Monster(
    val name: String,
    val image: String,
    val health: Int,
    val horrorDamage: Int = 0,
    val combatDamage: Int = 0
  )

  data class PlayerRefs(
    val healthVar: String,
    val sanityVar: String
  )

  data class BattlePhases(
    val horror: CheckPhase? = null,
    val combat: CombatPhase
  )

  data class CheckPhase(
    val enabled: Boolean = true,
    val name: String,
    val sides: Int,
    val difficulty: Int,
    val modifierVar: String,
    val onFailSanityDamage: Int = 0
  )

  data class CombatPhase(
    val name: String,
    val sides: Int,
    val difficulty: Int,
    val modifierVar: String,
    val damageOnSuccess: Int = 1,
    val damageOnCritSuccess: Int = 2,
    val damageToPlayerOnFail: Int = 1
  )

  data class EscapeConfig(
    val allowed: Boolean = true,
    val name: String,
    val sides: Int,
    val difficulty: Int,
    val modifierVar: String,
    val onFailPlayerDamage: Int = 0
  )

  data class BattleTransitions(
    val winScene: String,
    val loseScene: String,
    val escapeScene: String? = null
  )

  data class DiceDuel(
    val id: String,
    val title: String,
    val sides: Int = 20,
    val playerModifierVar: String,
    val opponent: DiceDuelOpponent,
    val cards: DiceDuelCards = DiceDuelCards(),
    val transitions: DiceDuelTransitions
  ) : SceneNode

  data class DiceDuelOpponent(
    val name: String,
    val image: String,
    val modifier: Float = 0f,
    val modifierVar: String? = null
  )

  data class DiceDuelCards(
    val allowCards: Boolean = true
  )

  data class DiceDuelTransitions(
    val winScene: String,
    val loseScene: String,
    val drawScene: String? = null
  )

}