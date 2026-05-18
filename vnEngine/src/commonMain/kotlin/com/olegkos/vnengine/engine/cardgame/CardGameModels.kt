package com.olegkos.vnengine.engine.cardgame

import com.olegkos.vnengine.engine.cards.CardData
import com.olegkos.vnengine.scene.SceneNode.BattleVnLine
import java.util.UUID

data class HandCard(
  val instanceId: String,
  val value: Int,
  val image: String,
  val tag: String,
  val fromMeta: Boolean
) {
  fun toTagValue() = CardTagValue(tag = tag, value = value)

  companion object {
    fun fromDeck(card: CardData): HandCard =
      HandCard(
        instanceId = UUID.randomUUID().toString(),
        value = card.value,
        image = card.image,
        tag = card.tag,
        fromMeta = false
      )

    fun fromMeta(id: String, value: Int, image: String, tag: String): HandCard =
      HandCard(
        instanceId = id,
        value = value,
        image = image,
        tag = tag,
        fromMeta = true
      )
  }
}

enum class CardGamePhase {
  /** Meta (сверху) + колода 8→4 (снизу) на одном экране. */
  DRAFT,
  SELECT_CLASH,
  /** Карты открыты; здесь же проигрывается vnAfterClash. */
  BATTLE_REVEAL,
  SCORE_BREAKDOWN,
  RESULT
}

enum class CardGameOutcome {
  WIN,
  LOSE,
  DRAW
}

data class CardGameState(
  val gameId: String,
  var phase: CardGamePhase = CardGamePhase.DRAFT,
  var battleTone: String = "",
  var metaPicked: List<HandCard> = emptyList(),
  var offerCards: List<HandCard> = emptyList(),
  var poolPicked: List<HandCard> = emptyList(),
  var hand: List<HandCard> = emptyList(),
  var discard: List<HandCard> = emptyList(),
  var playerClash: List<HandCard> = emptyList(),
  var enemyClash: List<HandCard> = emptyList(),
  var clashResolution: ClashResolution? = null,
  var breakdownSide: BreakdownSide = BreakdownSide.PLAYER,
  var breakdownStepIndex: Int = 0,
  var vnIndex: Int = 0,
  var outcome: CardGameOutcome? = null,
  var cardsRevealed: Boolean = false
)

enum class BreakdownSide {
  PLAYER,
  ENEMY,
  COMPARE
}
