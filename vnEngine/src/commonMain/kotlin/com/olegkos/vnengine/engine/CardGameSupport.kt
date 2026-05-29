package com.olegkos.vnengine.engine

import com.olegkos.vnengine.engine.EngineOutput.CardGameUiCard
import com.olegkos.vnengine.engine.EngineOutput.ShowCardGame
import com.olegkos.vnengine.engine.cardgame.BreakdownSide
import com.olegkos.vnengine.engine.cardgame.CardGameLogic
import com.olegkos.vnengine.engine.cardgame.CardGameOutcome
import com.olegkos.vnengine.engine.cardgame.CardGamePhase
import com.olegkos.vnengine.engine.cardgame.CardGameState
import com.olegkos.vnengine.engine.cardgame.HandCard
import com.olegkos.vnengine.scene.SceneNode

internal fun VnEngine.handleCardGameNode(node: SceneNode.CardGame): EngineOutput {
  val deck = cards ?: error("CardManager required for cardGame node")
  val gs = state.cardGame ?: CardGameState(
    gameId = node.id,
    battleTone = CardGameLogic.randomBattleTone()
  ).also { state.cardGame = it }

  if (gs.battleTone.isEmpty()) {
    gs.battleTone = CardGameLogic.randomBattleTone()
  }

  return when (gs.phase) {
    CardGamePhase.DRAFT -> {
      if (gs.offerCards.isEmpty()) {
        gs.offerCards = deck.drawUniqueWeighted(node.draft.offerCount)
          .map { HandCard.fromDeck(it) }
      }
      buildCardGameOutput(node, gs)
    }
    CardGamePhase.SELECT_CLASH -> buildCardGameOutput(node, gs)
    CardGamePhase.BATTLE_REVEAL -> buildCardGameOutput(node, gs)
    CardGamePhase.SCORE_BREAKDOWN -> buildCardGameOutput(node, gs)
    CardGamePhase.RESULT -> buildCardGameOutput(node, gs)
  }
}

fun VnEngine.cardGameConfirmDraft(
  metaSelectedIds: List<String>,
  poolSelectedIds: List<String>,
  metaCards: List<HandCard>
) {
  val node = currentCardGameNode() ?: return
  val gs = state.cardGame ?: return
  if (gs.phase != CardGamePhase.DRAFT) return

  val metaMax = node.draft.metaMax
  val handSize = node.draft.handSize
  val metaPicked = metaCards
    .filter { it.instanceId in metaSelectedIds.toSet() }
    .take(metaMax.coerceAtMost(handSize))
  val poolPicked = gs.offerCards.filter { it.instanceId in poolSelectedIds.toSet() }
  if (metaPicked.size + poolPicked.size != handSize) return

  val unpicked = gs.offerCards.filter { it.instanceId !in poolPicked.map { c -> c.instanceId }.toSet() }
  gs.discard = gs.discard + unpicked
  gs.metaPicked = metaPicked
  gs.poolPicked = poolPicked
  gs.hand = metaPicked + poolPicked
  gs.offerCards = emptyList()
  gs.phase = CardGamePhase.SELECT_CLASH
  prepareEnemyClash(gs, node)
}

private fun VnEngine.prepareEnemyClash(gs: CardGameState, node: SceneNode.CardGame) {
  if (gs.enemyClash.isNotEmpty()) return
  val deck = cards ?: return
  val draft = node.draft
  val pool = deck.drawUniqueWeighted(draft.offerCount).map { HandCard.fromDeck(it) }
  gs.enemyClash = CardGameLogic.pickEnemyClash(
    pool = pool,
    handSize = draft.handSize,
    clashSize = 3,
    battleTone = gs.battleTone,
  )
}

fun VnEngine.cardGameConfirmClash(selectedIds: List<String>) {
  val gs = state.cardGame ?: return
  val node = currentCardGameNode() ?: return
  if (gs.phase != CardGamePhase.SELECT_CLASH) return
  val picked = gs.hand.filter { it.instanceId in selectedIds.toSet() }
  if (picked.size != 3) return

  gs.playerClash = picked
  val unused = gs.hand.filter { it.instanceId !in picked.map { c -> c.instanceId }.toSet() }
  gs.discard = gs.discard + unused
  gs.hand = emptyList()

  prepareEnemyClash(gs, node)
  resolveClashAndShowBattle(gs)
}

private fun VnEngine.resolveClashAndShowBattle(gs: CardGameState) {
  gs.cardsRevealed = true
  val playerTags = gs.playerClash.map { it.toTagValue() }
  val enemyTags = gs.enemyClash.map { it.toTagValue() }
  gs.clashResolution = CardGameLogic.resolveClash(playerTags, enemyTags, gs.battleTone)
  gs.vnIndex = 0
  gs.phase = CardGamePhase.BATTLE_REVEAL
}

fun VnEngine.cardGameBattleContinue() {
  val node = currentCardGameNode() ?: return
  val gs = state.cardGame ?: return
  if (gs.phase != CardGamePhase.BATTLE_REVEAL) return
  if (!cardGameVnPlaybackComplete(node, gs)) return
  gs.breakdownSide = BreakdownSide.PLAYER
  gs.breakdownStepIndex = 0
  gs.phase = CardGamePhase.SCORE_BREAKDOWN
}

fun VnEngine.cardGameBreakdownNext() {
  val gs = state.cardGame ?: return
  if (gs.phase != CardGamePhase.SCORE_BREAKDOWN) return
  val resolution = gs.clashResolution ?: return

  val playerSteps = resolution.playerScore.steps.size
  val enemySteps = resolution.enemyScore.steps.size

  when (gs.breakdownSide) {
    BreakdownSide.PLAYER -> {
      if (gs.breakdownStepIndex < playerSteps - 1) {
        gs.breakdownStepIndex++
      } else {
        gs.breakdownSide = BreakdownSide.ENEMY
        gs.breakdownStepIndex = 0
      }
    }
    BreakdownSide.ENEMY -> {
      if (gs.breakdownStepIndex < enemySteps - 1) {
        gs.breakdownStepIndex++
      } else {
        gs.breakdownSide = BreakdownSide.COMPARE
        gs.breakdownStepIndex = 0
      }
    }
    BreakdownSide.COMPARE -> {
      gs.outcome = when {
        resolution.playerTotal > resolution.enemyTotal -> CardGameOutcome.WIN
        resolution.playerTotal < resolution.enemyTotal -> CardGameOutcome.LOSE
        else -> CardGameOutcome.DRAW
      }
      gs.phase = CardGamePhase.RESULT
    }
  }
}

fun VnEngine.cardGameVnNext() {
  val node = currentCardGameNode() ?: return
  val gs = state.cardGame ?: return
  if (gs.phase != CardGamePhase.BATTLE_REVEAL) return
  if (node.vnAfterClash.isEmpty()) return
  if (gs.vnIndex >= node.vnAfterClash.size) return
  gs.vnIndex++
}

private fun cardGameVnPlaybackComplete(node: SceneNode.CardGame, gs: CardGameState): Boolean =
  node.vnAfterClash.isEmpty() || gs.vnIndex >= node.vnAfterClash.size

fun VnEngine.cardGameFinish(): CardGameFinishResult? {
  val node = currentCardGameNode() ?: return null
  val gs = state.cardGame ?: return null
  if (gs.phase != CardGamePhase.RESULT) return null

  val outcome = gs.outcome ?: return null
  val discard = gs.discard.toList()

  val targetScene = when (outcome) {
    CardGameOutcome.WIN -> node.transitions.winScene
    CardGameOutcome.LOSE -> node.transitions.loseScene
    CardGameOutcome.DRAW -> node.transitions.drawScene
  }

  state.cardGame = null
  jumpToScene(targetScene)

  return CardGameFinishResult(
    outcome = outcome,
    discardForReward = if (outcome == CardGameOutcome.WIN) discard else emptyList()
  )
}

data class CardGameFinishResult(
  val outcome: CardGameOutcome,
  val discardForReward: List<HandCard>
)

fun VnEngine.currentCardGameNode(): SceneNode.CardGame? =
  currentNode() as? SceneNode.CardGame

fun VnEngine.buildCardGameOutput(node: SceneNode.CardGame, gs: CardGameState): ShowCardGame {
  val title = resolveTextVariables(node.title)
  val opponentName = resolveTextVariables(node.opponent.name)
  val opponentImage = resolveTextVariables(node.opponent.image)
  val playerName = variables.getString("my_name").trim().takeIf { it.isNotEmpty() }

  val resolution = gs.clashResolution

  val playerPlayed = gs.playerClash.mapIndexed { idx, card ->
    card.toUi(
      effective = resolution?.playerEffective?.getOrNull(idx),
      countered = resolution?.let {
        CardGameLogic.counteredByOpponent(card.toTagValue(), it.enemyCards)
      } ?: false,
      faceDown = !gs.cardsRevealed
    )
  }
  val enemyPlayed = gs.enemyClash.mapIndexed { idx, card ->
    card.toUi(
      effective = resolution?.enemyEffective?.getOrNull(idx),
      countered = resolution?.let {
        CardGameLogic.counteredByOpponent(card.toTagValue(), it.playerCards)
      } ?: false,
      faceDown = !gs.cardsRevealed
    )
  }

  val vnLine = if (
    gs.phase == CardGamePhase.BATTLE_REVEAL &&
    gs.vnIndex < node.vnAfterClash.size
  ) {
    node.vnAfterClash[gs.vnIndex]
  } else {
    null
  }

  val defaultSpeaker = node.speaker?.trim()?.takeIf { it.isNotEmpty() }?.let(::resolveTextVariables)

  val resultText = when (gs.phase) {
    CardGamePhase.RESULT -> when (gs.outcome) {
      CardGameOutcome.WIN -> "Победа"
      CardGameOutcome.LOSE -> "Поражение"
      CardGameOutcome.DRAW -> "Ничья"
      null -> null
    }
    else -> null
  }

  return ShowCardGame(
    gameId = node.id,
    title = title,
    speaker = defaultSpeaker,
    playerName = playerName,
    opponentName = opponentName,
    opponentImage = opponentImage,
    phase = gs.phase,
    battleTone = gs.battleTone,
    draftMetaMax = node.draft.metaMax,
    draftHandSize = node.draft.handSize,
    offerCards = gs.offerCards.map { it.toUi() },
    hand = gs.hand.map { it.toUi() },
    playerPlayed = playerPlayed,
    enemyPlayed = enemyPlayed,
    cardsRevealed = gs.cardsRevealed,
    clashResolution = resolution,
    breakdownSide = gs.breakdownSide.name,
    breakdownStepIndex = gs.breakdownStepIndex,
    vnSpeaker = vnLine?.speaker?.trim()?.takeIf { it.isNotEmpty() }?.let(::resolveTextVariables)
      ?: defaultSpeaker,
    vnText = vnLine?.let { resolveTextVariables(it.text) },
    vnPlaybackComplete = cardGameVnPlaybackComplete(node, gs),
    outcome = gs.outcome,
    resultText = resultText
  )
}

private fun HandCard.toUi(
  effective: Int? = null,
  countered: Boolean = false,
  faceDown: Boolean = false
): CardGameUiCard = CardGameUiCard(
  id = instanceId,
  value = value,
  image = image,
  tag = tag,
  effectiveValue = effective,
  countered = countered,
  faceDown = faceDown
)
