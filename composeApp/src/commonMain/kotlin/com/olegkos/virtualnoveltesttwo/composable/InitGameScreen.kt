package com.olegkos.virtualnoveltesttwo.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
      .background(Color(0xFFE9ECF3)) // мягкий светлый фон
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {

    Text(
      "Создание персонажа",
      style = MaterialTheme.typography.headlineMedium,
      color = Color(0xFF1A1A1A)
    )

    Spacer(modifier = Modifier.height(20.dp))

    // INPUT + BUTTON по центру
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {

      OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Имя персонажа") },
        modifier = Modifier.width(300.dp)
      )

      Spacer(modifier = Modifier.width(12.dp))

      Button(
        onClick = { if (name.isNotBlank()) onConfirm(name, selectedClass) },
        enabled = name.isNotBlank() && selectedClass != null,
        modifier = Modifier.height(56.dp)
      ) {
        Text("Начать игру")
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // 3 КЛАССА НА ЭКРАН
    Row(
      modifier = Modifier.fillMaxSize(),
      horizontalArrangement = Arrangement.SpaceEvenly
    ) {
      classes.take(3).forEachIndexed { index, cls ->

        val isHovered = hoveredClassId == cls.id
        val isSelected = selectedClass?.id == cls.id

        val baseBg = Color(0xFFC9D6F5)

        val hoverBg = when (index) {
          0 -> Color(0xFFBFE8C1)
          1 -> Color(0xFFBFC7FF)
          2 -> Color(0xFFFFC1C1)
          else -> baseBg
        }

        val selectedBg = when (index) {
          0 -> Color(0xFF7AD67A)
          1 -> Color(0xFF7A8CFF)
          2 -> Color(0xFFFF7A7A)
          else -> baseBg
        }

        val backgroundColor = when {
          isSelected -> selectedBg
          isHovered -> hoverBg
          else -> baseBg
        }

        val borderColor = when {
          isSelected -> Color(0xFF2E7D32)
          isHovered -> when (index) {
            0 -> Color(0xFF66FF66)
            1 -> Color(0xFF6699FF)
            2 -> Color(0xFFFF6666)
            else -> Color.Gray
          }
          else -> Color.Gray
        }

        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(8.dp)
            .border(
              width = 2.dp,
              color = borderColor,
              shape = RoundedCornerShape(12.dp)
            )
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .pointerMoveFilter(
              onEnter = {
                hoveredClassId = cls.id
                false
              },
              onExit = {
                hoveredClassId = null
                false
              }
            )
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.SpaceBetween
        ) {

          Text(
            text = cls.name,
            color = Color(0xFF1A1A1A),
            style = MaterialTheme.typography.headlineSmall
          )

          Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            cls.stats.forEach { (key, value) ->
              Text(
                "$key: $value",
                color = Color(0xFF2E2E2E)
              )
            }
          }

          Button(
            onClick = { selectedClass = cls },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isSelected) Color(0xFF4CAF50) else Color(0xFF607D8B)
            )
          ) {
            Text(if (isSelected) "Выбран" else "Выбрать")
          }
        }
      }
    }
  }
}