package com.olegkos.vnengine.engine.cards

data class DiceWithCards(
  val diceResult: Int,
  val maxCards: Int,
  val cardsInfo: List<CardInfo>
)

data class CardInfo(
  val value: Int,
  val image: String
)