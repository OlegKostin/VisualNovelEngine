package com.olegkos.virtualnoveltesttwo.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.dp
import com.olegkos.vnengine.scene.SubClass
import java.awt.Cursor

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
      .background(Color(0xFFE9ECF3))
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {

    Text(
      "Создание персонажа",
      style = MaterialTheme.typography.headlineMedium,
      color = Color(0xFF1A1A1A)
    )

    Spacer(modifier = Modifier.height(20.dp))

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
        onClick = { onConfirm(name, selectedClass) },
        enabled = name.isNotBlank() && selectedClass != null,
        modifier = Modifier.height(56.dp)
      ) {
        Text("Начать игру")
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

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
          0 -> Color(0xFF6FAF73)
          1 -> Color(0xFF7B86C2)
          2 -> Color(0xFFC07C7C)
          else -> baseBg
        }

        val bgColor = when {
          isSelected -> selectedBg
          isHovered -> hoverBg
          else -> baseBg
        }

        val backgroundBrush = Brush.verticalGradient(
          colors = listOf(
            bgColor.copy(alpha = 1f),
            bgColor.copy(alpha = 0.2f)
          )
        )

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
              2.dp,
              borderColor,
              RoundedCornerShape(12.dp)
            )
            .background(backgroundBrush, RoundedCornerShape(12.dp)) // 🔥 ВОТ ТУТ ГРАДИЕНТ
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
            .cursorForHand()
            .clickable {
              selectedClass = cls
            }
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

          Text(
            text = if (isSelected) "Выбрано" else "Нажми для выбора",
            color = if (isSelected) Color(0xFF2E7D32) else Color(0xFF555555)
          )
        }
      }
    }
  }
}

private fun Modifier.cursorForHand(): Modifier {
  return this.pointerHoverIcon(
    PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
  )
}