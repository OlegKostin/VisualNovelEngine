package com.olegkos.vnengine.engine

import com.olegkos.vnengine.engine.variables.GameValue
import kotlinx.serialization.*

@Serializable
data class NodePointer(
  val sceneId: String,
  val nodeIndex: Int
)

@Serializable
sealed class GameValueSerializable {
  @Serializable
  @SerialName("int") data class IntVal(val value: Int): GameValueSerializable()
  @Serializable
  @SerialName("float") data class FloatVal(val value: Float): GameValueSerializable()
  @Serializable
  @SerialName("bool") data class BoolVal(val value: Boolean): GameValueSerializable()
  @Serializable
  @SerialName("string") data class StringVal(val value: String): GameValueSerializable()
}
