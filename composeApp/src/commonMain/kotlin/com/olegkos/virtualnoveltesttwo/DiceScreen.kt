package com.olegkos.virtualnoveltesttwo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import virtualnoveltesttwo.composeapp.generated.resources.*

@Composable
fun DiceScreen(
  name: String,
  sides: Int,
  result: Int?,
  modifier: Int,
  difficulty: Int,
  onRoll: () -> Unit,
  onContinue: () -> Unit
) {

  var isRolling by remember { mutableStateOf(false) }
  var rotation by remember { mutableStateOf(0f) }
  var showResult by remember { mutableStateOf(false) }

  /**
   * 🎲 Dice spin animation
   */
  LaunchedEffect(isRolling) {
    if (!isRolling) return@LaunchedEffect

    var speed = 70f

    repeat(40) {
      rotation += speed
      speed *= 0.92f // плавное замедление
      delay(16)
    }

    onRoll()        // engine бросает кубик
    showResult = true
    isRolling = false
  }

  Column(horizontalAlignment = Alignment.CenterHorizontally) {

    Text("Проверка: $name d$sides")

    Spacer(Modifier.height(24.dp))

    val image =
      if (result == null)
        Res.drawable.d20
      else
        diceImageOrDefault(result)

    Image(
      painter = painterResource(image),
      contentDescription = null,
      modifier = Modifier
        .size(160.dp)
        .graphicsLayer {
          rotationZ = if (result == null) rotation else 0f
        }
    )

    Spacer(Modifier.height(24.dp))

    when {

      result == null && !isRolling -> {
        Button(onClick = {
          showResult = false
          isRolling = true
        }) {
          Text("Бросить")
        }
      }

      result != null && showResult -> {

        val total = result + modifier

        Text("Бросок: $result")
        Text("Модификатор: $modifier")
        Text("Итого: $total / $difficulty")

        Spacer(Modifier.height(16.dp))

        Button(onClick = onContinue) {
          Text("Продолжить")
        }
      }
    }
  }
}
fun diceImageOrDefault(value: Int): DrawableResource =
  when (value) {
    20 -> Res.drawable.d20
    18 -> Res.drawable.d18
    14 -> Res.drawable.d14
    12 -> Res.drawable.d12
    10 -> Res.drawable.d10
    4 -> Res.drawable.d4
    else -> Res.drawable.d20
  }