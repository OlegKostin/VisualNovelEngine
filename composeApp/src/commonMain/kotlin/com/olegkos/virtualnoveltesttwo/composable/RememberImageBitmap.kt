package com.olegkos.virtualnoveltesttwo.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import com.olegkos.vnengine.GameLoading.AssetReader

@Composable
fun rememberImageBitmap(
  path: String,
  reader: AssetReader,
): ImageBitmap? {
  var bitmap by remember(path) { mutableStateOf<ImageBitmap?>(null) }

  LaunchedEffect(path) {
    val bytes = reader.readBytes(path)
    bitmap = loadImageBitmap(bytes.inputStream())
  }

  return bitmap
}

@Composable
fun rememberBitmapPainter(
  path: String,
  reader: AssetReader,
): BitmapPainter? {
  val bitmap = rememberImageBitmap(path, reader)
  return bitmap?.let { remember(it) { BitmapPainter(it) } }
}
