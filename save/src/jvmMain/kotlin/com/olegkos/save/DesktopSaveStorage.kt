package com.olegkos.save

import java.io.File

class DesktopSaveStorage : SaveStorage {

  private val dir = File(
    System.getProperty("user.home"),
    ".virtualNovelSaves"
  ).apply { mkdirs() }

  override fun save(slot: String, data: String) {
    File(dir, "$slot.json").writeText(data)
  }

  override fun load(slot: String): String? {

    val file = File(dir, "$slot.json")

    return if (file.exists())
      file.readText()
    else
      null
  }

  override fun list(): List<String> =
    dir.listFiles()
      ?.map { it.nameWithoutExtension }
      ?: emptyList()

  override fun delete(slot: String) {
    File(dir, "$slot.json").delete()
  }
}