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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.olegkos.virtualnoveltesttwo.mappers.StatType
import com.olegkos.virtualnoveltesttwo.theme.VnOutlinedButton
import com.olegkos.vnengine.engine.variables.forStatPreview
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

  BoxWithConstraints(
    Modifier
      .fillMaxSize()
      .background(Color(0xFFE9ECF3))
      .padding(16.dp)
  ) {
    val statIconSize = maxWidth * 0.05f

    Column(
      Modifier.fillMaxSize(),
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

      VnOutlinedButton(
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

        val selectedColor = when (index) {
          0 -> Color(0xFF6FAF73)
          1 -> Color(0xFF7B86C2)
          else -> Color(0xFFC07C7C)
        }

        val bg = if (isSelected) selectedColor else Color(0xFFC9D6F5)

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
              transformOrigin = TransformOrigin(0.5f, 0.5f)
              clip = false
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
            .clickable {
              selectedClass = if (selectedClass?.id == cls.id) null else cls
            }
        ) {

          Column(
            Modifier
              .fillMaxSize()
              .padding(14.dp)
              .graphicsLayer { clip = false },
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
              statIconSize = statIconSize
            )

            // =======================
            // CARDS (HARDCODE)
            // =======================
            CardBlock()

            Text(
              if (isSelected) "Выбрано (клик снова — снять)" else "Клик для выбора"
            )
          }
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
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun StatsBlock(
  cls: SubClass.GameClass,
  statIconSize: Dp
) {
  var hoveredStatKey by remember { mutableStateOf<String?>(null) }

  val statPulseTransition = rememberInfiniteTransition(label = "statIconPulse")
  val statPulse by statPulseTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.08f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200),
      repeatMode = RepeatMode.Reverse
    ),
    label = "statPulseWave"
  )

  val tooltipMaxWidth = 168.dp
  val tooltipOffset = 6.dp

  val statRowGap = statIconSize * 0.35f + 6.dp
  val iconTextGap = statIconSize * 0.2f + 4.dp
  val iconTrackWidth = tooltipMaxWidth + tooltipOffset + statIconSize
  val cellMinWidth = iconTrackWidth + iconTextGap + 28.dp
  val cellMaxWidth = iconTrackWidth + iconTextGap + 120.dp

  Box(
    Modifier
      .fillMaxWidth()
      .padding(horizontal = 4.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

      cls.stats.entries.chunked(2).take(3).forEach { row ->

        Row(
          Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(statRowGap, Alignment.CenterHorizontally),
          verticalAlignment = Alignment.CenterVertically
        ) {

          row.forEachIndexed { colIndex, (key, value) ->

            val stat = StatType.fromKey(key)
            val statHovered = hoveredStatKey == key

            val pulseFactor = if (statHovered) statPulse else 1f

            val hoverScale by animateFloatAsState(
              targetValue = if (statHovered) 1.06f else 1f,
              animationSpec = spring(dampingRatio = 0.72f),
              label = "statHover"
            )

            val combinedScale = pulseFactor * hoverScale

            Column(
              modifier = Modifier
                .widthIn(min = cellMinWidth, max = cellMaxWidth)
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .graphicsLayer { clip = false },
              horizontalAlignment = Alignment.CenterHorizontally
            ) {

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {

                stat?.let {
                  Box(
                    Modifier
                      .width(iconTrackWidth)
                      .height(statIconSize)
                      .graphicsLayer { clip = false }
                      .pointerMoveFilter(
                        onEnter = {
                          hoveredStatKey = key
                          false
                        },
                        onExit = {
                          if (hoveredStatKey == key) hoveredStatKey = null
                          false
                        }
                      )
                      .cursorForHand()
                  ) {
                    Image(
                      painter = painterResource(it.image),
                      contentDescription = key,
                      modifier = Modifier
                        .align(
                          if (colIndex == 0) Alignment.CenterEnd else Alignment.CenterStart
                        )
                        .size(statIconSize)
                        .graphicsLayer {
                          scaleX = combinedScale
                          scaleY = combinedScale
                          transformOrigin = TransformOrigin(0.5f, 0.5f)
                          clip = false
                        }
                    )

                    if (statHovered) {
                      val hint = StatType.hoverHintForKey(key)
                      Surface(
                        color = Color(0xE6FFFFFF),
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 4.dp,
                        modifier = Modifier
                          .widthIn(max = tooltipMaxWidth)
                          .align(Alignment.CenterStart)
                          .offset(
                            x = if (colIndex == 0) 0.dp else statIconSize + tooltipOffset,
                            y = 0.dp
                          )
                      ) {
                        Text(
                          text = hint,
                          style = MaterialTheme.typography.bodySmall,
                          color = Color(0xFF2A3142),
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                      }
                    }
                  }
                  Spacer(Modifier.width(iconTextGap))
                }

                Text(
                  text = value.forStatPreview(),
                  style = MaterialTheme.typography.headlineSmall
                )
              }
            }
          }
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