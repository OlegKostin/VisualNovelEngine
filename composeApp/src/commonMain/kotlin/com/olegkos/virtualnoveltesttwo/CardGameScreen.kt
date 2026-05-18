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
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.olegkos.vnengine.engine.cardgame.CardGamePhase
import com.olegkos.vnengine.engine.cardgame.ClashResolution
import org.jetbrains.compose.resources.painterResource

private val Bg = Color(0xE610141C)
private val Accent = Color(0xFFBBDEFB)
private val Muted = Color(0xCCFFFFFF)
private val PanelBg = Color(0xFF1A2438)

/** Кольцо контра: кто кого бьёт (основные теги). */
private val COUNTER_CHAIN = listOf(
  StatType.STR to StatType.LUCK,
  StatType.LUCK to StatType.WIS,
  StatType.WIS to StatType.WILL,
  StatType.WILL to StatType.STR,
)

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
      CardGamePhase.BATTLE_REVEAL,
      CardGamePhase.SCORE_BREAKDOWN,
      CardGamePhase.RESULT -> Unit
    }
  }

  when (output.phase) {
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

    CardGamePhase.DRAFT -> BoxWithConstraints(Modifier.fillMaxSize().background(Bg)) {
      val cardW = maxOf(maxWidth * 0.14f, 72.dp)
      val cardH = cardW * 1.38f
      Column(
        Modifier.fillMaxSize().padding(12.dp).verticalScroll(rememberScrollState())
      ) {
        CardGameHeader(output, showBattleTone = false)
        DraftScreen(
          output = output,
          selectedMeta = selectedMeta,
          selectedPool = selectedPool,
          cardW = cardW,
          cardH = cardH,
          cardPainter = cardPainter,
          onMetaChange = { selectedMeta = it },
          onPoolChange = { selectedPool = it },
          onConfirm = {
            viewModel.cardGameConfirmDraft(selectedMeta.toList(), selectedPool.toList())
          }
        )
      }
    }

    CardGamePhase.SELECT_CLASH,
    CardGamePhase.BATTLE_REVEAL,
    CardGamePhase.SCORE_BREAKDOWN -> CardGameMainRow(
      output = output,
      selectedClash = selectedClash,
      cardPainter = cardPainter,
      onClashChange = { selectedClash = it },
      onConfirmClash = { viewModel.cardGameConfirmClash(selectedClash.toList()) },
      onBattleContinue = { viewModel.cardGameBattleContinue() },
      onBreakdownNext = { viewModel.cardGameBreakdownNext() },
      onVnNext = { viewModel.cardGameVnNext() },
    )
  }
}

/** Экраны 2–3: слева карты, справа подсказка по контру. */
@Composable
private fun CardGameMainRow(
  output: EngineOutput.ShowCardGame,
  selectedClash: Set<String>,
  cardPainter: @Composable (String) -> BitmapPainter?,
  onClashChange: (Set<String>) -> Unit,
  onConfirmClash: () -> Unit,
  onBattleContinue: () -> Unit,
  onBreakdownNext: () -> Unit,
  onVnNext: () -> Unit,
) {
  var expandedCard by remember(output.gameId) { mutableStateOf<EngineOutput.CardGameUiCard?>(null) }

  LaunchedEffect(output.phase) {
    expandedCard = null
  }

  val isBattleArena =
    output.phase == CardGamePhase.BATTLE_REVEAL || output.phase == CardGamePhase.SCORE_BREAKDOWN

  Box(Modifier.fillMaxSize().background(Bg)) {
  Row(Modifier.fillMaxSize()) {
    BoxWithConstraints(
      modifier = Modifier
        .weight(0.64f)
        .fillMaxHeight()
        .padding(start = 12.dp, end = 8.dp, top = 12.dp, bottom = 12.dp)
    ) {
      val cardW = maxOf(maxWidth * 0.2f, 68.dp)
      val cardH = cardW * 1.38f
      Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        CardGameHeader(output, showBattleTone = true)
        if (output.phase == CardGamePhase.SELECT_CLASH) {
          ClashPickScreen(
            hand = output.hand,
            selected = selectedClash,
            cardW = cardW,
            cardH = cardH,
            cardPainter = cardPainter,
            onChange = onClashChange,
            onConfirm = onConfirmClash
          )
        } else if (
          output.phase == CardGamePhase.BATTLE_REVEAL ||
          output.phase == CardGamePhase.SCORE_BREAKDOWN
        ) {
          BattleArenaScreen(
            output = output,
            cardW = cardW,
            cardH = cardH,
            cardPainter = cardPainter,
            onCardClick = { expandedCard = it },
            onBattleContinue = onBattleContinue,
            onBreakdownNext = onBreakdownNext
          )
        }
      }
    }

    CounterGuidePanel(
      battleTone = output.battleTone,
      showToneHint = output.phase != CardGamePhase.SELECT_CLASH,
      modifier = Modifier
        .weight(0.36f)
        .fillMaxHeight()
        .padding(end = 12.dp, top = 12.dp, bottom = 12.dp)
    )
  }

    if (
      output.phase == CardGamePhase.BATTLE_REVEAL &&
      !output.vnPlaybackComplete &&
      output.vnText != null
    ) {
      CardGameVnOverlay(
        speaker = output.vnSpeaker,
        text = output.vnText!!,
        onNext = onVnNext,
      )
    }

    if (isBattleArena) {
      expandedCard?.let { card ->
        CardExpandedOverlay(
          card = card,
          showEffective = output.phase == CardGamePhase.SCORE_BREAKDOWN,
          cardPainter = cardPainter,
          onDismiss = { expandedCard = null }
        )
      }
    }
  }
}

@Composable
private fun CardGameHeader(output: EngineOutput.ShowCardGame, showBattleTone: Boolean) {
  SkikoSafeText(output.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
  if (showBattleTone) {
    val toneStat = StatType.fromKey(output.battleTone)
    val toneLabel = toneStat?.title ?: output.battleTone
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
    ) {
      SkikoSafeText("Тон боя:", fontSize = 14.sp, color = Accent)
      Spacer(Modifier.width(8.dp))
      toneStat?.let {
        Image(
          painter = painterResource(it.image),
          contentDescription = it.title,
          modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(6.dp))
      }
      SkikoSafeText(toneLabel, fontSize = 14.sp, color = Color.White)
    }
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
}

@Composable
private fun DraftScreen(
  output: EngineOutput.ShowCardGame,
  selectedMeta: Set<String>,
  selectedPool: Set<String>,
  cardW: Dp,
  cardH: Dp,
  cardPainter: @Composable (String) -> BitmapPainter?,
  onMetaChange: (Set<String>) -> Unit,
  onPoolChange: (Set<String>) -> Unit,
  onConfirm: () -> Unit
) {
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
    cardPainter,
    onMetaChange
  )
  Spacer(Modifier.height(16.dp))
  DraftSection(
    "Колода — до $poolCap из ${output.offerCards.size} (снизу)",
    output.offerCards,
    selectedPool,
    poolCap,
    cardW,
    cardH,
    cardPainter,
    onPoolChange
  )
  Spacer(Modifier.height(12.dp))
  CardGameAction(
    text = "Собрать руку (${selectedMeta.size + selectedPool.size}/$handSize)",
    enabled = selectedMeta.size + selectedPool.size == handSize,
    onClick = onConfirm
  )
}

@Composable
private fun ClashPickScreen(
  hand: List<EngineOutput.CardGameUiCard>,
  selected: Set<String>,
  cardW: Dp,
  cardH: Dp,
  cardPainter: @Composable (String) -> BitmapPainter?,
  onChange: (Set<String>) -> Unit,
  onConfirm: () -> Unit
) {
  SkikoSafeText("Выберите 3 карты для боя", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
  Spacer(Modifier.height(8.dp))
  SelectableCardRow(hand, selected, cardW, cardH, cardPainter, max = 3, onChange)
  Spacer(Modifier.height(12.dp))
  CardGameAction("К бою", enabled = selected.size == 3, onClick = onConfirm)
}

/** Один экран боя: карты обеих сторон + разбор на том же месте. */
@Composable
private fun BattleArenaScreen(
  output: EngineOutput.ShowCardGame,
  cardW: Dp,
  cardH: Dp,
  cardPainter: @Composable (String) -> BitmapPainter?,
  onCardClick: (EngineOutput.CardGameUiCard) -> Unit,
  onBattleContinue: () -> Unit,
  onBreakdownNext: () -> Unit
) {
  val showEffective = output.phase == CardGamePhase.SCORE_BREAKDOWN
  LabeledCardRow("Ваши карты", output.playerPlayed, cardW, cardH, cardPainter, showEffective, onCardClick)
  Spacer(Modifier.height(12.dp))
  LabeledCardRow("Карты противника", output.enemyPlayed, cardW, cardH, cardPainter, showEffective, onCardClick)

  if (output.phase == CardGamePhase.BATTLE_REVEAL && output.vnPlaybackComplete) {
    Spacer(Modifier.height(16.dp))
    CardGameAction("Разбор боя", onClick = onBattleContinue)
  } else if (output.phase == CardGamePhase.SCORE_BREAKDOWN) {
    output.clashResolution?.let { resolution ->
      Spacer(Modifier.height(12.dp))
      ScoreBreakdownBlock(output, resolution, onBreakdownNext)
    }
  }
}

/** Увеличенная карта на весь экран; повторное нажатие закрывает. */
@Composable
private fun CardExpandedOverlay(
  card: EngineOutput.CardGameUiCard,
  showEffective: Boolean,
  cardPainter: @Composable (String) -> BitmapPainter?,
  onDismiss: () -> Unit
) {
  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xD9000000))
      .clickable(onClick = onDismiss),
    contentAlignment = Alignment.Center
  ) {
    val footerH = if (showEffective && card.effectiveValue != null) 28.dp else 24.dp
    val totalH = maxHeight * 0.96f
    val previewH = (totalH - footerH).coerceAtLeast(72.dp)
    val previewW = previewH / 1.38f
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.height(totalH)
    ) {
      GameCardTile(
        card = card,
        selected = false,
        cardW = previewW,
        cardH = previewH,
        painter = if (card.faceDown) null else cardPainter(card.image),
        onClick = onDismiss,
        showEffective = showEffective,
        showTagAndValue = false
      )
    }
    SkikoSafeText(
      "Нажмите, чтобы закрыть",
      fontSize = 12.sp,
      color = Color(0x99FFFFFF),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 16.dp)
    )
  }
}

@Composable
private fun CounterGuidePanel(
  battleTone: String,
  showToneHint: Boolean,
  modifier: Modifier = Modifier
) {
  Column(
    modifier
      .background(PanelBg, RoundedCornerShape(12.dp))
      .padding(14.dp)
      .verticalScroll(rememberScrollState())
  ) {
    SkikoSafeText(
      "Контр (основные)",
      fontSize = 15.sp,
      fontWeight = FontWeight.Bold,
      color = Color.White,
      modifier = Modifier.padding(bottom = 10.dp)
    )
    SkikoSafeText(
      "Стрелка: левый тег бьёт правый → value обнуляется.",
      fontSize = 11.sp,
      color = Color(0x99FFFFFF),
      modifier = Modifier.padding(bottom = 12.dp)
    )
    COUNTER_CHAIN.forEach { (attacker, defender) ->
      CounterChainRow(attacker, defender)
      Spacer(Modifier.height(10.dp))
    }

    if (showToneHint) {
      Spacer(Modifier.height(8.dp))
      SkikoSafeText(
        "Тон боя",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Accent,
        modifier = Modifier.padding(bottom = 8.dp)
      )
      val toneStat = StatType.fromKey(battleTone)
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
        toneStat?.let {
          Image(painterResource(it.image), it.title, Modifier.size(24.dp))
          Spacer(Modifier.width(8.dp))
        }
        SkikoSafeText(toneStat?.title ?: battleTone, fontSize = 13.sp, color = Color.White)
      }
      SkikoSafeText(
        "Карта тона = тону боя: множит базу.\nИначе тон просто +value.",
        fontSize = 11.sp,
        color = Color(0x99FFFFFF),
        modifier = Modifier.padding(bottom = 10.dp)
      )
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ToneLegendChip(StatType.DARK)
        ToneLegendChip(StatType.LIGHT)
      }
    }
  }
}

@Composable
private fun CounterChainRow(attacker: StatType, defender: StatType) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    TagChip(attacker)
    SkikoSafeText("→", fontSize = 16.sp, color = Accent, fontWeight = FontWeight.Bold)
    TagChip(defender)
  }
}

@Composable
private fun TagChip(stat: StatType) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .background(Color(0xFF263238), RoundedCornerShape(8.dp))
      .padding(horizontal = 8.dp, vertical = 6.dp)
  ) {
    Image(painterResource(stat.image), stat.title, Modifier.size(28.dp))
    Spacer(Modifier.width(6.dp))
    SkikoSafeText(stat.title, fontSize = 12.sp, color = Color.White)
  }
}

@Composable
private fun ToneLegendChip(stat: StatType) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Image(painterResource(stat.image), stat.title, Modifier.size(32.dp))
    SkikoSafeText(stat.title, fontSize = 10.sp, color = Muted, modifier = Modifier.padding(top = 4.dp))
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
  CardRow(
    cards = cards,
    selected = selected,
    cardW = cardW,
    cardH = cardH,
    cardPainter = cardPainter,
    selectable = true,
    onToggle = { id -> onSelected(toggleSelection(selected, id, max)) }
  )
}

@Composable
private fun LabeledCardRow(
  label: String,
  cards: List<EngineOutput.CardGameUiCard>,
  cardW: Dp,
  cardH: Dp,
  cardPainter: @Composable (String) -> BitmapPainter?,
  showEffective: Boolean = false,
  onCardClick: ((EngineOutput.CardGameUiCard) -> Unit)? = null
) {
  SkikoSafeText(label, color = Color.White, modifier = Modifier.padding(bottom = 4.dp))
  CardRow(cards, emptySet(), cardW, cardH, cardPainter, showEffective = showEffective, onCardClick = onCardClick)
}

@Composable
private fun ScoreBreakdownBlock(
  output: EngineOutput.ShowCardGame,
  resolution: ClashResolution,
  onNext: () -> Unit
) {
  val side = output.breakdownSide
  val step = output.breakdownStepIndex
  val steps = when (side) {
    "PLAYER" -> resolution.playerScore.steps
    "ENEMY" -> resolution.enemyScore.steps
    else -> emptyList()
  }
  steps.getOrNull(step)?.let { s ->
    SkikoSafeText(
      "${s.label}: ${s.detail} → ${s.runningTotal}",
      fontSize = 15.sp,
      color = Accent,
      modifier = Modifier.padding(vertical = 4.dp)
    )
  }
  if (side == "COMPARE") {
    SkikoSafeText(
      "Итог: вы ${resolution.playerTotal} — враг ${resolution.enemyTotal}",
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
      color = Color.White,
      modifier = Modifier.padding(vertical = 8.dp)
    )
  }
  CardGameAction(
    text = if (side == "COMPARE") "К результату" else "Далее",
    onClick = onNext
  )
}

@Composable
private fun CardGameVnOverlay(speaker: String?, text: String, onNext: () -> Unit) {
  Box(
    Modifier
      .fillMaxSize()
      .background(Color(0x99000000))
      .clickable(onClick = onNext)
      .padding(20.dp),
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
private fun CardRow(
  cards: List<EngineOutput.CardGameUiCard>,
  selected: Set<String>,
  cardW: Dp,
  cardH: Dp,
  cardPainter: @Composable (String) -> BitmapPainter?,
  selectable: Boolean = false,
  showEffective: Boolean = false,
  onToggle: (String) -> Unit = {},
  onCardClick: ((EngineOutput.CardGameUiCard) -> Unit)? = null
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
        onClick = when {
          selectable -> ({ onToggle(card.id) })
          onCardClick != null -> ({ onCardClick(card) })
          else -> null
        },
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
  onClick: (() -> Unit)? = null,
  showEffective: Boolean,
  showTagAndValue: Boolean = false
) {
  val borderColor = when {
    card.countered -> Color(0xFFFF5252)
    selected -> Color(0xFF64B5F6)
    else -> Color.Transparent
  }
  val shape = RoundedCornerShape(8.dp)
  val artModifier = Modifier
    .width(cardW)
    .height(cardH)
    .border(3.dp, borderColor, shape)
    .background(if (card.faceDown) Color(0xFF1A237E) else Color(0xFF263238), shape)
    .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Box(
      artModifier,
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
