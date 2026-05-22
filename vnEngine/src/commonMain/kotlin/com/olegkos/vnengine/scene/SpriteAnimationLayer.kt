package com.olegkos.vnengine.scene

data class SpriteAnimationLayer(
  val image: String,
  val columns: Int = 4,
  val rows: Int = 4,
  val frameDurationMs: Long = 80,
  val loop: Boolean = true,
  val scale: SpriteSheetScale = SpriteSheetScale.Fit,
)
