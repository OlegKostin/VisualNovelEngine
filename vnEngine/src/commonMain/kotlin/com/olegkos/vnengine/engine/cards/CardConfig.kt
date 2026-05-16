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
  val weight: Int,
  /** opt_str | opt_wisdom | opt_will | opt_luck | opt_dark | opt_light */
  val tag: String
)