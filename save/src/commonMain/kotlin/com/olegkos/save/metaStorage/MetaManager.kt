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
}