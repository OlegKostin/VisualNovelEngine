package com.olegkos.vnengine.engine.cards

class CardManager {

  private var cards: List<CardData> = emptyList()

  fun setCards(cards: List<CardData>) {
    this.cards = cards
  }

  fun drawCard(): CardData {
    require(cards.isNotEmpty()) { "Cards not loaded!" }

    val totalWeight = cards.sumOf { it.weight }

    val rnd = (1..totalWeight).random()

    var current = 0
    for (card in cards) {
      current += card.weight
      if (rnd <= current) return card
    }

    return cards.last()
  }
  fun getByValue(value: Int): CardData? {
    return cards
      .filter { it.value == value }
      .randomOrNull()
  }

  /** Карта с точным путём изображения (как в колоде). */
  fun getByImage(image: String): CardData? {
    return cards
      .filter { it.image == image }
      .randomOrNull()
  }

  fun allCards(): List<CardData> = cards

  /** Несколько уникальных карт по image (взвешенная выборка без повторов). */
  fun drawUniqueWeighted(count: Int): List<CardData> {
    require(cards.isNotEmpty()) { "Cards not loaded!" }
    require(count > 0)
    val pool = cards.toMutableList()
    val result = mutableListOf<CardData>()
    repeat(count.coerceAtMost(pool.size)) {
      val picked = drawFromPool(pool)
      result += picked
      pool.removeAll { it.image == picked.image && it.value == picked.value && it.tag == picked.tag }
    }
    return result
  }

  fun drawWeighted(count: Int): List<CardData> {
    require(cards.isNotEmpty()) { "Cards not loaded!" }
    return List(count) { drawCard() }
  }

  private fun drawFromPool(pool: List<CardData>): CardData {
    val totalWeight = pool.sumOf { it.weight }
    val rnd = (1..totalWeight).random()
    var current = 0
    for (card in pool) {
      current += card.weight
      if (rnd <= current) return card
    }
    return pool.last()
  }
}