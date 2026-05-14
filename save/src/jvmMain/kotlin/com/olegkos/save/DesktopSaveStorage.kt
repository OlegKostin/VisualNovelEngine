package com.olegkos.save

import java.io.File

class DesktopSaveStorage : SaveStorage {

  private val dir = File(
    System.getProperty("user.home"),
    ".virtualNovelSaves"
  ).apply { mkdirs() }

  override fun save(slot: String, data: String) {
    val file = File(dir, "$slot.json")
    println("💾 SAVE FILE PATH: ${file.absolutePath}")
    println("💾 SAVE DATA: $data")

    file.writeText(data)
  }

  override fun load(slot: String): String? {
    val file = File(dir, "$slot.json")

    println("📂 LOAD FILE PATH: ${file.absolutePath}")
    println("📂 EXISTS: ${file.exists()}")

    return if (file.exists()) {
      val text = file.readText()
      println("📂 LOADED DATA: $text")
      text
    } else {
      null
    }
  }

  override fun list(): List<String> =
    dir.listFiles()
      ?.filter { it.name.endsWith(".json", ignoreCase = true) }
      ?.map { it.nameWithoutExtension }
      ?: emptyList()

  override fun delete(slot: String) {
    File(dir, "$slot.json").delete()
    File(dir, "$slot.png").delete()
  }

  override fun savePreviewPng(slot: String, pngBytes: ByteArray) {
    File(dir, "$slot.png").writeBytes(pngBytes)
  }

  override fun loadPreviewPng(slot: String): ByteArray? {
    val file = File(dir, "$slot.png")
    return if (file.exists()) file.readBytes() else null
  }

  override fun deletePreviewPng(slot: String) {
    File(dir, "$slot.png").delete()
  }
}