package com.olegkos.save

interface SaveStorage {

  fun save(slot: String, data: String)

  fun load(slot: String): String?

  fun list(): List<String>

  fun delete(slot: String)

  fun savePreviewPng(slot: String, pngBytes: ByteArray)

  fun loadPreviewPng(slot: String): ByteArray?

  fun deletePreviewPng(slot: String)
}