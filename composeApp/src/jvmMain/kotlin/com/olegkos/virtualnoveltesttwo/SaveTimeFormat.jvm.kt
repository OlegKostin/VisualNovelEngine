package com.olegkos.virtualnoveltesttwo

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val formatter: DateTimeFormatter =
  DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault())

actual fun formatSaveDateTime(epochMs: Long): String =
  formatter.format(Instant.ofEpochMilli(epochMs))
