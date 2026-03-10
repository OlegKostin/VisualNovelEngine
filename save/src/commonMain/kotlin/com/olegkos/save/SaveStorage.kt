package com.olegkos.save

interface SaveStorage {

  fun save(slot: String, data: String)

  fun load(slot: String): String?

  fun list(): List<String>

  fun delete(slot: String)
}