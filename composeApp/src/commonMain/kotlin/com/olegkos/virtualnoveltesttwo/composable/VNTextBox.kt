package com.olegkos.virtualnoveltesttwo.composable

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VNTextBox(
  text: String,
  onNext: () -> Unit
) {
  var visibleCount by remember { mutableIntStateOf(0) }
  var isFullyShown by remember { mutableStateOf(false) }
  var skipRequested by remember { mutableStateOf(false) }

  val infiniteTransition = rememberInfiniteTransition()
  val arrowAlpha by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(800),
      repeatMode = RepeatMode.Reverse
    )
  )

  LaunchedEffect(text) {
    visibleCount = 0
    isFullyShown = false
    skipRequested = false

    for (i in text.indices) {
      if (skipRequested) {
        visibleCount = text.length
        isFullyShown = true
        return@LaunchedEffect
      }

      visibleCount = i + 1

      val delayTime = when (text[i]) {
        '.', '!', '?' -> 200
        ',', ';' -> 120
        else -> 70
      }

      kotlinx.coroutines.delay(delayTime.toLong())
    }

    isFullyShown = true
  }

  BoxWithConstraints(
    modifier = Modifier.fillMaxSize()
  ) {
    val fontSize = (maxHeight.value * 0.035f).sp
    val lineHeight = (fontSize.value * 1.4f).sp
    val boxHeight = maxHeight * 0.20f
    val arrowSize = (maxHeight.value * 0.04f).sp

    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      contentAlignment = Alignment.BottomCenter
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(boxHeight)
          .clip(
            RoundedCornerShape(
              topStart = 16.dp,
              topEnd = 16.dp,
              bottomStart = 4.dp,
              bottomEnd = 4.dp
            )
          )
          .border(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.2f),
            shape = RoundedCornerShape(16.dp)
          )
          .background(Color(0xCCBBDEFB))
          .clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
          ) {
            if (!isFullyShown) skipRequested = true
            else onNext()
          }
          .padding(16.dp)
      ) {

        FlowRow(
          modifier = Modifier.fillMaxWidth()
        ) {
          text.forEachIndexed { index, char ->
            val targetAlpha = if (index < visibleCount) 1f else 0f
            val alpha by animateFloatAsState(
              targetValue = targetAlpha,
              animationSpec = tween(150),
              label = ""
            )

            Text(
              text = char.toString(),
              fontSize = fontSize,
              lineHeight = lineHeight,
              color = Color(0xFF111111).copy(alpha = alpha)
            )
          }
        }

        if (isFullyShown) {
          Box(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(4.dp)
          ) {
            Text(
              text = "▶",
              fontSize = arrowSize,
              color = Color.White.copy(alpha = arrowAlpha)
            )
          }
        }
      }
    }
  }
}