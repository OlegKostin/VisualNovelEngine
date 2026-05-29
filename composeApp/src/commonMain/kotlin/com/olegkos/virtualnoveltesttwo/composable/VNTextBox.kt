package com.olegkos.virtualnoveltesttwo.composable

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VNTextBox(
  text: String,
  speaker: String? = null,
  long: Boolean = false,
  onNext: () -> Unit
) {
  var visibleCount by remember { mutableIntStateOf(0) }
  var isFullyShown by remember { mutableStateOf(false) }
  var skipRequested by remember { mutableStateOf(false) }
  var isFastMode by remember { mutableStateOf(false) }

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
        break
      }

      visibleCount = i + 1

      val baseDelay = when (text[i]) {
        '.', '!', '?' -> 400
        ',', ';' -> 250
        else -> 100
      }
      val delayTime = if (isFastMode) baseDelay / 5 else baseDelay
      kotlinx.coroutines.delay(delayTime.toLong())
    }

    isFullyShown = true
  }

  BoxWithConstraints(
    modifier = Modifier.fillMaxSize()
  ) {
    val maxLines = if (long) 4 else 2
    val fontSize = (maxHeight.value * 0.038f).sp
    val lineHeight = (fontSize.value * 1.35f).sp
    val density = LocalDensity.current
    val boxHeight = with(density) {
      (lineHeight.toPx() * maxLines + 24.dp.toPx()).toDp()
    }
    val arrowSize = (maxHeight.value * 0.05f).sp
    val textBoxBg = Color(0x55BBDEFB)
    val speakerBg = Color(0x88BBDDFF)

    val visibleText = text.take(visibleCount)

    val onScreenClick = {
      when {
        !isFullyShown && !isFastMode -> isFastMode = true
        !isFullyShown && isFastMode -> skipRequested = true
        else -> {
          isFastMode = false
          onNext()
        }
      }
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .clickable(
          indication = null,
          interactionSource = remember { MutableInteractionSource() },
        ) { onScreenClick() }
        .padding(16.dp),
      verticalArrangement = Arrangement.Bottom,
      horizontalAlignment = Alignment.Start
    ) {
      speaker?.let {
        Box(
          modifier = Modifier
            .wrapContentWidth()
            .background(speakerBg, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .padding(bottom = 6.dp)
        ) {
          Text(
            text = it,
            fontSize = fontSize,
            color = Color(0xFF111111),
          )
        }
      }


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
            color = Color.White.copy(alpha = 0.15f),
            shape = RoundedCornerShape(16.dp)
          )
          .background(textBoxBg)
          .padding(horizontal = 12.dp, vertical = 10.dp)
      ) {
        Text(
          text = visibleText,
          fontSize = fontSize,
          lineHeight = lineHeight,
          color = Color(0xFF111111),
          maxLines = maxLines,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier
            .fillMaxWidth()
            .padding(end = arrowSize.value.dp + 8.dp)
        )

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