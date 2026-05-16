package com.olegkos.virtualnoveltesttwo.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.olegkos.virtualnoveltesttwo.theme.VnOutlinedButton

@Composable
fun GameMenuHub(
  onSaveLoad: () -> Unit,
  onPlayerStats: () -> Unit,
  onDismiss: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xE6000000)),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth(0.85f)
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text(
        text = "Меню",
        style = MaterialTheme.typography.headlineSmall,
        color = Color.White
      )

      Spacer(Modifier.height(8.dp))

      VnOutlinedButton(
        onClick = onSaveLoad,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("Сохранение / загрузка")
      }

      VnOutlinedButton(
        onClick = onPlayerStats,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("Характеристики")
      }

      Spacer(Modifier.height(8.dp))

      VnOutlinedButton(onClick = onDismiss) {
        Text("Закрыть")
      }
    }
  }
}
