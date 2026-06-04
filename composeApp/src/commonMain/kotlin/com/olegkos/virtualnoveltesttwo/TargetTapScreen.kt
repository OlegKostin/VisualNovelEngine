package com.olegkos.virtualnoveltesttwo

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.olegkos.virtualnovelapp.GameViewModel
import com.olegkos.virtualnoveltesttwo.theme.SkikoSafeText
import com.olegkos.vnengine.engine.EngineOutput
import kotlinx.coroutines.delay

@Composable
fun TargetTapScreen(
  output: EngineOutput.ShowTargetTap,
  viewModel: GameViewModel,
  imagePainter: @Composable (String) -> BitmapPainter?,
) {
  val resolvedIds = remember(output.gameId) { mutableSetOf<String>() }

  LaunchedEffect(output.awaitingSpawn, output.caughtCount, output.missCount, output.activeTargets.size) {
    if (output.awaitingSpawn) {
      delay(output.spawnDelayMs.coerceAtLeast(0L))
      viewModel.targetTapContinueSpawn()
      return@LaunchedEffect
    }
    if (
      output.caughtCount < output.targetCount &&
      output.activeTargets.isEmpty()
    ) {
      delay(output.spawnDelayMs.coerceAtLeast(0L))
      viewModel.targetTapContinueSpawn()
    }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val minSide = min(maxWidth, maxHeight)
    val hitRadius = minSide * (output.hitRadiusPercent / 100f)

    Box(
      Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = output.overlayDarkness.coerceIn(0f, 1f))),
    )

    Column(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 24.dp, start = 16.dp, end = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      output.prompt?.let { caption ->
        SkikoSafeText(
          text = caption,
          fontSize = 20.sp,
          color = Color.White,
          modifier = Modifier.padding(bottom = 8.dp),
        )
      }
      SkikoSafeText(
        text = "${output.caughtCount} / ${output.targetCount}",
        fontSize = 16.sp,
        color = Color.White.copy(alpha = 0.85f),
      )
      if (output.maxMisses > 1) {
        val livesLeft = (output.maxMisses - output.missCount).coerceAtLeast(0)
        SkikoSafeText(
          text = "Жизни: $livesLeft",
          fontSize = 14.sp,
          color = Color(0xFFFFAB91),
          modifier = Modifier.padding(top = 4.dp),
        )
      }
    }

    output.activeTargets.forEach { target ->
      key(target.id) {
        TargetTapTarget(
          targetId = target.id,
          imagePath = target.image,
          xPercent = target.xPercent,
          yPercent = target.yPercent,
          lifetimeMs = output.lifetimeMs,
          startScale = output.startScale,
          endScale = output.endScale,
          hitRadius = hitRadius,
          maxWidth = maxWidth,
          maxHeight = maxHeight,
          imagePainter = imagePainter(target.image),
          alreadyResolved = target.id in resolvedIds,
          onResolved = { resolvedIds.add(target.id) },
          onHit = { viewModel.targetTapHit(target.id) },
          onMiss = { viewModel.targetTapMiss(target.id) },
        )
      }
    }
  }
}

@Composable
private fun TargetTapTarget(
  targetId: String,
  imagePath: String,
  xPercent: Float,
  yPercent: Float,
  lifetimeMs: Long,
  startScale: Float,
  endScale: Float,
  hitRadius: Dp,
  maxWidth: Dp,
  maxHeight: Dp,
  imagePainter: BitmapPainter?,
  alreadyResolved: Boolean,
  onResolved: () -> Unit,
  onHit: () -> Unit,
  onMiss: () -> Unit,
) {
  val scale = remember(targetId) { Animatable(startScale.coerceAtLeast(0.05f)) }
  val resolved = remember(targetId) { androidx.compose.runtime.mutableStateOf(alreadyResolved) }

  LaunchedEffect(targetId, lifetimeMs) {
    if (resolved.value) return@LaunchedEffect
    scale.snapTo(startScale.coerceAtLeast(0.05f))
    scale.animateTo(
      targetValue = endScale.coerceAtLeast(0.05f),
      animationSpec = tween(lifetimeMs.coerceIn(400L, 30_000L).toInt()),
    )
    if (!resolved.value) {
      resolved.value = true
      onResolved()
      onMiss()
    }
  }

  val size = hitRadius * 2f * scale.value
  val xOffset = maxWidth * (xPercent / 100f) - size / 2f
  val yOffset = maxHeight * (yPercent / 100f) - size / 2f

  Box(
    modifier = Modifier
      .offset(x = xOffset, y = yOffset)
      .size(size)
      .clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        enabled = !resolved.value,
      ) {
        if (resolved.value) return@clickable
        resolved.value = true
        onResolved()
        onHit()
      },
    contentAlignment = Alignment.Center,
  ) {
    imagePainter?.let { painter ->
      Image(
        painter = painter,
        contentDescription = imagePath,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit,
      )
    }
  }
}
