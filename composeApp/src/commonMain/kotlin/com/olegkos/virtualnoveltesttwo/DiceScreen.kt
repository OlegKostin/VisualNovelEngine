package com.olegkos.virtualnoveltesttwo


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import virtualnoveltesttwo.composeapp.generated.resources.Res
import virtualnoveltesttwo.composeapp.generated.resources.d10
import virtualnoveltesttwo.composeapp.generated.resources.d12
import virtualnoveltesttwo.composeapp.generated.resources.d14
import virtualnoveltesttwo.composeapp.generated.resources.d18
import virtualnoveltesttwo.composeapp.generated.resources.d20
import virtualnoveltesttwo.composeapp.generated.resources.d4
import virtualnoveltesttwo.composeapp.generated.resources.d8

@Composable
fun DiceScreen(
  name: String,
  sides: Int,
  result: Int?,
  onRoll: () -> Unit,
  onContinue: () -> Unit
) {

  val allowed = listOf(20, 14, 4, 18, 12, 10) // реально существующие грани

  var isRolling by remember { mutableStateOf(false) }
  var currentFace by remember { mutableStateOf(allowed.first()) }

  LaunchedEffect(isRolling) {
    if (isRolling) {
      val steps = 30
      for (i in 0 until steps) {
        // цикл по allowed, повторяем несколько раз
        val index = i % allowed.size
        currentFace = allowed[index]

        // замедляем к концу
        val delayMs = 20 + i * 10
        delay(delayMs.toLong())
      }

      isRolling = false
      onRoll() // engine выдаёт финальный результат
    }
  }

  Column(horizontalAlignment = Alignment.CenterHorizontally) {

    Text("Бросок: $name d$sides")
    Spacer(Modifier.height(24.dp))

    val faceToShow = result ?: currentFace

    val image = when (faceToShow) {
      20 -> Res.drawable.d20
      14 -> Res.drawable.d14
      4 -> Res.drawable.d4
      18 -> Res.drawable.d18
      12 -> Res.drawable.d12
      10 -> Res.drawable.d10
      else -> Res.drawable.d20
    }

    Image(
      painter = painterResource(image),
      contentDescription = null
    )

    Spacer(Modifier.height(24.dp))

    when {
      result == null && !isRolling -> {
        Button(onClick = { isRolling = true }) {
          Text("Бросить")
        }
      }
      result != null -> {
        Text("Результат: $result")
        Spacer(Modifier.height(16.dp))
        Button(onClick = onContinue) {
          Text("Продолжить")
        }
      }
    }
  }
}