package com.olegkos.virtualnoveltesttwo.composable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun VNTextBox(
  text: String,
  onNext: () -> Unit
) {
  var visibleCount by remember { mutableStateOf(0) }
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

      val char = text[i]
      val delayTime = when (char) {
        '.', '!', '?' -> 200
        ',', ';' -> 120
        else -> 100
      }

      kotlinx.coroutines.delay(delayTime.toLong())
    }

    isFullyShown = true
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    contentAlignment = Alignment.BottomCenter
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(androidx.compose.ui.graphics.Color(0xAA000000))
        .clickable {
          if (!isFullyShown) skipRequested = true
          else onNext()
        }
        .padding(16.dp)
    ) {

      Box {
        Text(
          text = text,
          color = androidx.compose.ui.graphics.Color.Transparent,
          modifier = Modifier.fillMaxWidth()
        )

        FlowRow {
          text.forEachIndexed { index, char ->
            val targetAlpha = if (index < visibleCount) 1f else 0f
            val alpha by animateFloatAsState(
              targetValue = targetAlpha,
              animationSpec = tween(200),
              label = ""
            )
            Text(
              text = char.toString(),
              color = androidx.compose.ui.graphics.Color.White.copy(alpha = alpha)
            )
          }
        }


        if (isFullyShown) {
          Text(
            text = "▶",
            color = androidx.compose.ui.graphics.Color.White.copy(alpha = arrowAlpha),
            modifier = Modifier.align(Alignment.BottomEnd)
          )
        }
      }
    }
  }
}