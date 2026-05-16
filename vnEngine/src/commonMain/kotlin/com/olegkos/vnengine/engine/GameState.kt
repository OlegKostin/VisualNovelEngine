package com.olegkos.vnengine.engine

import com.olegkos.vnengine.engine.cardgame.CardGameState
import com.olegkos.vnengine.scene.SceneNode.BattleVnLine
import com.olegkos.vnengine.engine.variables.GameValue
import kotlinx.serialization.Serializable


@Serializable
data class VisibleCharacter(
  val id: String,
  val image: String,
  val position: String,
  val scale: Float = 1f
)

data class GameState(
  var pointer: NodePointer,
  val variables: MutableMap<String, GameValue> = mutableMapOf(),
  var diceResult: Int? = null,
  var diceModifiedResult: Float? = null,
  var pendingOutput: EngineOutput? = null,
  var waitingForUi: Boolean = false,
  val scenarioStack: ArrayDeque<NodePointer> = ArrayDeque(),
  var pendingDiceJumpScene: String? = null,
  var isGameInitialized: Boolean = false,
  var diceDuel: DiceDuelState? = null,
  var battle: BattleState? = null,
  var cardGame: CardGameState? = null,
  /** Макс. целые значения для переменных после InitGame (сейчас: health, mental_health). */
  var statCapsInt: Map<String, Int> = emptyMap(),
  /** Персонажи на экране (ShowCharacter без HideCharacter); участвует в save/load. */
  var visibleCharacters: List<VisibleCharacter> = emptyList()
)

data class BattleState(
  val battleId: String,
  var phase: BattlePhase = BattlePhase.START,
  var monsterHp: Int,
  val monsterMaxHp: Int,
  var pendingRoll: Int? = null,
  var pendingModified: Float? = null,
  var postCombatVnLines: List<BattleVnLine> = emptyList(),
  var postCombatVnIndex: Int = 0,
  var combatSummaryRoll: Int? = null,
  var combatSummarySides: Int? = null,
  var combatSummaryDifficulty: Int? = null,
  var combatSummaryModifier: Float? = null
)

data class DiceDuelState(
  val duelId: String,
  var phase: DiceDuelPhase = DiceDuelPhase.START,
  var playerRoll: Int? = null,
  var playerModified: Float? = null,
  var opponentRoll: Int? = null,
  var opponentModified: Float? = null,
  var winner: DiceDuelWinner? = null
)

enum class DiceDuelWinner {
  PLAYER,
  OPPONENT,
  DRAW
}
