package com.olegkos.save.metaStorage

import kotlinx.serialization.Serializable

@Serializable
data class MetaState(
  val cards: List<CardInstance> = emptyList(),
  val diceResults: Map<String, DiceInstance> = emptyMap(),
  /** Уже выданные drawCard: ключ = scenario|sceneId|nodeIndex */
  val drawCardNodes: Map<String, CardInstance> = emptyMap(),
)

@Serializable
data class CardInstance(
  val value: Int,
  val image: String,
  val id: String,
  val tag: String
)

@Serializable
data class DiceInstance(
  val id: String,
  val result: Int
)
