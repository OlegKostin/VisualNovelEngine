package com.olegkos.vnengine

import com.olegkos.vnengine.GameLoading.AssetReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException

class DesktopAssetReader : AssetReader {

  override suspend fun readText(path: String): String =
    withContext(Dispatchers.IO) {
      val stream = Thread.currentThread()
        .contextClassLoader
        .getResourceAsStream(path)
        ?: throw FileNotFoundException("Resource not found: $path")

      stream.bufferedReader().use { it.readText() }
    }

  override suspend fun readBytes(path: String): ByteArray =
    withContext(Dispatchers.IO) {
      val stream = Thread.currentThread()
        .contextClassLoader
        .getResourceAsStream(path)
        ?: throw FileNotFoundException("Resource not found: $path")

      stream.readBytes()
    }
}