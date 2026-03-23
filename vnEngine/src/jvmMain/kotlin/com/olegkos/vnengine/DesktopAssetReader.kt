package com.olegkos.vnengine

import com.olegkos.vnengine.GameLoading.AssetReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException

class DesktopAssetReader : AssetReader {

  override suspend fun readText(path: String): String {
    return this::class.java.classLoader
      .getResource(path)
      ?.readText()
      ?: throw FileNotFoundException("Resource not found: $path")
  }

  override suspend fun readBytes(path: String): ByteArray {
    return this::class.java.classLoader
      .getResourceAsStream(path)
      ?.readBytes()
      ?: throw FileNotFoundException("Resource not found: $path")
  }
}