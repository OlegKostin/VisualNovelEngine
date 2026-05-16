package com.olegkos.virtualnoveltesttwo.theme

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/** Текст без Material3 Typography — не падает на Skiko Desktop (baselineShift). */
@Composable
fun SkikoSafeText(
  text: String,
  modifier: Modifier = Modifier,
  color: Color = Color.Unspecified,
  fontSize: TextUnit = 16.sp,
  fontWeight: FontWeight = FontWeight.Normal,
  maxLines: Int = Int.MAX_VALUE,
  overflow: TextOverflow = TextOverflow.Clip
) {
  if (text.isEmpty()) return
  val size = fontSize.value.coerceIn(8f, 96f).sp
  BasicText(
    text = text,
    modifier = modifier,
    style = TextStyle(
      color = color,
      fontSize = size,
      lineHeight = (size.value * 1.25f).sp,
      fontWeight = fontWeight,
      baselineShift = BaselineShift.None,
      lineHeightStyle = null
    ),
    maxLines = maxLines,
    overflow = overflow
  )
}
