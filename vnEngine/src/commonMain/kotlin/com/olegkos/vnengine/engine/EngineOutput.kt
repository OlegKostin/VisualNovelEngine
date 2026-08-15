package com.olegkos.vnengine.engine

import com.olegkos.vnengine.engine.cardgame.CardGameOutcome
import com.olegkos.vnengine.engine.cardgame.CardGamePhase
import com.olegkos.vnengine.engine.cardgame.ClashResolution
import com.olegkos.vnengine.engine.cards.CardData
import com.olegkos.vnengine.scene.Option
import com.olegkos.vnengine.scene.SceneNode
import com.olegkos.vnengine.scene.PanImageDirection
import com.olegkos.vnengine.scene.SpriteSheetScale
import com.olegkos.vnengine.scene.SubClass

sealed interface EngineOutput {
  object Loading : EngineOutput
  data class ShowText(
    val speaker: String?,
    val speakerVar: String?,
    val text: String,
    val long: Boolean = false,
    val light: Boolean = false,
  ) : EngineOutput

  /** Затемнение экрана (нода [SceneNode.TimeSkip]). */
  data class ShowTimeSkip(
    val durationMs: Long,
    val text: String? = null,
  ) : EngineOutput

  data class ShowChoices(
    val options: List<Option>,
    val prompt: String? = null,
  ) : EngineOutput
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
  data class SpriteAnimationLayerOutput(
    val image: String,
    val columns: Int = 4,
    val rows: Int = 4,
    val frameDurationMs: Long = 80,
    val loop: Boolean = true,
    val scale: SpriteSheetScale = SpriteSheetScale.Fit,
  )

  data class ShowSpriteAnimation(
    val layers: List<SpriteAnimationLayerOutput>,
    val clicksToAdvance: Int = 2,
    val text: String? = null,
    val speaker: String? = null,
  ) : EngineOutput

  data class ShowPanImage(
    val image: String,
    val direction: PanImageDirection,
    val durationMs: Long,
    val endAtCenter: Boolean = true,
    val clicksToAdvance: Int = 1,
    val text: String? = null,
    val speaker: String? = null,
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
    val image: String?,
    val addToMeta: Boolean = true,
  ) : EngineOutput
  data class ShowSceneView(
    val background: String,
    val navigation: SceneNode.Navigation?,
    val hotspots: List<SceneNode.Hotspot>
  ) : EngineOutput

  data class ShowAcademyDaySummary(
    val day: Int,
    val title: String = "Итоги дня",
    val changes: List<AcademyDayVarChangeUi>,
  ) : EngineOutput

  data class AcademyDayVarChangeUi(
    val label: String,
    val before: String,
    val after: String,
    val delta: String,
    /** −1 / 0 / +1 для цвета в UI */
    val deltaSign: Int = 0,
  )

  data class AcademyStatUi(
    val varName: String,
    val label: String,
    val displayValue: String,
  )

  data class ShowAcademyHub(
    val background: String,
    val day: Int,
    /** «Будний день» / «Выходной» */
    val dayKindLabel: String = "",
    /** «Будни 3/5» или «Выходные 1/2» */
    val dayCycleLabel: String = "",
    val planning: Boolean,
    val resources: Int,
    val resourcesLabel: String = "Ресурсы",
    val stats: List<AcademyStatUi> = emptyList(),
    val buildingGroups: List<AcademyBuildingGroupUi>,
    val timeSlots: List<AcademyTimeSlotUi>,
    val canCommit: Boolean,
    val commitBlockedReason: String?,
    val selectedBuildingId: String?,
    val buildUsedToday: Boolean,
    val unlockableActions: List<AcademyUnlockableUi> = emptyList(),
    val pendingUnlockLabel: String? = null,
    val laws: List<AcademyLawUi> = emptyList(),
    /** NORMAL | FULL_DAY */
    val planMode: String = "NORMAL",
    /** Есть ли сегодня хотя бы одно fullDay-действие */
    val fullDayModeAvailable: Boolean = false,
    val fullDayActivities: List<AcademyActivityOptionUi> = emptyList(),
    val selectedFullDayActivityId: String? = null,
    val commitDayLabel: String = "Подтвердить день",
  ) : EngineOutput

  enum class AcademyLawStatus {
    ENACTED,
    AVAILABLE,
    LOCKED,
  }

  data class AcademyLawUi(
    val id: String,
    val label: String,
    val status: AcademyLawStatus,
    val lockedReason: String?,
    val cost: Int,
    val requirementsText: String,
    val descriptionText: String,
    val actionLabel: String,
    val actionEnabled: Boolean,
    /** Что даст закон (из effectHint или effects в JSON). */
    val effectSummary: String? = null,
  )

  data class AcademyUnlockableUi(
    val id: String,
    val label: String,
    val status: AcademyUnlockableStatus,
    val lockedReason: String?,
    val selectedForTomorrow: Boolean,
    val requirementsText: String,
    val descriptionText: String,
    val actionLabel: String,
    val actionEnabled: Boolean,
    val completed: Boolean = false,
  )

  enum class AcademyUnlockableStatus {
    LOCKED,
    CAN_QUEUE,
    PENDING,
    ACTIVE,
  }

  data class AcademyBuildingGroupUi(
    val id: String,
    val label: String,
    val buildings: List<AcademyBuildingUi>,
  )

  data class AcademyBuildingUi(
    val id: String,
    val label: String,
    val group: String,
    val level: Int,
    val xPercent: Float,
    val yPercent: Float,
    val enabled: Boolean,
    val lockedReason: String?,
    val selected: Boolean,
    val statusLabel: String = "",
    val isBuilt: Boolean = false,
    val buildCost: Int? = null,
    val requirementsText: String = "",
    val descriptionText: String = "",
    val actionLabel: String = "",
    val completed: Boolean = false,
  )

  data class AcademyTimeSlotUi(
    val phaseId: String,
    val label: String,
    val selectedActivityId: String?,
    val activities: List<AcademyActivityOptionUi>,
  )

  data class AcademyActivityOptionUi(
    val id: String,
    val label: String,
    val fromBuilding: Boolean = false,
    val fromUnlockable: Boolean = false,
    /** Зелёная подсветка «новое строение» — только в день после постройки. */
    val highlightBuilding: Boolean = false,
  )
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
    /** Сколько карт взять всего (meta + колода). */
    val draftHandSize: Int,
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
    /** Все реплики vnAfterClash показаны (фаза BATTLE_REVEAL). */
    val vnPlaybackComplete: Boolean = true,
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

  data class ShowTargetTap(
    val gameId: String,
    val overlayDarkness: Float,
    val prompt: String?,
    val lifetimeMs: Long,
    val startScale: Float,
    val endScale: Float,
    val hitRadiusPercent: Float,
    val spawnDelayMs: Long,
    val targetCount: Int,
    val caughtCount: Int,
    val missCount: Int,
    val maxMisses: Int,
    val awaitingSpawn: Boolean,
    val started: Boolean = false,
    val startPrompt: String? = null,
    val activeTargets: List<TargetTapTargetUi> = emptyList(),
  ) : EngineOutput

  data class TargetTapTargetUi(
    val id: String,
    val image: String,
    val xPercent: Float,
    val yPercent: Float,
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