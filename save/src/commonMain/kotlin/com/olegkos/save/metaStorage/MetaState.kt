package com.olegkos.save.metaStorage

import kotlinx.serialization.Serializable

@Serializable
data class MetaState(
  val cards: List<CardInstance>,
  val diceResults: Map<String, DiceInstance> = emptyMap()
)

@Serializable
data class CardInstance(
  val value: Int,
  val image: String,
  val id: String
)

@Serializable
data class DiceInstance(
  val id: String,
  val result: Int
)