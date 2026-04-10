package com.olegkos.vnengine.engine.cards

class CardManager(
  private val cards: List<CardData>
) {

  fun drawCard(): CardData {
    val totalWeight = cards.sumOf { it.weight }
    val rnd = (1..totalWeight).random()

    var current = 0

    for (card in cards) {
      current += card.weight
      if (rnd <= current) return card
    }

    return cards.last()
  }
}