package com.olegkos.virtualnoveltesttwo.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.dp
import com.olegkos.vnengine.scene.SubClass

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InitGameScreen(
  classes: List<SubClass.GameClass>,
  onConfirm: (String, SubClass.GameClass?) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var selectedClass by remember { mutableStateOf<SubClass.GameClass?>(null) }
  var hoveredClassId by remember { mutableStateOf<String?>(null) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {

    Text("Создание персонажа", style = MaterialTheme.typography.headlineMedium)
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
      value = name,
      onValueChange = { name = it },
      label = { Text("Имя персонажа") },
      modifier = Modifier.fillMaxWidth(0.5f)
    )

    Spacer(modifier = Modifier.height(32.dp))


    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly
    ) {
      classes.forEach { cls ->
        val isHovered = hoveredClassId == cls.id
        val isSelected = selectedClass?.id == cls.id

        Column(
          modifier = Modifier
            .width(200.dp)
            .height(250.dp)
            .border(
              width = 2.dp,
              color = if (isSelected) Color.Green else if (isHovered) Color.Yellow else Color.Gray,
              shape = RoundedCornerShape(12.dp)
            )
            .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
            .pointerMoveFilter(
              onEnter = { hoveredClassId = cls.id; false },
              onExit = { hoveredClassId = null; false }
            )
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = cls.name,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Статы
          Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            cls.stats.forEach { (key, value) ->
              Text("$key: $value", color = Color.LightGray)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = { selectedClass = cls },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isSelected) Color.Green else MaterialTheme.colorScheme.primary
            )
          ) {
            Text(if (isSelected) "Выбран" else "Выбрать")
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(32.dp))

    Button(
      onClick = { if (name.isNotBlank()) onConfirm(name, selectedClass) },
      enabled = name.isNotBlank() && selectedClass != null
    ) {
      Text("Начать игру")
    }
  }
}