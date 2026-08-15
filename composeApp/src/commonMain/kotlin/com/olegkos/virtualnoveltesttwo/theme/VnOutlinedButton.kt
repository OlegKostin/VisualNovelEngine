package com.olegkos.virtualnoveltesttwo.theme

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp

enum class VnButtonSurface {
  /** Светлые панели (меню, карточки, инит). */
  Light,
  /** Тёмный фон (хаб академии, итоги дня). */
  Dark,
}

@Composable
fun VnOutlinedButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  selected: Boolean = false,
  surface: VnButtonSurface = VnButtonSurface.Light,
  /**
   * Подложка без наведения/выделения.
   * По умолчанию прозрачная; для choices задаём цвет с alpha = половина от hover/selected.
   */
  idleFill: Color? = null,
  /** Переопределение заливки при наведении (иначе из palette). */
  hoverFill: Color? = null,
  colors: ButtonColors? = null,
  contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  content: @Composable RowScope.() -> Unit,
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

  val palette = when (surface) {
    VnButtonSurface.Light -> VnButtonPalette(
      labelDefault = Color(0xFF1A2433),
      labelHover = Color(0xFF050A12),
      labelDisabled = Color(0xFF7A8699),
      hoverFill = Color.White.copy(alpha = 0.24f),
      selectedFill = Color(0xFF3D5A80).copy(alpha = 0.18f),
      selectedBorder = Color(0xFF3D5A80),
      outlineBorder = Color(0xFF7C8AA8),
    )
    VnButtonSurface.Dark -> VnButtonPalette(
      labelDefault = Color(0xFFE8EDF5),
      labelHover = Color.White,
      labelDisabled = Color(0xFF6B7588),
      hoverFill = Color.White.copy(alpha = 0.14f),
      selectedFill = Color(0xFFBBDEFB).copy(alpha = 0.28f),
      selectedBorder = Color(0xFFBBDEFB),
      outlineBorder = Color(0x55FFFFFF),
    )
  }

  val idleContainer = idleFill ?: Color.Transparent
  val hoverContainer = hoverFill ?: palette.hoverFill

  val resolvedColors = colors ?: ButtonDefaults.outlinedButtonColors(
    contentColor = when {
      !enabled -> palette.labelDisabled
      selected -> palette.selectedBorder
      isHovered -> palette.labelHover
      else -> palette.labelDefault
    },
    containerColor = when {
      !enabled -> Color.Transparent
      selected -> palette.selectedFill
      isHovered -> hoverContainer
      else -> idleContainer
    },
    disabledContainerColor = Color.Transparent,
    disabledContentColor = palette.labelDisabled,
  )

  val border = when {
    selected -> BorderStroke(2.dp, palette.selectedBorder)
    else -> BorderStroke(1.dp, palette.outlineBorder)
  }

  OutlinedButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    colors = resolvedColors,
    border = border,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
    content = content,
  )
}

private data class VnButtonPalette(
  val labelDefault: Color,
  val labelHover: Color,
  val labelDisabled: Color,
  val hoverFill: Color,
  val selectedFill: Color,
  val selectedBorder: Color,
  val outlineBorder: Color,
)
