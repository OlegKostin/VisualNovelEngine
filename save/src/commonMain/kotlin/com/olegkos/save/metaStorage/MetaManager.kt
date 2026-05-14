package com.olegkos.save.metaStorage

import com.olegkos.vnengine.engine.cards.CardData
import java.util.UUID

class MetaManager(
  private val storage: MetaStorage
) {

  private var state: MetaState = storage.load()

  fun getCards(): List<CardInstance> = state.cards

  fun addCard(card: CardData): CardInstance {
    val newCard = CardInstance(
      value = card.value,
      image = card.image,
      id = UUID.randomUUID().toString()
    )

    state = state.copy(cards = state.cards + newCard)
    storage.save(state)

    return newCard
  }

  fun consumeCard(cardId: String) {
    state = state.copy(
      cards = state.cards.filterNot { it.id == cardId }
    )
    storage.save(state)
  }

  fun getDiceResult(id: String): Int? {
    return state.diceResults[id]?.result
  }

  fun saveDiceResult(id: String, result: Int) {
    if (state.diceResults.containsKey(id)) return

    val newMap = state.diceResults + (id to DiceInstance(id, result))
    state = state.copy(diceResults = newMap)
    storage.save(state)
  }
}