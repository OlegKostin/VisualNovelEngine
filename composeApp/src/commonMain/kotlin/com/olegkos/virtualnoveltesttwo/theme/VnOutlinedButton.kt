package com.olegkos.virtualnoveltesttwo.theme

import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun VnOutlinedButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  colors: ButtonColors? = null,
  contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  content: @Composable RowScope.() -> Unit
) {
  var hoverDepth by remember { mutableIntStateOf(0) }
  val isHovered = hoverDepth > 0

  LaunchedEffect(interactionSource) {
    interactionSource.interactions.collect { interaction ->
      when (interaction) {
        is HoverInteraction.Enter -> hoverDepth = hoverDepth + 1
        is HoverInteraction.Exit -> hoverDepth = (hoverDepth - 1).coerceAtLeast(0)
      }
    }
  }

  val labelDefault = Color(0xFF1A2433)
  val labelHover = Color(0xFF050A12)
  val labelDisabled = Color(0xFF7A8699)
  val hoverFill = Color.White.copy(alpha = 0.24f)

  val resolvedColors = colors ?: ButtonDefaults.outlinedButtonColors(
    contentColor = when {
      !enabled -> labelDisabled
      isHovered -> labelHover
      else -> labelDefault
    },
    containerColor = if (enabled && isHovered) hoverFill else Color.Transparent,
    disabledContainerColor = Color.Transparent,
    disabledContentColor = labelDisabled
  )

  OutlinedButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    colors = resolvedColors,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
    content = content
  )
}
