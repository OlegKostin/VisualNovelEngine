package com.olegkos.virtualnoveltesttwo.composable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.olegkos.vnengine.scene.SpriteSheetScale
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

/**
 * Один кадр спрайт-листа на весь экран.
 * Ячейка в пикселях: bitmap.width/columns × bitmap.height/rows.
 * Кадры: слева направо, затем следующий ряд.
 * При [text] — снизу [VNTextBox], переход как у обычной реплики.
 */
@Composable
fun SpriteSheetAnimationScreen(
  sheetBitmap: ImageBitmap?,
  columns: Int = 4,
  rows: Int = 4,
  frameDurationMs: Long = 80,
  loop: Boolean = true,
  clicksToAdvance: Int = 2,
  scale: SpriteSheetScale = SpriteSheetScale.Fit,
  text: String? = null,
  speaker: String? = null,
  onAdvance: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val hasText = !text.isNullOrBlank()
  var clicks by remember { mutableIntStateOf(0) }
  var frameIndex by remember { mutableIntStateOf(0) }
  val gridColumns = columns.coerceAtLeast(1)
  val gridRows = rows.coerceAtLeast(1)
  val frameCount = gridColumns * gridRows
  val col = frameIndex % gridColumns
  val row = frameIndex / gridColumns

  LaunchedEffect(sheetBitmap, frameDurationMs, frameCount, loop) {
    if (sheetBitmap == null) return@LaunchedEffect
    frameIndex = 0
    var frame = 0
    while (true) {
      delay(frameDurationMs.coerceAtLeast(16L))
      frame = if (loop) {
        (frame + 1) % frameCount
      } else {
        (frame + 1).coerceAtMost(frameCount - 1)
      }
      frameIndex = frame
    }
  }

  val boxModifier = modifier
    .fillMaxSize()
    .clip(RectangleShape)
    .then(
      if (!hasText) {
        Modifier.clickable(
          indication = null,
          interactionSource = remember { MutableInteractionSource() },
        ) {
          clicks++
          if (clicks >= clicksToAdvance) onAdvance()
        }
      } else {
        Modifier
      },
    )

  Box(modifier = boxModifier) {
    val bitmap = sheetBitmap
    if (bitmap == null) {
      Text(
        text = "Загрузка спрайт-листа…",
        modifier = Modifier.align(Alignment.Center),
      )
      return@Box
    }

    if (bitmap.width <= 0 || bitmap.height <= 0) {
      Text(
        text = "Некорректный спрайт-лист",
        modifier = Modifier.align(Alignment.Center),
      )
      return@Box
    }

    val cellW = bitmap.width / gridColumns
    val cellH = bitmap.height / gridRows
    if (cellW <= 0 || cellH <= 0) {
      Text(
        text = "Некорректная сетка $gridColumns×$gridRows",
        modifier = Modifier.align(Alignment.Center),
      )
      return@Box
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
      val dstW = size.width
      val dstH = size.height
      val (drawW, drawH) = when (scale) {
        SpriteSheetScale.Fit -> {
          val s = minOf(dstW / cellW, dstH / cellH)
          cellW * s to cellH * s
        }
        SpriteSheetScale.Crop -> {
          val s = maxOf(dstW / cellW, dstH / cellH)
          cellW * s to cellH * s
        }
        SpriteSheetScale.Fill -> dstW to dstH
      }
      drawImage(
        image = bitmap,
        srcOffset = IntOffset(col * cellW, row * cellH),
        srcSize = IntSize(cellW, cellH),
        dstOffset = IntOffset(
          ((dstW - drawW) / 2f).roundToInt(),
          ((dstH - drawH) / 2f).roundToInt(),
        ),
        dstSize = IntSize(drawW.roundToInt(), drawH.roundToInt()),
      )
    }

    if (hasText) {
      VNTextBox(
        text = text!!,
        speaker = speaker,
        onNext = onAdvance,
      )
    } else if (clicks == 1 && clicksToAdvance > 1) {
      Text(
        text = "▶",
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .offset(y = (-32).dp),
      )
    }
  }
}
