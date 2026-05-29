package com.olegkos.vnengine.engine.cardgame

object CardGameLogic {

  val MAIN_TAGS = setOf("opt_str", "opt_wisdom", "opt_will", "opt_luck")
  val TONE_TAGS = setOf("opt_dark", "opt_light")

  fun counters(attackerTag: String, defenderTag: String): Boolean {
    if (attackerTag !in MAIN_TAGS || defenderTag !in MAIN_TAGS) return false
    return when (attackerTag) {
      "opt_str" -> defenderTag == "opt_luck"
      "opt_luck" -> defenderTag == "opt_wisdom"
      "opt_wisdom" -> defenderTag == "opt_will"
      "opt_will" -> defenderTag == "opt_str"
      else -> false
    }
  }

  fun effectiveValues(cards: List<CardTagValue>, opponents: List<CardTagValue>): List<Int> =
    cards.map { card ->
      if (counteredByOpponent(card, opponents)) 0 else card.value
    }

  fun counteredByOpponent(card: CardTagValue, opponents: List<CardTagValue>): Boolean =
    opponents.any { opp -> counters(opp.tag, card.tag) }

  fun computeScore(
    cards: List<CardTagValue>,
    effective: List<Int>,
    battleTone: String
  ): ScoreBreakdown {
    val paired = cards.zip(effective)
    val main = paired.filter { it.first.tag !in TONE_TAGS }
    val tones = paired.filter { it.first.tag in TONE_TAGS }

    val base = main.sumOf { it.second }
    val matching = tones.filter { it.first.tag == battleTone }
    val nonMatching = tones.filter { it.first.tag != battleTone }

    val steps = mutableListOf<ScoreStep>()
    steps.add(ScoreStep("База (основные)", base.toString(), base))

    var total = base
    if (matching.isNotEmpty()) {
      if (base > 0) {
        val product = matching.fold(1) { acc, (_, v) -> acc * (v + 1) }
        val factorText = matching.joinToString(" × ") { "${it.second + 1}" }
        total = base * product
        steps.add(
          ScoreStep(
            label = "Тон $battleTone ($factorText)",
            detail = "$base × $product",
            runningTotal = total
          )
        )
      } else {
        total = 0
        steps.add(ScoreStep("Тон (база 0)", "0", 0))
      }
    }
    if (nonMatching.isNotEmpty()) {
      steps.add(ScoreStep("Противоположное течение", "0", total))
    }

    return ScoreBreakdown(
      total = total,
      base = base,
      steps = steps
    )
  }

  fun resolveClash(
    playerCards: List<CardTagValue>,
    enemyCards: List<CardTagValue>,
    battleTone: String
  ): ClashResolution {
    require(playerCards.size == 3 && enemyCards.size == 3)
    val pEff = effectiveValues(playerCards, enemyCards)
    val eEff = effectiveValues(enemyCards, playerCards)
    val pScore = computeScore(playerCards, pEff, battleTone)
    val eScore = computeScore(enemyCards, eEff, battleTone)
    return ClashResolution(
      playerCards = playerCards,
      enemyCards = enemyCards,
      playerEffective = pEff,
      enemyEffective = eEff,
      playerScore = pScore,
      enemyScore = eScore,
      playerTotal = pScore.total,
      enemyTotal = eScore.total
    )
  }

  fun randomBattleTone(): String = TONE_TAGS.random()

  /** Карта тона, совпадающая с [battleTone], может давать множитель; противоположная — нет. */
  fun isBeneficialToneCard(tag: String, battleTone: String): Boolean =
    tag !in TONE_TAGS || tag == battleTone

  /**
   * Скрытый выбор оппонента до хода игрока: [pool] → [handSize] → [clashSize].
   * Оценка только по своим картам (без знания руки игрока и контров).
   */
  fun pickEnemyClash(
    pool: List<HandCard>,
    handSize: Int,
    clashSize: Int,
    battleTone: String,
  ): List<HandCard> {
    require(pool.size >= handSize && handSize >= clashSize)

    fun scoreClash(clash: List<HandCard>): Int {
      val tags = clash.map { it.toTagValue() }
      val eff = effectiveValues(tags, emptyList())
      return computeScore(tags, eff, battleTone).total
    }

    fun clashAllowed(clash: List<HandCard>): Boolean =
      clash.all { isBeneficialToneCard(it.tag, battleTone) }

    var best: List<HandCard>? = null
    var bestScore = Int.MIN_VALUE

    for (hand in combinations(pool, handSize)) {
      for (clash in combinations(hand, clashSize)) {
        if (!clashAllowed(clash)) continue
        val score = scoreClash(clash)
        if (score > bestScore) {
          bestScore = score
          best = clash
        }
      }
    }

    if (best == null) {
      for (hand in combinations(pool, handSize)) {
        for (clash in combinations(hand, clashSize)) {
          val score = scoreClash(clash)
          if (score > bestScore) {
            bestScore = score
            best = clash
          }
        }
      }
    }

    return best ?: pool.take(clashSize)
  }

  private fun <T> combinations(items: List<T>, k: Int): List<List<T>> {
    if (k <= 0) return listOf(emptyList())
    if (items.size < k) return emptyList()
    if (k == items.size) return listOf(items)
    val head = items.first()
    val tail = items.drop(1)
    return combinations(tail, k - 1).map { listOf(head) + it } + combinations(tail, k)
  }
}

data class CardTagValue(val tag: String, val value: Int)

data class ScoreStep(val label: String, val detail: String, val runningTotal: Int)

data class ScoreBreakdown(
  val total: Int,
  val base: Int,
  val steps: List<ScoreStep>
)

data class ClashResolution(
  val playerCards: List<CardTagValue>,
  val enemyCards: List<CardTagValue>,
  val playerEffective: List<Int>,
  val enemyEffective: List<Int>,
  val playerScore: ScoreBreakdown,
  val enemyScore: ScoreBreakdown,
  val playerTotal: Int,
  val enemyTotal: Int
)
