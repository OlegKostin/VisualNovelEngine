package com.olegkos.virtualnoveltesttwo

import androidx.compose.ui.awt.ComposeWindow
import java.awt.Rectangle
import java.awt.Robot
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal object RegisteredComposeWindow {
  var window: ComposeWindow? = null
}

actual suspend fun captureScreenshotPngBytes(): ByteArray? =
  withContext(Dispatchers.Default) {
    val w = RegisteredComposeWindow.window ?: return@withContext null
    runCatching {
      val img = Robot().createScreenCapture(Rectangle(w.x, w.y, w.width, w.height))
      ByteArrayOutputStream().also { baos -> ImageIO.write(img, "png", baos) }.toByteArray()
    }.getOrNull()
  }
