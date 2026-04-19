package com.olegkos.virtualnoveltesttwo.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
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

        // --- PERSONA EFFECT ---
        val scale by animateFloatAsState(
          targetValue = when {
            isSelected -> 1.06f
            isHovered -> 1.03f
            else -> 0.98f
          },
          animationSpec = spring(dampingRatio = 0.7f)
        )

        val offsetX by animateFloatAsState(
          targetValue = when {
            isSelected -> 0f
            selectedClass != null && index < classes.indexOf(selectedClass) -> -20f
            selectedClass != null && index > classes.indexOf(selectedClass) -> 20f
            else -> 0f
          }
        )

        val alpha by animateFloatAsState(
          targetValue = when {
            isSelected -> 1f
            selectedClass != null -> 0.7f
            else -> 1f
          }
        )

        val elevation = when {
          isSelected -> 20.dp
          isHovered -> 12.dp
          else -> 4.dp
        }

        val baseBg = Color(0xFFC9D6F5)

        val selectedBg = when (index) {
          0 -> Color(0xFF6FAF73)
          1 -> Color(0xFF7B86C2)
          2 -> Color(0xFFC07C7C)
          else -> baseBg
        }

        val bgColor = if (isSelected) selectedBg else baseBg

        val backgroundBrush = Brush.verticalGradient(
          colors = listOf(
            bgColor.copy(alpha = 0.95f),
            bgColor.copy(alpha = 0.6f)
          )
        )

        val glowOverlay = if (isSelected) {
          Brush.radialGradient(
            colors = listOf(
              Color.White.copy(alpha = 0.35f),
              Color.Transparent
            )
          )
        } else null

        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(8.dp)
            .graphicsLayer {
              scaleX = scale
              scaleY = scale
              translationX = offsetX
              this.alpha = alpha
            }
            .shadow(elevation, RoundedCornerShape(14.dp))
            .background(backgroundBrush, RoundedCornerShape(14.dp))
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

          // --- GLOW ---
          glowOverlay?.let {
            Box(
              modifier = Modifier
                .matchParentSize()
                .background(it, RoundedCornerShape(14.dp))
            )
          }

          Column(
            modifier = Modifier
              .fillMaxSize()
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

                val stat = StatType.fromKey(key)

                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                  stat?.let {
                    Image(
                      painter = painterResource(it.image),
                      contentDescription = key,
                      modifier = Modifier.size(20.dp)
                    )
                  }

                  Text(
                    text = value.toString(),
                    color = Color(0xFF2E2E2E)
                  )
                }
              }            }

            Text(
              text = if (isSelected) "Выбрано" else "Нажми",
              color = if (isSelected) Color(0xFF2E7D32) else Color(0xFF555555)
            )
          }
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