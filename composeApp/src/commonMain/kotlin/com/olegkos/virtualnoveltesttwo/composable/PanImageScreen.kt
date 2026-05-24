package com.olegkos.virtualnoveltesttwo.composable

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.olegkos.vnengine.GameLoading.AssetReader
import com.olegkos.vnengine.engine.asserts.AssetPathResolver
import com.olegkos.vnengine.scene.PanImageDirection
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * Панорама по широкому/высокому изображению.
 * С [text] — [VNTextBox] после (или во время) панорамы; без текста — клик после конца.
 */
@Composable
fun PanImageScreen(
  image: String,
  direction: PanImageDirection,
  durationMs: Long,
  clicksToAdvance: Int,
  text: String?,
  speaker: String?,
  assets: AssetPathResolver,
  reader: AssetReader,
  onAdvance: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val imagePath = assets.image(image)
  val bitmap = rememberImageBitmap(imagePath, reader)
  val hasText = !text.isNullOrBlank()
  val scope = rememberCoroutineScope()

  var panDone by remember { mutableStateOf(false) }
  var clicks by remember { mutableIntStateOf(0) }
  val progress = remember { Animatable(initialProgress(direction)) }

  fun skipPanToEnd() {
    scope.launch {
      progress.snapTo(targetProgress(direction))
      panDone = true
    }
  }

  LaunchedEffect(bitmap, direction, durationMs) {
    if (bitmap == null) return@LaunchedEffect
    panDone = false
    val target = targetProgress(direction)
    progress.snapTo(initialProgress(direction))
    if (durationMs <= 0L) {
      progress.snapTo(target)
      panDone = true
    } else {
      progress.animateTo(target, tween(durationMs.toInt().coerceAtLeast(1)))
      panDone = true
    }
  }

  val canClickThrough = !hasText && panDone
  val boxModifier = modifier
    .fillMaxSize()
    .then(
      if (!hasText) {
        Modifier.clickable(
          indication = null,
          interactionSource = remember { MutableInteractionSource() },
        ) {
          if (!panDone) {
            skipPanToEnd()
            return@clickable
          }
          clicks++
          if (clicks >= clicksToAdvance) onAdvance()
        }
      } else {
        Modifier
      },
    )

  Box(modifier = boxModifier) {
    if (bitmap == null) {
      Text("Не удалось загрузить: $image", modifier = Modifier.align(Alignment.Center))
      return@Box
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
      PanImageCanvas(
        bitmap = bitmap,
        direction = direction,
        progress = progress.value,
        modifier = Modifier.fillMaxSize(),
      )
    }

    if (hasText && panDone) {
      VNTextBox(
        text = text!!,
        speaker = speaker,
        onNext = onAdvance,
      )
    } else if (hasText && !panDone) {
      Box(
        Modifier
          .fillMaxSize()
          .clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
          ) { skipPanToEnd() },
      )
    } else if (canClickThrough && clicks == 0 && clicksToAdvance > 1) {
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
private fun PanImageCanvas(
  bitmap: ImageBitmap,
  direction: PanImageDirection,
  progress: Float,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    if (bitmap.width <= 0 || bitmap.height <= 0) return@Canvas

    val horizontal = direction == PanImageDirection.LeftToRight ||
      direction == PanImageDirection.RightToLeft

    if (horizontal) {
      val scale = size.height / bitmap.height
      val drawW = bitmap.width * scale
      val drawH = size.height
      val maxPan = (drawW - size.width).coerceAtLeast(0f)
      val offsetX = (-progress * maxPan).roundToInt()
      drawImage(
        image = bitmap,
        dstOffset = IntOffset(offsetX, 0),
        dstSize = IntSize(drawW.roundToInt(), drawH.roundToInt()),
      )
    } else {
      val scale = size.width / bitmap.width
      val drawW = size.width
      val drawH = bitmap.height * scale
      val maxPan = (drawH - size.height).coerceAtLeast(0f)
      val offsetY = (-progress * maxPan).roundToInt()
      drawImage(
        image = bitmap,
        dstOffset = IntOffset(0, offsetY),
        dstSize = IntSize(drawW.roundToInt(), drawH.roundToInt()),
      )
    }
  }
}

private fun initialProgress(direction: PanImageDirection): Float =
  when (direction) {
    PanImageDirection.RightToLeft, PanImageDirection.BottomToTop -> 1f
    else -> 0f
  }

private fun targetProgress(direction: PanImageDirection): Float =
  when (direction) {
    PanImageDirection.RightToLeft, PanImageDirection.BottomToTop -> 0f
    else -> 1f
  }
