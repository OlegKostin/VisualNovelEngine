package com.olegkos.virtualnoveltesttwo.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

/** Размеры шрифтов хаба академии от высоты/ширины окна. */
data class AcademyHubTypography(
  val dayHeader: TextUnit,
  val phaseHeader: TextUnit,
  val panelTitle: TextUnit,
  val section: TextUnit,
  val body: TextUnit,
  val hint: TextUnit,
  val stat: TextUnit,
  val button: TextUnit,
  val menuTitle: TextUnit,
  val menuClose: TextUnit,
  val tab: TextUnit,
  val menuHint: TextUnit,
  val cardTitle: TextUnit,
  val cardBody: TextUnit,
  val cardButton: TextUnit,
  val cardRowMinHeight: Dp,
  val cardActionRowMinHeight: Dp,
) {
  fun lineHeight(font: TextUnit, factor: Float = 1.28f): TextUnit =
    (font.value * factor).sp

  companion object {
    fun fromViewport(heightDp: Float, widthDp: Float): AcademyHubTypography {
      val basis = min(heightDp, widthDp * 0.55f)
      val scale = (basis / 500f).coerceIn(1.25f, 1.85f)
      fun sz(px: Float) = (px * scale).sp
      fun dp(px: Float) = (px * scale).dp
      return AcademyHubTypography(
        dayHeader = sz(22f),
        phaseHeader = sz(17f),
        panelTitle = sz(17f),
        section = sz(14f),
        body = sz(14f),
        hint = sz(12f),
        stat = sz(13f),
        button = sz(15f),
        menuTitle = sz(20f),
        menuClose = sz(16f),
        tab = sz(14f),
        menuHint = sz(13f),
        cardTitle = sz(16f),
        cardBody = sz(13f),
        cardButton = sz(12f),
        cardRowMinHeight = dp(52f),
        cardActionRowMinHeight = dp(48f),
      )
    }

    val Default: AcademyHubTypography = fromViewport(720f, 1280f)
  }
}

val LocalAcademyHubTypography = compositionLocalOf { AcademyHubTypography.Default }
