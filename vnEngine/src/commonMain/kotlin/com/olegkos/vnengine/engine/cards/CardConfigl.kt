package com.olegkos.vnengine.engine.cards

import kotlinx.serialization.Serializable

@Serializable
data class CardConfig(
  val cards: List<CardData>
)

@Serializable
data class CardData(
  val value: Int,
  val image: String,
  val weight: Int
)