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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.olegkos.vnengine.GameLoading.AssetReader
import com.olegkos.vnengine.engine.EngineOutput.SpriteAnimationLayerOutput
import com.olegkos.vnengine.engine.asserts.AssetPathResolver
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
 * Один или несколько спрайт-листов: слои снизу вверх.
 * При [text] — снизу [VNTextBox]; без текста — два клика по экрану.
 */
@Composable
fun SpriteSheetAnimationScreen(
  layers: List<SpriteAnimationLayerOutput>,
  assets: AssetPathResolver,
  reader: AssetReader,
  clicksToAdvance: Int = 2,
  text: String? = null,
  speaker: String? = null,
  onAdvance: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val hasText = !text.isNullOrBlank()
  var clicks by remember { mutableIntStateOf(0) }

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
    if (layers.isEmpty()) {
      Text("Нет слоёв анимации", modifier = Modifier.align(Alignment.Center))
      return@Box
    }

    layers.forEach { layer ->
      key(layer.image) {
        SpriteSheetLayerFromAssets(
          imagePath = assets.image(layer.image),
          reader = reader,
          columns = layer.columns,
          rows = layer.rows,
          frameDurationMs = layer.frameDurationMs,
          loop = layer.loop,
          scale = layer.scale,
        )
      }
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

@Composable
private fun SpriteSheetLayerFromAssets(
  imagePath: String,
  reader: AssetReader,
  columns: Int,
  rows: Int,
  frameDurationMs: Long,
  loop: Boolean,
  scale: SpriteSheetScale,
) {
  val bitmap = rememberImageBitmap(imagePath, reader) ?: return
  SpriteSheetLayer(
    bitmap = bitmap,
    columns = columns,
    rows = rows,
    frameDurationMs = frameDurationMs,
    loop = loop,
    scale = scale,
    modifier = Modifier.fillMaxSize(),
  )
}

@Composable
private fun SpriteSheetLayer(
  bitmap: ImageBitmap,
  columns: Int,
  rows: Int,
  frameDurationMs: Long,
  loop: Boolean,
  scale: SpriteSheetScale,
  modifier: Modifier = Modifier,
) {
  var frameIndex by remember(bitmap, columns, rows) { mutableIntStateOf(0) }
  val gridColumns = columns.coerceAtLeast(1)
  val gridRows = rows.coerceAtLeast(1)
  val frameCount = gridColumns * gridRows
  val col = frameIndex % gridColumns
  val row = frameIndex / gridColumns

  LaunchedEffect(bitmap, frameDurationMs, frameCount, loop) {
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

  if (bitmap.width <= 0 || bitmap.height <= 0) return

  val cellW = bitmap.width / gridColumns
  val cellH = bitmap.height / gridRows
  if (cellW <= 0 || cellH <= 0) return

  Canvas(modifier = modifier) {
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
}
