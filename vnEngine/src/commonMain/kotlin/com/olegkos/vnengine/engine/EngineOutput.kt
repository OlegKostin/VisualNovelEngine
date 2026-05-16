package com.olegkos.vnengine.engine

import com.olegkos.vnengine.engine.cardgame.CardGameOutcome
import com.olegkos.vnengine.engine.cardgame.CardGamePhase
import com.olegkos.vnengine.engine.cardgame.ClashResolution
import com.olegkos.vnengine.engine.cards.CardData
import com.olegkos.vnengine.scene.Option
import com.olegkos.vnengine.scene.SceneNode
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
    val phase: DicePhase,
    val difficulty: Int,
    val cards: List<UiCard> = emptyList(),
  ) : EngineOutput
  data class ShowVar(val name: String, val value: String, val text: String?= null): EngineOutput
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
  data class DrawCardRequest(
    val random: Boolean?,
    val value: Int?,
    val image: String?
  ) : EngineOutput
  data class ShowSceneView(
    val background: String,
    val navigation: SceneNode.Navigation?,
    val hotspots: List<SceneNode.Hotspot>
  ) : EngineOutput
  data class ShowCard(
    val image: String,
    val id: String
  ) : EngineOutput
  data class ShowCardUsage(
    val diceResult: Int,
    val cards: List<CardData>,
    val maxCards: Int
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

  data class ShowDiceDuel(
    val duelId: String,
    val title: String,
    val sides: Int,
    val playerName: String?,
    val playerModifier: Float,
    val playerRoll: Int?,
    val playerTotal: Float?,
    val opponentName: String,
    val opponentImage: String,
    val opponentModifier: Float,
    val opponentRoll: Int?,
    val opponentTotal: Float?,
    val phase: DiceDuelPhase,
    val canUseCards: Boolean,
    val cards: List<UiCard> = emptyList(),
    val resultText: String? = null
  ) : EngineOutput

  data class ShowCardGame(
    val gameId: String,
    val title: String,
    val speaker: String?,
    val playerName: String?,
    val opponentName: String,
    val opponentImage: String,
    val phase: CardGamePhase,
    val battleTone: String,
    val draftMetaMax: Int,
    val draftPickCount: Int,
    val metaCards: List<CardGameUiCard> = emptyList(),
    val metaSelectedIds: Set<String> = emptySet(),
    val offerCards: List<CardGameUiCard> = emptyList(),
    val offerSelectedIds: Set<String> = emptySet(),
    val hand: List<CardGameUiCard> = emptyList(),
    val clashSelectedIds: Set<String> = emptySet(),
    val playerPlayed: List<CardGameUiCard> = emptyList(),
    val enemyPlayed: List<CardGameUiCard> = emptyList(),
    val cardsRevealed: Boolean = false,
    val clashResolution: ClashResolution? = null,
    val breakdownSide: String? = null,
    val breakdownStepIndex: Int = 0,
    val vnSpeaker: String? = null,
    val vnText: String? = null,
    val outcome: CardGameOutcome? = null,
    val resultText: String? = null
  ) : EngineOutput

  data class CardGameUiCard(
    val id: String,
    val value: Int,
    val image: String,
    val tag: String,
    val effectiveValue: Int? = null,
    val countered: Boolean = false,
    val faceDown: Boolean = false
  )

  data class ShowBattle(
    val battleId: String,
    val title: String,
    val playerName: String? = null,
    val monsterName: String,
    val monsterImage: String,
    val monsterHp: Int,
    val monsterMaxHp: Int,
    val monsterCombatDamage: Int,
    val monsterHorrorDamage: Int,
    val playerHealth: Int,
    val playerSanity: Int,
    val phase: BattlePhase,
    val diceName: String? = null,
    val sides: Int? = null,
    val difficulty: Int? = null,
    val result: Int? = null,
    val modifier: Float = 0f,
    val canUseCards: Boolean = false,
    val canEscape: Boolean = false,
    val postCombatVnSpeaker: String? = null,
    val postCombatVnText: String? = null
  ) : EngineOutput
}

enum class BattlePhase {
  START, HORROR, ACTION, COMBAT, ESCAPE, RESOLVE, POST_COMBAT_VN, WIN, LOSE, ESCAPED
}

enum class DicePhase {
  ROLL,
  RESULT,
  CARD_SELECTION,
  FINAL
}

enum class DiceDuelPhase {
  START,
  PLAYER_ROLL,
  PLAYER_MODIFY,
  OPPONENT_ROLL,
  RESOLVE
}