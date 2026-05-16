package com.olegkos.virtualnoveltesttwo.composable

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch

/** Колесо мыши на Desktop прокручивает горизонтальный [androidx.compose.foundation.horizontalScroll]. */
@Composable
fun Modifier.horizontalWheelScroll(scrollState: ScrollState, factor: Float = 12f): Modifier {
  val scope = rememberCoroutineScope()
  return pointerInput(scrollState, factor) {
    awaitPointerEventScope {
      while (true) {
        val event = awaitPointerEvent(PointerEventPass.Main)
        var wheel = 0f
        event.changes.forEach { change ->
          val delta = change.scrollDelta
          if (delta != Offset.Zero) {
            wheel += delta.y + delta.x
            change.consume()
          }
        }
        if (wheel != 0f) {
          scope.launch {
            scrollState.scroll { scrollBy(-wheel * factor) }
          }
        }
      }
    }
  }
}
