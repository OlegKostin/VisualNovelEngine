package com.olegkos.vnengine.engine.cards

class CardManager {

  private var cards: List<CardData> = emptyList()

  fun setCards(cards: List<CardData>) {
    this.cards = cards
  }

  fun drawCard(): CardData {
    require(cards.isNotEmpty()) { "Cards not loaded!" }

    println("CARDS SIZE: ${cards.size}")

    val totalWeight = cards.sumOf { it.weight }
    println("TOTAL WEIGHT: $totalWeight")

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
}