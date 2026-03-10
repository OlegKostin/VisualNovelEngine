package com.olegkos.save

import com.olegkos.vnengine.engine.GameValueSerializable
import com.olegkos.vnengine.engine.NodePointer
import kotlinx.serialization.Serializable

@Serializable
data class GameStateSerializable(
  val pointer: NodePointer,
  val variables: Map<String, GameValueSerializable>,
  val diceResult: Int? = null,
  val timestamp: Long,
  val scenario: String
)