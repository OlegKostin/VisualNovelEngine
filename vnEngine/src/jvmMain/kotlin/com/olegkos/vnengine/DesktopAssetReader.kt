package com.olegkos.vnengine

import com.olegkos.vnengine.GameLoading.AssetReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DesktopAssetReader : AssetReader {

  override suspend fun readText(path: String) =
    withContext(Dispatchers.IO) {
      File(path).readText()
    }

  override suspend fun readBytes(path: String) =
    withContext(Dispatchers.IO) {
      File(path).readBytes()
    }
}