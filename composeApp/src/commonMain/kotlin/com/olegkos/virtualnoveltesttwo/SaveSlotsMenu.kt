package com.olegkos.virtualnoveltesttwo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.olegkos.virtualnovelapp.GameViewModel

@Composable
fun SaveSlotsMenu(viewModel: GameViewModel) {

  var saves by remember { mutableStateOf(viewModel.listSaves()) }

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {

    Text("Сохранения")

    (1..4).forEach { slotIndex ->

      val slot = "slot$slotIndex"
      val exists = saves.contains(slot)

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {

        Text(
          if (exists) "Slot $slotIndex (есть сейв)"
          else "Slot $slotIndex (пусто)"
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

          Button(
            onClick = { viewModel.saveGame(slot) }
          ) {
            Text("Save")
          }

          Button(
            enabled = exists,
            onClick = { viewModel.loadSave(slot) }
          ) {
            Text("Load")
          }
        }
      }
    }
  }
}