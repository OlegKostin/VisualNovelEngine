package com.olegkos.virtualnoveltesttwo.composable

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.olegkos.virtualnoveltesttwo.mappers.StatType
import org.jetbrains.compose.resources.painterResource
import java.awt.Cursor

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PulsingHoverBox(
  statKey: String,
  enablePulse: Boolean,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  var hovered by remember(statKey) { mutableStateOf(false) }

  val statPulseTransition = rememberInfiniteTransition(label = "hoverBoxPulse")
  val statPulse by statPulseTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.06f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200),
      repeatMode = RepeatMode.Reverse
    ),
    label = "hoverBoxPulseWave"
  )

  val hoverScale by animateFloatAsState(
    targetValue = if (hovered && enablePulse) 1.04f else 1f,
    animationSpec = spring(dampingRatio = 0.72f),
    label = "hoverBoxScale"
  )

  val scale = if (hovered && enablePulse) statPulse * hoverScale else 1f

  Box(
    modifier = modifier
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
        transformOrigin = TransformOrigin(0.5f, 0.5f)
      }
      .pointerMoveFilter(
        onEnter = {
          hovered = true
          false
        },
        onExit = {
          hovered = false
          false
        }
      )
      .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
    contentAlignment = Alignment.Center
  ) {
    content()
  }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PulsingStatValue(
  statKey: String,
  displayValue: String,
  statIconSize: Dp,
  enablePulse: Boolean,
  modifier: Modifier = Modifier
) {
  val stat = StatType.fromKey(statKey)
  var hovered by remember(statKey) { mutableStateOf(false) }

  val statPulseTransition = rememberInfiniteTransition(label = "playerStatPulse")
  val statPulse by statPulseTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.08f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200),
      repeatMode = RepeatMode.Reverse
    ),
    label = "playerStatPulseWave"
  )

  val hoverScale by animateFloatAsState(
    targetValue = if (hovered && enablePulse) 1.06f else 1f,
    animationSpec = spring(dampingRatio = 0.72f),
    label = "playerStatHover"
  )

  val combinedScale = if (hovered && enablePulse) statPulse * hoverScale else 1f
  val tooltipMaxWidth = 168.dp

  Box(
    modifier = modifier.widthIn(min = 72.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.pointerMoveFilter(
        onEnter = {
          hovered = true
          false
        },
        onExit = {
          hovered = false
          false
        }
      )
    ) {
      stat?.let {
        Image(
          painter = painterResource(it.image),
          contentDescription = statKey,
          modifier = Modifier
            .size(statIconSize)
            .graphicsLayer {
              scaleX = combinedScale
              scaleY = combinedScale
              transformOrigin = TransformOrigin(0.5f, 0.5f)
            }
            .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
        )
        Spacer(Modifier.width(statIconSize * 0.2f + 4.dp))
      }

      Text(
        text = displayValue,
        style = MaterialTheme.typography.headlineSmall,
        color = Color(0xFFE8ECF5)
      )
    }

    if (hovered && enablePulse) {
      val hint = StatType.hoverHintForKey(statKey)
      Surface(
        color = Color(0xE6FFFFFF),
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 4.dp,
        modifier = Modifier
          .align(Alignment.TopCenter)
          .widthIn(max = tooltipMaxWidth)
          .graphicsLayer { translationY = -statIconSize.value * 1.6f }
      ) {
        Text(
          text = hint,
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFF2A3142),
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
      }
    }
  }
}
