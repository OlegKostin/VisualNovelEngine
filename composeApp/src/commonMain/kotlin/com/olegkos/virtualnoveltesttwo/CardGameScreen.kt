package com.olegkos.virtualnoveltesttwo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olegkos.virtualnovelapp.GameViewModel
import com.olegkos.virtualnoveltesttwo.composable.horizontalWheelScroll
import com.olegkos.virtualnoveltesttwo.mappers.StatType
import com.olegkos.virtualnoveltesttwo.theme.SkikoSafeText
import com.olegkos.virtualnoveltesttwo.theme.VnOutlinedButton
import com.olegkos.vnengine.engine.EngineOutput
import org.jetbrains.compose.resources.painterResource
import com.olegkos.vnengine.engine.cardgame.CardGamePhase
import com.olegkos.vnengine.engine.cardgame.ClashResolution

private val Bg = Color(0xE610141C)
private val Accent = Color(0xFFBBDEFB)
private val Muted = Color(0xCCFFFFFF)

@Composable
fun CardGameScreen(
  output: EngineOutput.ShowCardGame,
  viewModel: GameViewModel,
  cardPainter: @Composable (String) -> BitmapPainter?,
) {
  var selectedMeta by remember(output.gameId) { mutableStateOf(setOf<String>()) }
  var selectedPool by remember(output.gameId) { mutableStateOf(setOf<String>()) }
  var selectedClash by remember(output.gameId) { mutableStateOf(setOf<String>()) }

  LaunchedEffect(output.phase) {
    when (output.phase) {
      CardGamePhase.DRAFT -> {
        selectedMeta = emptySet()
        selectedPool = emptySet()
      }
      CardGamePhase.SELECT_CLASH -> selectedClash = emptySet()
      else -> Unit
    }
  }

  when (output.phase) {
    CardGamePhase.VN_AFTER_CLASH -> CardGameVnOverlay(
      speaker = output.vnSpeaker,
      text = output.vnText.orEmpty(),
      onNext = { viewModel.cardGameVnNext() }
    )

    CardGamePhase.RESULT -> Box(
      Modifier.fillMaxSize().background(Bg).padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        output.resultText?.let { result ->
          SkikoSafeText(
            text = result,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
          )
        }
        CardGameAction("Продолжить (в сцену)") { viewModel.cardGameFinish() }
      }
    }

    else -> BoxWithConstraints(Modifier.fillMaxSize()) {
      val cardW = maxOf(maxWidth * 0.14f, 72.dp)
      val cardH = cardW * 1.38f
      val toneLabel = StatType.fromKey(output.battleTone)?.title ?: output.battleTone
      val showBattleTone = output.phase != CardGamePhase.DRAFT

      Column(
        Modifier.fillMaxSize().background(Bg).padding(12.dp).verticalScroll(rememberScrollState())
      ) {
        SkikoSafeText(output.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        if (showBattleTone) {
          SkikoSafeText(
            "Тон боя: $toneLabel",
            fontSize = 14.sp,
            color = Accent,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
          )
        } else {
          Spacer(Modifier.height(8.dp))
        }
        output.playerName?.let { SkikoSafeText("Игрок: $it", fontSize = 13.sp, color = Muted) }
        SkikoSafeText(
          "Противник: ${output.opponentName}",
          fontSize = 13.sp,
          color = Muted,
          modifier = Modifier.padding(bottom = 8.dp)
        )

        when (output.phase) {
          CardGamePhase.DRAFT -> {
            val handSize = output.draftHandSize
            val metaCap = minOf(output.draftMetaMax, handSize - selectedPool.size)
            val poolCap = handSize - selectedMeta.size
            DraftSection(
              "Meta — до $metaCap (сверху)",
              output.metaCards,
              selectedMeta,
              metaCap,
              cardW,
              cardH,
              cardPainter
            ) { selectedMeta = it }
            Spacer(Modifier.height(16.dp))
            DraftSection(
              "Колода — до $poolCap из ${output.offerCards.size} (снизу)",
              output.offerCards,
              selectedPool,
              poolCap,
              cardW,
              cardH,
              cardPainter
            ) { selectedPool = it }
            Spacer(Modifier.height(12.dp))
            CardGameAction(
              text = "Собрать руку (${selectedMeta.size + selectedPool.size}/$handSize)",
              enabled = selectedMeta.size + selectedPool.size == handSize
            ) {
              viewModel.cardGameConfirmDraft(selectedMeta.toList(), selectedPool.toList())
            }
          }

          CardGamePhase.SELECT_CLASH -> {
            SkikoSafeText("Выложите 3 карты для clash", color = Color.White)
            SelectableCardRow(output.hand, selectedClash, cardW, cardH, cardPainter, max = 3) { selectedClash = it }
            Spacer(Modifier.height(12.dp))
            CardGameAction("К бою", enabled = selectedClash.size == 3) {
              viewModel.cardGameConfirmClash(selectedClash.toList())
            }
          }

          CardGamePhase.BATTLE_REVEAL -> {
            LabeledCardRow("Ваши карты", output.playerPlayed, cardW, cardH, cardPainter)
            Spacer(Modifier.height(12.dp))
            LabeledCardRow("Карты противника", output.enemyPlayed, cardW, cardH, cardPainter)
            Spacer(Modifier.height(12.dp))
            CardGameAction("Далее — разбор боя") { viewModel.cardGameBattleContinue() }
          }

          CardGamePhase.SCORE_BREAKDOWN -> output.clashResolution?.let { resolution ->
            ScoreBreakdown(output, resolution, cardW, cardH, cardPainter) { viewModel.cardGameBreakdownNext() }
          }

          else -> Unit
        }
      }
    }
  }
}

@Composable
private fun DraftSection(
  title: String,
  cards: List<EngineOutput.CardGameUiCard>,
  selected: Set<String>,
  maxPick: Int,
  cardW: Dp,
  cardH: Dp,
  cardPainter: @Composable (String) -> BitmapPainter?,
  onSelected: (Set<String>) -> Unit
) {
  SkikoSafeText(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 6.dp))
  SelectableCardRow(cards, selected, cardW, cardH, cardPainter, maxPick, onSelected)
}

@Composable
private fun SelectableCardRow(
  cards: List<EngineOutput.CardGameUiCard>,
  selected: Set<String>,
  cardW: Dp,
  cardH: Dp,
  cardPainter: @Composable (String) -> BitmapPainter?,
  max: Int,
  onSelected: (Set<String>) -> Unit
) {
  CardRow(cards, selected, cardW, cardH, cardPainter, selectable = true) { id ->
    onSelected(toggleSelection(selected, id, max))
  }
}

@Composable
private fun LabeledCardRow(
  label: String,
  cards: List<EngineOutput.CardGameUiCard>,
  cardW: Dp,
  cardH: Dp,
  cardPainter: @Composable (String) -> BitmapPainter?,
  showEffective: Boolean = false
) {
  SkikoSafeText(label, color = Color.White, modifier = Modifier.padding(bottom = 4.dp))
  CardRow(cards, emptySet(), cardW, cardH, cardPainter, showEffective = showEffective)
}

@Composable
private fun CardGameVnOverlay(speaker: String?, text: String, onNext: () -> Unit) {
  Box(
    Modifier.fillMaxSize().background(Bg).clickable(onClick = onNext).padding(20.dp),
    contentAlignment = Alignment.BottomStart
  ) {
    Column(
      Modifier.fillMaxWidth().background(Color(0xCCBBDEFB), RoundedCornerShape(12.dp)).padding(16.dp)
    ) {
      speaker?.let {
        SkikoSafeText(it, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111111), modifier = Modifier.padding(bottom = 8.dp))
      }
      SkikoSafeText(text.ifEmpty { "…" }, fontSize = 16.sp, color = Color(0xFF111111), modifier = Modifier.padding(bottom = 8.dp))
      SkikoSafeText("Нажмите, чтобы продолжить", fontSize = 12.sp, color = Color(0xFF333333))
    }
  }
}

@Composable
private fun CardGameAction(text: String, enabled: Boolean = true, onClick: () -> Unit) {
  VnOutlinedButton(onClick = onClick, enabled = enabled, modifier = Modifier.padding(top = 4.dp)) {
    SkikoSafeText(text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
  }
}

@Composable
private fun ScoreBreakdown(
  output: EngineOutput.ShowCardGame,
  resolution: ClashResolution,
  cardW: Dp,
  cardH: Dp,
  cardPainter: @Composable (String) -> BitmapPainter?,
  onNext: () -> Unit
) {
  val side = output.breakdownSide
  val step = output.breakdownStepIndex

  when (side) {
    "PLAYER", "ENEMY" -> {
      val playerSide = side == "PLAYER"
      LabeledCardRow(
        if (playerSide) "Ваши карты" else "Карты противника",
        if (playerSide) output.playerPlayed else output.enemyPlayed,
        cardW,
        cardH,
        cardPainter,
        showEffective = true
      )
      if (playerSide) {
        SkikoSafeText(
          "Контр: сила -> удача -> мудрость -> воля -> сила. Подсвеченные карты обнулены.",
          fontSize = 12.sp,
          color = Color(0x99FFFFFF),
          modifier = Modifier.padding(vertical = 4.dp)
        )
      }
      val steps = if (playerSide) resolution.playerScore.steps else resolution.enemyScore.steps
      steps.getOrNull(step)?.let { s ->
        SkikoSafeText("${s.label}: ${s.detail} -> ${s.runningTotal}", fontSize = 15.sp, color = Accent, modifier = Modifier.padding(vertical = 8.dp))
      }
    }
    "COMPARE" -> SkikoSafeText(
      "Итог: вы ${resolution.playerTotal} — враг ${resolution.enemyTotal}",
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
      color = Color.White,
      modifier = Modifier.padding(vertical = 12.dp)
    )
  }

  CardGameAction(if (side == "COMPARE") "Далее — текст после боя" else "Далее", onClick = onNext)
}

@Composable
private fun CardRow(
  cards: List<EngineOutput.CardGameUiCard>,
  selected: Set<String>,
  cardW: Dp,
  cardH: Dp,
  cardPainter: @Composable (String) -> BitmapPainter?,
  selectable: Boolean = false,
  showEffective: Boolean = false,
  onToggle: (String) -> Unit = {}
) {
  val scrollState = rememberScrollState()
  val footerH = if (selectable) 26.dp else 22.dp
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(cardH + footerH)
      .padding(vertical = 8.dp)
      .horizontalWheelScroll(scrollState)
      .horizontalScroll(scrollState),
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalAlignment = Alignment.Top
  ) {
    cards.forEach { card ->
      GameCardTile(
        card = card,
        selected = card.id in selected,
        cardW = cardW,
        cardH = cardH,
        painter = if (card.faceDown) null else cardPainter(card.image),
        onClick = { if (selectable) onToggle(card.id) },
        showEffective = showEffective,
        showTagAndValue = selectable
      )
    }
    Spacer(Modifier.width(16.dp))
  }
}

@Composable
private fun GameCardTile(
  card: EngineOutput.CardGameUiCard,
  selected: Boolean,
  cardW: Dp,
  cardH: Dp,
  painter: BitmapPainter?,
  onClick: () -> Unit,
  showEffective: Boolean,
  showTagAndValue: Boolean = false
) {
  val borderColor = when {
    card.countered -> Color(0xFFFF5252)
    selected -> Color(0xFF64B5F6)
    else -> Color.Transparent
  }
  val shape = RoundedCornerShape(8.dp)

  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Box(
      Modifier
        .width(cardW)
        .height(cardH)
        .border(3.dp, borderColor, shape)
        .background(if (card.faceDown) Color(0xFF1A237E) else Color(0xFF263238), shape)
        .clickable(onClick = onClick),
      contentAlignment = Alignment.Center
    ) {
      when {
        card.faceDown -> SkikoSafeText("?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        painter != null -> Image(
          painter = painter,
          contentDescription = null,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )
      }
      if (!card.faceDown) {
        CardTagBadge(tag = card.tag, iconSize = (cardW * 0.24f).coerceIn(18.dp, 26.dp))
      }
    }
    when {
      showEffective && card.effectiveValue != null -> SkikoSafeText(
        "→ ${card.effectiveValue}",
        fontSize = 12.sp,
        color = if (card.countered) Color(0xFFFF8A80) else Accent,
        modifier = Modifier.padding(top = 2.dp)
      )
      showTagAndValue && !card.faceDown -> SkikoSafeText(
        "${card.value}",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        modifier = Modifier.padding(top = 2.dp)
      )
      !card.faceDown -> SkikoSafeText("${card.value}", fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 2.dp))
    }
  }
}

/** Иконка тега на арте карты (левый верхний угол). */
@Composable
private fun BoxScope.CardTagBadge(tag: String, iconSize: Dp) {
  val stat = StatType.fromKey(tag)
  Box(
    Modifier
      .align(Alignment.TopStart)
      .padding(4.dp)
      .background(Color(0xD010141C), RoundedCornerShape(6.dp))
      .padding(horizontal = 4.dp, vertical = 3.dp),
    contentAlignment = Alignment.Center
  ) {
    if (stat != null) {
      Image(
        painter = painterResource(stat.image),
        contentDescription = stat.title,
        modifier = Modifier.size(iconSize)
      )
    } else {
      SkikoSafeText(
        text = tag.removePrefix("opt_").take(3),
        fontSize = 10.sp,
        color = Accent
      )
    }
  }
}

private fun toggleSelection(current: Set<String>, id: String, max: Int): Set<String> = when {
  id in current -> current - id
  current.size < max -> current + id
  else -> current
}
