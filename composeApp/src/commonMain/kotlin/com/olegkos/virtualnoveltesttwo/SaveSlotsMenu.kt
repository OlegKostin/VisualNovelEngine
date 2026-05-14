package com.olegkos.virtualnoveltesttwo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olegkos.virtualnovelapp.GameViewModel
import com.olegkos.virtualnoveltesttwo.theme.VnOutlinedButton

private enum class SaveLoadTab {
  SAVE,
  LOAD
}

@Composable
private fun SaveLoadModeToggleButton(
  label: String,
  selected: Boolean,
  onClick: () -> Unit
) {
  if (selected) {
    Button(
      onClick = onClick,
      colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF3D5A80),
        contentColor = Color.White
      )
    ) {
      Text(label)
    }
  } else {
    OutlinedButton(
      onClick = onClick,
      colors = ButtonDefaults.outlinedButtonColors(
        containerColor = Color.Transparent,
        contentColor = Color.LightGray,
        disabledContainerColor = Color.Transparent
      )
    ) {
      Text(label)
    }
  }
}

@Composable
fun SaveSlotsMenu(
  viewModel: GameViewModel,
  onRequestSaveWithScreenshot: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var tab by remember { mutableStateOf(SaveLoadTab.SAVE) }
  var refresh by remember { mutableStateOf(0) }
  var overwriteSlot by remember { mutableStateOf<String?>(null) }

  fun reload() {
    refresh++
  }

  val saves = remember(refresh) { viewModel.listSaves().toSet() }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xE6000000))
      .padding(12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      VnOutlinedButton(onClick = onDismiss) {
        Text("Закрыть")
      }

      Row(
        modifier = Modifier.padding(end = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        SaveLoadModeToggleButton(
          label = "Сохранить",
          selected = tab == SaveLoadTab.SAVE,
          onClick = { tab = SaveLoadTab.SAVE }
        )
        SaveLoadModeToggleButton(
          label = "Загрузить",
          selected = tab == SaveLoadTab.LOAD,
          onClick = { tab = SaveLoadTab.LOAD }
        )
      }
    }

    Spacer(Modifier.size(8.dp))

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        SaveSlotCell(
          slot = "slot1",
          exists = saves.contains("slot1"),
          slotRevision = refresh,
          tab = tab,
          viewModel = viewModel,
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
          onDelete = {
            viewModel.deleteSave("slot1")
            reload()
          },
          onSlotClick = {
            when (tab) {
              SaveLoadTab.SAVE -> {
                if (saves.contains("slot1")) {
                  overwriteSlot = "slot1"
                } else {
                  onRequestSaveWithScreenshot("slot1")
                }
              }

              SaveLoadTab.LOAD -> {
                if (saves.contains("slot1")) {
                  viewModel.loadSave("slot1")
                  onDismiss()
                }
              }
            }
          }
        )
        SaveSlotCell(
          slot = "slot2",
          exists = saves.contains("slot2"),
          slotRevision = refresh,
          tab = tab,
          viewModel = viewModel,
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
          onDelete = {
            viewModel.deleteSave("slot2")
            reload()
          },
          onSlotClick = {
            when (tab) {
              SaveLoadTab.SAVE -> {
                if (saves.contains("slot2")) {
                  overwriteSlot = "slot2"
                } else {
                  onRequestSaveWithScreenshot("slot2")
                }
              }

              SaveLoadTab.LOAD -> {
                if (saves.contains("slot2")) {
                  viewModel.loadSave("slot2")
                  onDismiss()
                }
              }
            }
          }
        )
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        SaveSlotCell(
          slot = "slot3",
          exists = saves.contains("slot3"),
          slotRevision = refresh,
          tab = tab,
          viewModel = viewModel,
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
          onDelete = {
            viewModel.deleteSave("slot3")
            reload()
          },
          onSlotClick = {
            when (tab) {
              SaveLoadTab.SAVE -> {
                if (saves.contains("slot3")) {
                  overwriteSlot = "slot3"
                } else {
                  onRequestSaveWithScreenshot("slot3")
                }
              }

              SaveLoadTab.LOAD -> {
                if (saves.contains("slot3")) {
                  viewModel.loadSave("slot3")
                  onDismiss()
                }
              }
            }
          }
        )
        SaveSlotCell(
          slot = "slot4",
          exists = saves.contains("slot4"),
          slotRevision = refresh,
          tab = tab,
          viewModel = viewModel,
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
          onDelete = {
            viewModel.deleteSave("slot4")
            reload()
          },
          onSlotClick = {
            when (tab) {
              SaveLoadTab.SAVE -> {
                if (saves.contains("slot4")) {
                  overwriteSlot = "slot4"
                } else {
                  onRequestSaveWithScreenshot("slot4")
                }
              }

              SaveLoadTab.LOAD -> {
                if (saves.contains("slot4")) {
                  viewModel.loadSave("slot4")
                  onDismiss()
                }
              }
            }
          }
        )
      }
    }
  }

  overwriteSlot?.let { slot ->
    AlertDialog(
      onDismissRequest = { overwriteSlot = null },
      title = { Text("Перезаписать сохранение?") },
      text = { Text("Слот ${slot.removePrefix("slot")} уже занят. Сделать новый снимок и перезаписать?") },
      confirmButton = {
        TextButton(
          onClick = {
            overwriteSlot = null
            onRequestSaveWithScreenshot(slot)
          }
        ) {
          Text("Перезаписать")
        }
      },
      dismissButton = {
        TextButton(onClick = { overwriteSlot = null }) {
          Text("Отмена")
        }
      }
    )
  }
}

@Composable
private fun SaveSlotCell(
  slot: String,
  exists: Boolean,
  slotRevision: Int,
  tab: SaveLoadTab,
  viewModel: GameViewModel,
  modifier: Modifier = Modifier,
  onDelete: () -> Unit,
  onSlotClick: () -> Unit
) {
  val pngBytes = remember(exists, slot, slotRevision) {
    if (exists) viewModel.savePreviewPng(slot) else null
  }
  val ts = remember(exists, slot, slotRevision) {
    if (exists) viewModel.saveTimestampMillis(slot) else null
  }

  var painter by remember { mutableStateOf<BitmapPainter?>(null) }
  LaunchedEffect(pngBytes) {
    painter =
      if (pngBytes != null && pngBytes.isNotEmpty()) {
        runCatching {
          BitmapPainter(loadImageBitmap(pngBytes.inputStream()))
        }.getOrNull()
      } else {
        null
      }
  }

  val canClickMain =
    when (tab) {
      SaveLoadTab.SAVE -> true
      SaveLoadTab.LOAD -> exists
    }

  Box(
    modifier = modifier
      .border(1.dp, Color(0xFF666666))
      .background(Color(0xFF222222))
      .clickable(enabled = canClickMain) { onSlotClick() }
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(bottom = 28.dp)
    ) {
      if (painter != null) {
        Image(
          painter = painter!!,
          contentDescription = null,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )
      } else {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Пусто",
            color = Color.Gray
          )
        }
      }
    }

    if (exists) {
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(6.dp)
          .size(36.dp)
          .clickable {
            onDelete()
          },
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "✕",
          color = Color.Red,
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    ts?.let { t ->
      Text(
        text = formatSaveDateTime(t),
        color = Color.White,
        fontSize = 12.sp,
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .background(Color(0xCC000000))
          .fillMaxWidth()
          .padding(vertical = 4.dp, horizontal = 6.dp),
        maxLines = 1
      )
    }
  }
}
