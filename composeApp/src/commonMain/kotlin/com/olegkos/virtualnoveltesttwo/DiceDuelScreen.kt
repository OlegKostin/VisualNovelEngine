package com.olegkos.virtualnoveltesttwo

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olegkos.vnengine.engine.DiceDuelPhase
import com.olegkos.vnengine.engine.UiCard
import kotlin.math.abs
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

private fun formatSum(value: Float): String {
  val r = (value * 100f).toInt() / 100f
  return if (abs(r - r.toInt()) < 0.001f) r.toInt().toString() else r.toString()
}

@Composable
fun DiceDuelScreen(
  title: String,
  sides: Int,
  playerName: String?,
  playerModifier: Float,
  playerRoll: Int?,
  playerTotal: Float?,
  opponentName: String,
  @Suppress("UNUSED_PARAMETER")
  opponentImagePainter: BitmapPainter?,
  opponentModifier: Float,
  opponentRoll: Int?,
  opponentTotal: Float?,
  phase: DiceDuelPhase,
  cards: List<UiCard>,
  canUseCards: Boolean,
  resultText: String?,
  cardPainter: @Composable (String) -> BitmapPainter?,
  onRoll: () -> Unit,
  onApplyCards: (Float, List<String>) -> Unit,
  onContinue: () -> Unit
) {
  var selectedCards by remember { mutableStateOf(setOf<String>()) }
  var showCards by remember { mutableStateOf(false) }

  var rollAnimEpoch by remember { mutableIntStateOf(0) }
  var isRolling by remember { mutableStateOf(false) }
  var displayPlayer by remember { mutableIntStateOf(1) }
  var displayOpponent by remember { mutableIntStateOf(1) }

  LaunchedEffect(rollAnimEpoch, playerRoll, opponentRoll) {
    if (rollAnimEpoch == 0 || !isRolling) return@LaunchedEffect

    var delayMs = 40L
    repeat(15) {
      displayPlayer = (1..sides).random()
      displayOpponent = (1..sides).random()
      delay(delayMs)
      delayMs += 10
    }
    displayPlayer = playerRoll ?: displayPlayer
    displayOpponent = opponentRoll ?: displayOpponent
    isRolling = false
  }

  val bonus = cards
    .filter { it.id in selectedCards }
    .sumOf { it.value.toDouble() }
    .toFloat()

  val previewBonus =
    if (phase == DiceDuelPhase.PLAYER_MODIFY && showCards) bonus else 0f

  val playerSumText = when {
    playerTotal != null -> formatSum(playerTotal)
    playerRoll != null -> formatSum(playerRoll + playerModifier + previewBonus)
    else -> "-"
  }

  val opponentSumText = when {
    opponentTotal != null -> formatSum(opponentTotal)
    opponentRoll != null -> formatSum(opponentRoll + opponentModifier)
    else -> "-"
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val boxMaxWidth = maxWidth
    val boxMaxHeight = maxHeight

    val fontSize = (boxMaxHeight.value * 0.034f).sp
    val cardImageWidth = boxMaxWidth * 0.30f
    val cardImageHeight = cardImageWidth * 1.38f

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
      Text(
        text = title,
        fontSize = fontSize,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(Modifier.height(6.dp))

      Row(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(horizontal = 4.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Top
        ) {
          Text(playerName ?: "Игрок", fontSize = fontSize, textAlign = TextAlign.Center)
          Spacer(Modifier.height(4.dp))
          val pFace = when {
            isRolling -> displayPlayer
            playerRoll != null -> playerRoll
            else -> 1
          }
          Image(
            painter = painterResource(diceImage(pFace)),
            contentDescription = null,
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(1f)
          )
          Spacer(Modifier.height(4.dp))
          Text("Мод $playerModifier", fontSize = fontSize, textAlign = TextAlign.Center)
          Text("Результат $playerSumText", fontSize = fontSize, textAlign = TextAlign.Center)
        }

        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .then(
              if (phase == DiceDuelPhase.PLAYER_MODIFY && showCards) {
                Modifier.padding(horizontal = 0.dp)
              } else {
                Modifier.padding(horizontal = 4.dp)
              }
            ),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          when (phase) {
            DiceDuelPhase.PLAYER_ROLL -> {
              Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Button(
                  enabled = !isRolling,
                  onClick = {
                    onRoll()
                    isRolling = true
                    rollAnimEpoch++
                  },
                  modifier = Modifier.fillMaxWidth(0.95f)
                ) {
                  Text(if (isRolling) "Бросок..." else "Бросить", fontSize = fontSize)
                }
              }
            }

            DiceDuelPhase.PLAYER_MODIFY -> {
              when {
                showCards -> {
                  Column(
                    modifier = Modifier
                      .weight(1f)
                      .fillMaxWidth()
                      .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    LazyColumn(
                      modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                      verticalArrangement = Arrangement.spacedBy(12.dp),
                      horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                      items(cards, key = { it.id }) { card ->
                        val sel = card.id in selectedCards
                        val p = cardPainter(card.image)
                        Column(
                          modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                              selectedCards =
                                if (sel) selectedCards - card.id else selectedCards + card.id
                            },
                          horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                          p?.let {
                            Box {
                              Image(
                                painter = it,
                                contentDescription = null,
                                modifier = Modifier
                                  .width(cardImageWidth)
                                  .height(cardImageHeight),
                                contentScale = ContentScale.Fit
                              )
                              if (sel) {
                                Text(
                                  "✓",
                                  fontSize = (fontSize.value * 1.4f).sp,
                                  modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                )
                              }
                            }
                          }
                          Text(
                            "+${card.value}",
                            fontSize = fontSize,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                          )
                        }
                      }
                    }

                    Spacer(Modifier.height(8.dp))
                    Button(
                      onClick = {
                        onApplyCards(bonus, selectedCards.toList())
                        selectedCards = emptySet()
                        showCards = false
                      },
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp)
                    ) {
                      Text(
                        if (selectedCards.isEmpty()) "Применить" else "Применить (+$bonus)",
                        fontSize = fontSize
                      )
                    }
                  }
                }

                canUseCards -> {
                  Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    Button(
                      onClick = { onApplyCards(0f, emptyList()) },
                      modifier = Modifier.fillMaxWidth(0.95f)
                    ) {
                      Text("Без карт", fontSize = fontSize)
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                      onClick = {
                        selectedCards = emptySet()
                        showCards = true
                      },
                      modifier = Modifier.fillMaxWidth(0.95f)
                    ) {
                      Text("Карты", fontSize = fontSize)
                    }
                  }
                }

                else -> {
                  Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    Button(
                      onClick = { onApplyCards(0f, emptyList()) },
                      modifier = Modifier.fillMaxWidth(0.95f)
                    ) {
                      Text("Дальше", fontSize = fontSize)
                    }
                  }
                }
              }
            }

            DiceDuelPhase.RESOLVE -> {
              Column(
                modifier = Modifier
                  .fillMaxSize()
                  .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Text(
                  text = resultText.orEmpty(),
                  fontSize = (fontSize.value * 1.05f).sp,
                  textAlign = TextAlign.Center,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                )
                Spacer(Modifier.height(12.dp))
                Button(
                  onClick = onContinue,
                  modifier = Modifier.fillMaxWidth(0.95f)
                ) {
                  Text("Продолжить", fontSize = fontSize)
                }
              }
            }

            DiceDuelPhase.START,
            DiceDuelPhase.OPPONENT_ROLL -> Unit
          }
        }

        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(horizontal = 4.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Top
        ) {
          Text(
            text = opponentName,
            fontSize = fontSize,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(Modifier.height(4.dp))
          val oFace = when {
            isRolling -> displayOpponent
            opponentRoll != null -> opponentRoll
            else -> 1
          }
          Image(
            painter = painterResource(diceImage(oFace)),
            contentDescription = null,
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(1f)
          )
          Spacer(Modifier.height(4.dp))
          Text("Мод $opponentModifier", fontSize = fontSize, textAlign = TextAlign.Center)
          Text("Результат $opponentSumText", fontSize = fontSize, textAlign = TextAlign.Center)
        }
      }
    }
  }
}