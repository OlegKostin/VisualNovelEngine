package com.olegkos.save.metaStorage

import com.olegkos.save.conventors.SaveJson
import kotlinx.serialization.Serializable
import java.io.File
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class MetaStorage {

  private val file = File(
    System.getProperty("user.home"),
    ".virtualNovelMeta.json"
  )

  private val SECRET = "my_super_secret_key_123"

  @Serializable
  data class SecureMeta(
    val data: MetaState,
    val signature: String
  )

  fun save(state: MetaState) {
    val json = SaveJson.encodeToString(MetaState.serializer(), state)

    val signature = sign(json)

    val secure = SecureMeta(
      data = state,
      signature = signature
    )

    val finalJson = SaveJson.encodeToString(
      SecureMeta.serializer(),
      secure
    )

    file.writeText(finalJson)
  }

  fun load(): MetaState {
    if (!file.exists()) return MetaState(emptyList())

    val json = file.readText()

    return try {
      val secure = SaveJson.decodeFromString(
        SecureMeta.serializer(),
        json
      )

      val dataJson = SaveJson.encodeToString(
        MetaState.serializer(),
        secure.data
      )

      val expected = sign(dataJson)
      println("SIGN CHECK START")
      println("EXPECTED: $expected")
      println("ACTUAL: ${secure.signature}")
      if (expected != secure.signature) {
        println("SAVE FILE TAMPERED!")
        MetaState(emptyList())
      } else {
        secure.data
      }

    } catch (e: Exception) {
      println("SAVE LOAD ERROR: ${e.message}")
      MetaState(emptyList())
    }
  }

  private fun sign(data: String): String {
    val mac = Mac.getInstance("HmacSHA256")
    val key = SecretKeySpec(SECRET.toByteArray(), "HmacSHA256")
    mac.init(key)
    val raw = mac.doFinal(data.toByteArray())
    return Base64.getEncoder().encodeToString(raw)
  }
}