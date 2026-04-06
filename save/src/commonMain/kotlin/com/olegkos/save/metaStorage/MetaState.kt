package com.olegkos.save.metaStorage

import kotlinx.serialization.Serializable

@Serializable
data class MetaState(
  val cards: List<CardInstance>
)

@Serializable
data class CardInstance(
  val value: Int,
  val image: String,
  val id: String
)