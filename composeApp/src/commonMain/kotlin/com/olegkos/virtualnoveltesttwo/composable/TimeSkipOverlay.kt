package com.olegkos.virtualnoveltesttwo.composable

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olegkos.virtualnoveltesttwo.theme.SkikoSafeText
import kotlinx.coroutines.delay

@Composable
fun TimeSkipOverlay(
  durationMs: Long,
  text: String?,
  onComplete: () -> Unit,
) {
  val alpha = remember { Animatable(0f) }
  val totalMs = durationMs.coerceIn(300L, 10_000L)
  val fadeInMs = (totalMs * 0.4f).toInt().coerceAtLeast(1)
  val holdMs = (totalMs * 0.2f).toLong()
  val fadeOutMs = (totalMs * 0.4f).toInt().coerceAtLeast(1)

  LaunchedEffect(totalMs) {
    alpha.snapTo(0f)
    alpha.animateTo(1f, animationSpec = tween(fadeInMs))
    delay(holdMs)
    alpha.animateTo(0f, animationSpec = tween(fadeOutMs))
    onComplete()
  }

  Box(
    Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = alpha.value))
      .clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
      ) { },
    contentAlignment = Alignment.Center,
  ) {
    text?.let { caption ->
      if (alpha.value >= 0.5f) {
        SkikoSafeText(
          text = caption,
          fontSize = 18.sp,
          color = Color.White.copy(alpha = alpha.value.coerceIn(0f, 1f)),
          modifier = Modifier.padding(horizontal = 32.dp),
        )
      }
    }
  }
}
