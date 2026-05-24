package com.olegkos.vnengine.scene

enum class PanImageDirection {
  /** Камера едет вправо: сначала левый край, затем правый. */
  LeftToRight,
  /** Сначала правый край, затем левый. */
  RightToLeft,
  /** Сверху вниз (для высоких изображений). */
  TopToBottom,
  /** Снизу вверх. */
  BottomToTop,
  ;

  companion object {
    fun fromJson(raw: String?): PanImageDirection = when (raw?.lowercase()) {
      "righttoleft", "right_to_left", "rtl" -> RightToLeft
      "toptobottom", "top_to_bottom", "ttb", "down" -> TopToBottom
      "bottomtotop", "bottom_to_top", "btt", "up" -> BottomToTop
      else -> LeftToRight
    }
  }
}
