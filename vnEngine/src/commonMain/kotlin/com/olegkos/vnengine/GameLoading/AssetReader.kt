package com.olegkos.vnengine.GameLoading

interface AssetReader {

  suspend fun readText(path: String): String

  suspend fun readBytes(path: String): ByteArray
}