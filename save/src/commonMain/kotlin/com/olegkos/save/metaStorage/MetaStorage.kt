package com.olegkos.save.metaStorage

import com.olegkos.save.conventors.SaveJson
import kotlinx.serialization.builtins.serializer
import java.io.File

class MetaStorage {

  private val file = File(
    System.getProperty("user.home"),
    ".virtualNovelMeta.json"
  )

  fun save(state: MetaState) {
    val json = SaveJson.encodeToString(MetaState.serializer(), state)
    file.writeText(json)
  }

  fun load(): MetaState {
    if (!file.exists()) return MetaState(emptyList())

    val json = file.readText()

    return SaveJson.decodeFromString(
      MetaState.serializer(),
      json
    )
  }
}