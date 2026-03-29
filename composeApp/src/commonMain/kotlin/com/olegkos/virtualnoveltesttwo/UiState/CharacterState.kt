package com.olegkos.virtualnoveltesttwo.UiState

import androidx.compose.ui.Alignment


data class CharacterState(
  val id: String,
  val image: String,
  val alignment: Alignment,
  val scale: Float = 1f,
  val position: String,
)

