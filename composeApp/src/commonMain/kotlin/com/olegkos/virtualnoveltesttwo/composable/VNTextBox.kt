package com.olegkos.virtualnoveltesttwo.composable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
  var visibleText by remember { mutableStateOf("") }
  var isFullyShown by remember { mutableStateOf(false) }
  var skipRequested by remember { mutableStateOf(false) }

  LaunchedEffect(text) {
    visibleText = ""
    isFullyShown = false
    skipRequested = false

    for (i in text.indices) {

      if (skipRequested) {
        visibleText = text
        isFullyShown = true
        return@LaunchedEffect
      }

      visibleText = text.substring(0, i + 1)
      kotlinx.coroutines.delay(80)
    }

    isFullyShown = true
  }

  Box(
    modifier = Modifier.fillMaxSize()
  ) {
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .background(androidx.compose.ui.graphics.Color(0x22000000))
        .clickable {
          if (!isFullyShown) {
            skipRequested = true
          } else {
            onNext()
          }
        }
        .padding(16.dp)
    ) {

      Box {
        Text(
          text = text,
          color = androidx.compose.ui.graphics.Color.Black,
          modifier = Modifier.fillMaxWidth()
        )

        Text(
          text = visibleText,
          color = androidx.compose.ui.graphics.Color.White,
          modifier = Modifier.fillMaxWidth()
        )
      }

      Spacer(Modifier.height(4.dp))
    }
  }
}