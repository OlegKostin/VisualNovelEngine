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
  var showResult by remember { mutableStateOf(false) }
  var rollingValue by remember { mutableIntStateOf(1) }

  LaunchedEffect(isRolling) {

    if (!isRolling) return@LaunchedEffect

    onRoll()

    val final = result ?: (1..sides).random()

    var delayMs = 40L

    repeat(20) {
      rollingValue = (1..sides).random()
      delay(delayMs)
      delayMs += 10
    }

    val nearValues = listOf(
      (1..sides).random(),
      (1..sides).random(),
      final
    )

    for (v in nearValues) {
      rollingValue = v
      delay(delayMs)
      delayMs += 40
    }

    rollingValue = final
    showResult = true
    isRolling = false
  }

  Column(horizontalAlignment = Alignment.CenterHorizontally) {

    Text("Проверка: $name d$sides")

    Spacer(Modifier.height(24.dp))

    val valueToShow =
      when {
        isRolling -> rollingValue
        result != null -> result
        else -> 20
      }

    Image(
      painter = painterResource(diceImage(valueToShow)),
      contentDescription = null,
      modifier = Modifier.size(160.dp)
    )

    Spacer(Modifier.height(24.dp))

    when {

      result == null && !isRolling -> {
        Button(
          onClick = {
            showResult = false
            isRolling = true
          }
        ) {
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

fun diceImage(value: Int): DrawableResource =
  when (value) {
    1 -> Res.drawable.d1
    2 -> Res.drawable.d2
    3 -> Res.drawable.d3
    4 -> Res.drawable.d4
    5 -> Res.drawable.d5
    6 -> Res.drawable.d6
    7 -> Res.drawable.d7
    8 -> Res.drawable.d8
    9 -> Res.drawable.d9
    10 -> Res.drawable.d10
    11 -> Res.drawable.d11
    12 -> Res.drawable.d12
    13 -> Res.drawable.d13
    14 -> Res.drawable.d14
    15 -> Res.drawable.d15
    16 -> Res.drawable.d16
    17 -> Res.drawable.d17
    18 -> Res.drawable.d18
    19 -> Res.drawable.d19
    20 -> Res.drawable.d20
    else -> Res.drawable.d20
  }