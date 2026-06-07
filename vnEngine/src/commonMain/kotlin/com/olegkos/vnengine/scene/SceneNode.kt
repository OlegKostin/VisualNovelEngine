package com.olegkos.vnengine.scene

import com.olegkos.vnengine.engine.variables.GameValue

sealed interface SceneNode {

  data class Text(
    val speaker: String? = null,
    val speakerVar: String? = null,
    val text: String,
    /** true — до 4 строк в текстбоксе; по умолчанию 2 */
    val long: Boolean = false,
    /** true — светлый цвет текста (для тёмного фона) */
    val light: Boolean = false,
  ) : SceneNode

  /** Краткое затемнение экрана — «прошло время». */
  data class TimeSkip(
    val durationMs: Long = 1200L,
    /** Подпись на чёрном экране, например «Прошло несколько часов…» */
    val text: String? = null,
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
    val critFailScene: String?,
    /** false — не сохранять бросок в meta; при повторном проходе снова показывается UI. */
    val addToMeta: Boolean = true,
  ) : SceneNode

  data class SetVar(val varName: String, val value: GameValue) : SceneNode
  data class DrawCard(
    val random: Boolean? = null,
    val value: Int? = null,
    val image: String? = null,
    /** false — не запоминать ноду (каждый проход заново), но карта всё равно добавляется в инвентарь. */
    val addToMeta: Boolean = true,
  ) : SceneNode

  data class ModifyVar(val varName: String, val value: GameValue, val text: String? = null) : SceneNode

  data class If(
    val variable: String,
    val equals: GameValue,
    val successScene: String,
    val failScene: String
  ) : SceneNode

  /** Все [requires] должны выполниться → [successScene], иначе [failScene]. */
  data class IfAll(
    val requires: List<WeightedRandomJump.Requirement>,
    val successScene: String,
    val failScene: String,
  ) : SceneNode

  /** Хотя бы одно из [requires] → [successScene], иначе [failScene]. */
  data class IfAny(
    val requires: List<WeightedRandomJump.Requirement>,
    val successScene: String,
    val failScene: String,
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

  /** Спрайт-лист(ы): слои рисуются снизу вверх. С текстом — VNTextBox снизу; без — два клика. */
  data class SpriteAnimation(
    val layers: List<SpriteAnimationLayer>,
    val text: String? = null,
    val speaker: String? = null,
    val speakerVar: String? = null,
  ) : SceneNode

  /**
   * Широкое (или высокое) изображение с панорамированием.
   * Горизонтальные направления: картинка по высоте экрана, движение по X.
   * Вертикальные: по ширине экрана, движение по Y.
   */
  data class PanImage(
    val image: String,
    val direction: PanImageDirection = PanImageDirection.LeftToRight,
    val durationMs: Long = 8_000L,
    /** После прохода по краям — остановка в центре кадра (для leftToRight: слева→вправо→центр). */
    val endAtCenter: Boolean = true,
    val text: String? = null,
    val speaker: String? = null,
    val speakerVar: String? = null,
    val clicksToAdvance: Int = 1,
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
    val sanityVar: String,
    /** Имя игрока на экране боя; значение читается из переменной (по умолчанию [my_name]). */
    val playerNameVar: String? = null
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

  data class BattleVnLine(
    val text: String,
    val speaker: String? = null
  )

  data class CombatPhase(
    val name: String,
    val sides: Int,
    val difficulty: Int,
    val modifierVar: String,
    val damageOnSuccess: Int = 1,
    val damageOnCritSuccess: Int = 2,
    val damageToPlayerOnFail: Int = 1,
    val vnAfterMonsterHit: Map<Int, List<BattleVnLine>> = emptyMap(),
    val vnAfterPlayerHit: Map<Int, List<BattleVnLine>> = emptyMap()
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

  data class CardGame(
    val id: String,
    val title: String,
    val speaker: String? = null,
    val opponent: CardGameOpponent,
    val draft: CardGameDraftConfig = CardGameDraftConfig(),
    val vnAfterClash: List<BattleVnLine> = emptyList(),
    val transitions: CardGameTransitions
  ) : SceneNode

  data class CardGameOpponent(
    val name: String,
    val image: String
  )

  data class CardGameDraftConfig(
    /** Сколько карт можно взять из meta (не больше [handSize]). */
    val metaMax: Int = 4,
    /** Сколько карт показать из глобальной колоды на драфте. */
    val offerCount: Int = 7,
    /** Размер руки после драфта: meta + колода = ровно [handSize]. */
    val handSize: Int = 4,
  )

  data class CardGameTransitions(
    val winScene: String,
    val loseScene: String,
    val drawScene: String
  )

  /** Глобальный хаб академии; данные в [configFile]. */
  data class AcademyHub(
    val configFile: String,
  ) : SceneNode

  /**
   * Мини-игра «поймай цель»: затемнённый экран, уменьшающиеся картинки из [images].
   * Победа — все [targetCount] пойманы; промах — исчезла без клика ([maxMisses] попыток).
   */
  data class TargetTap(
    val id: String,
    /** Пул картинок; для каждой цели выбирается случайная. */
    val images: List<String>,
    val targetCount: Int = 5,
    /** Сколько целей одновременно на экране (1 — по одной). */
    val simultaneous: Int = 1,
    /** Допустимо промахов; 1 — мгновенный fail на первом промахе. */
    val maxMisses: Int = 1,
    val lifetimeMs: Long = 2200L,
    val spawnDelayMs: Long = 300L,
    val overlayDarkness: Float = 0.75f,
    val startScale: Float = 1f,
    val endScale: Float = 0.15f,
    /** Радиус клика в % от меньшей стороны экрана. */
    val hitRadiusPercent: Float = 12f,
    val modifierVar: String? = null,
    val modifierLifetimeMsPerPoint: Long = 50L,
    val prompt: String? = null,
    /** Подпись до старта; клик по экрану запускает раунд. */
    val startPrompt: String? = null,
    /** true — цели сразу, без экрана подготовки. */
    val autoStart: Boolean = false,
    val successScene: String,
    val failScene: String,
  ) : SceneNode

}