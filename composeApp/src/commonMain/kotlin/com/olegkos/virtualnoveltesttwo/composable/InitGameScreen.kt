package com.olegkos.virtualnoveltesttwo.composable

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.dp
import com.olegkos.virtualnoveltesttwo.mappers.StatType
import com.olegkos.vnengine.scene.SubClass
import org.jetbrains.compose.resources.painterResource
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
  val infinite = rememberInfiniteTransition(label = "pulse")

  val pulse by infinite.animateFloat(
    initialValue = 1f,
    targetValue = if (selectedClass != null && hoveredClassId == selectedClass?.id) 1.08f else 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )
  Column(
    Modifier
      .fillMaxSize()
      .background(Color(0xFFE9ECF3))
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {

    Text("Создание персонажа", style = MaterialTheme.typography.headlineMedium)

    Spacer(Modifier.height(16.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {

      OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Имя") }
      )

      Spacer(Modifier.width(12.dp))

      Button(
        onClick = { onConfirm(name, selectedClass) },
        enabled = name.isNotBlank() && selectedClass != null
      ) {
        Text("Начать")
      }
    }

    Spacer(Modifier.height(24.dp))

    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly) {

      classes.take(3).forEachIndexed { index, cls ->

        val isHovered = hoveredClassId == cls.id
        val isSelected = selectedClass?.id == cls.id

        val scale by animateFloatAsState(
          targetValue = if (isSelected) 1.05f else if (isHovered) 1.02f else 1f,
          animationSpec = spring(dampingRatio = 0.75f),
          label = ""
        )

        val base = Color(0xFFC9D6F5)

        val selectedColor = when (index) {
          0 -> Color(0xFF6FAF73)
          1 -> Color(0xFF7B86C2)
          else -> Color(0xFFC07C7C)
        }

        val bg = if (isSelected) selectedColor else base

        val brush = Brush.verticalGradient(
          listOf(bg.copy(0.95f), bg.copy(0.65f))
        )

        Box(
          modifier = Modifier
            .weight(1f)
            .padding(8.dp)
            .graphicsLayer {
              scaleX = scale
              scaleY = scale
            }
            .shadow(10.dp, RoundedCornerShape(14.dp))
            .background(brush, RoundedCornerShape(14.dp))
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
            .clickable { selectedClass = cls }
        ) {

          Column(
            Modifier
              .fillMaxSize()
              .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
          ) {

            // =======================
            // TITLE
            // =======================
            Text(cls.name, style = MaterialTheme.typography.titleLarge)

            // =======================
            // DESCRIPTION (HARDCODE)
            // =======================
            Text(
              text = getDescription(cls.id),
              style = MaterialTheme.typography.bodyMedium
            )

            // =======================
            // STATS (2x3 GRID)
            // =======================
            StatsBlock(
              cls = cls,
              isSelected = isSelected,
              pulse = pulse,
              hovered = isHovered
            )

            // =======================
            // CARDS (HARDCODE)
            // =======================
            CardBlock()

            Text(
              if (isSelected) "Выбрано" else "Клик для выбора"
            )
          }
        }
      }
    }
  }
}

private fun getDescription(id: String): String {
  return when (id) {
    "hikki" -> "Воин — ближний бой и высокая выживаемость."
    "nerd" -> "Маг — дальний урон и контроль."
    "lucky" -> "Разбойник — крит и скорость."
    else -> "Класс без описания."
  }
}
@Composable
fun StatsBlock(
  cls: SubClass.GameClass,
  isSelected: Boolean,
  pulse: Float,
  hovered: Boolean
) {

  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

    cls.stats.entries.chunked(2).take(3).forEach { row ->

      Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {

        row.forEach { (key, value) ->

          val stat = StatType.fromKey(key)

          val scale by animateFloatAsState(
            targetValue = when {
              hovered -> 1.15f
              isSelected -> pulse
              else -> 1f
            },
            animationSpec = spring(dampingRatio = 0.7f),
            label = "statScale"
          )

          Row(
            modifier = Modifier
              .weight(1f)
              .graphicsLayer {
                scaleX = scale
                scaleY = scale
              },
            verticalAlignment = Alignment.CenterVertically
          ) {

            stat?.let {
              Image(
                painter = painterResource(it.image),
                contentDescription = key,
                modifier = Modifier.size(40.dp) // 👈 УВЕЛИЧИЛ ИКОНКУ
              )
            }

            Spacer(Modifier.width(8.dp))

            Text(
              text = value.toString(),
              style = MaterialTheme.typography.titleMedium
            )
          }
        }

        if (row.size == 1) {
          Spacer(Modifier.weight(1f))
        }
      }
    }
  }
}
private fun Modifier.cursorForHand(): Modifier {
  return pointerHoverIcon(
    PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
  )
}
@Composable
fun CardBlock() {

  Column(
    Modifier
      .fillMaxWidth()
      .padding(top = 8.dp)
  ) {

    Text(
      text = "Карты персонажа",
      style = MaterialTheme.typography.labelMedium
    )

    Spacer(Modifier.height(6.dp))

    Row(
      Modifier
        .fillMaxWidth()
        .height(60.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {

      repeat(3) { index ->

        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(
              color = Color(0xFF2E3A59),
              shape = RoundedCornerShape(10.dp)
            )
            .clickable { /* позже добавишь логику */ },
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Карта ${index + 1}",
            color = Color.White,
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    }
  }
}