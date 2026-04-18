package com.olegkos.virtualnoveltesttwo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.olegkos.vnengine.engine.DicePhase
import com.olegkos.vnengine.engine.UiCard
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import virtualnoveltesttwo.composeapp.generated.resources.*

@Composable
fun DiceScreen(
  name: String,
  sides: Int,
  result: Int?,
  modifier: Float,
  difficulty: Int,
  cards: List<UiCard>,
  phase: DicePhase,
  onRoll: () -> Unit,
  onApplyCard: (Float, List<String>) -> Unit,
  onContinue: () -> Unit
) {

  var isRolling by remember { mutableStateOf(false) }
  var rollingValue by remember { mutableIntStateOf(1) }

  var selectedCards by remember { mutableStateOf(setOf<String>()) }
  var showCards by remember { mutableStateOf(false) }

  // 🎲 анимация броска
  LaunchedEffect(isRolling) {
    if (!isRolling) return@LaunchedEffect

    var delayMs = 40L

    repeat(15) {
      rollingValue = (1..sides).random()
      delay(delayMs)
      delayMs += 10
    }

    rollingValue = result ?: 1
    isRolling = false
  }

  Column(horizontalAlignment = Alignment.CenterHorizontally) {

    Text("Проверка: $name d$sides")

    Spacer(Modifier.height(24.dp))

    val valueToShow = when {
      isRolling -> rollingValue
      result != null -> result
      else -> 1
    }

    Image(
      painter = painterResource(diceImage(valueToShow)),
      contentDescription = null,
      modifier = Modifier.size(160.dp)
    )

    Spacer(Modifier.height(24.dp))

    // 👇 UI-фаза НЕ ломаем engine
    val uiPhase = when {
      showCards -> DicePhase.CARD_SELECTION
      else -> phase
    }

    when (uiPhase) {

      DicePhase.ROLL -> {
        Button(onClick = {
          onRoll()
          isRolling = true
        }) {
          Text("Бросить")
        }
      }

      DicePhase.RESULT -> {
        if (!isRolling) {

          Text("Бросок: $result")
          Text("Модификатор: $modifier")

          Spacer(Modifier.height(16.dp))

          Button(onClick = {
            onApplyCard(0f, emptyList())
          }) {
            Text("Без карт")
          }

          Spacer(Modifier.height(8.dp))

          Button(onClick = {
            // ❗ ТОЛЬКО UI переключение
            selectedCards = emptySet()
            showCards = true
          }) {
            Text("Использовать карты")
          }
        }
      }

      DicePhase.FINAL -> {

        val total = (result ?: 0) + modifier

        Text("Бросок: $result")
        Text("Итоговый модификатор: $modifier")
        Text("Итого: $total / $difficulty")

        Spacer(Modifier.height(16.dp))

        Button(onClick = onContinue) {
          Text("Продолжить")
        }
      }

      DicePhase.CARD_SELECTION -> {

        Column {

          Text("Выбери карты (+1 за каждую)")

          cards.forEach { card ->

            val isSelected = card.id in selectedCards

            Button(onClick = {
              selectedCards = if (isSelected) {
                selectedCards - card.id
              } else {
                selectedCards + card.id
              }
            }) {
              Text(if (isSelected) "✓ ${card.id}" else card.id)
            }
          }

          Spacer(Modifier.height(12.dp))

          Button(onClick = {
            val usedCards = selectedCards.toList()
            val bonus = usedCards.size * 1f

            onApplyCard(bonus, usedCards)

            selectedCards = emptySet()
            showCards = false
          }) {
            Text("Применить")
          }
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