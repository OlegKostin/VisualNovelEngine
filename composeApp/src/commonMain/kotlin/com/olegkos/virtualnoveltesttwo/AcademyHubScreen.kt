package com.olegkos.virtualnoveltesttwo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olegkos.virtualnovelapp.GameViewModel
import com.olegkos.virtualnoveltesttwo.theme.AcademyHubTypography
import com.olegkos.virtualnoveltesttwo.theme.LocalAcademyHubTypography
import com.olegkos.virtualnoveltesttwo.theme.SkikoSafeText
import com.olegkos.virtualnoveltesttwo.theme.VnButtonSurface
import com.olegkos.virtualnoveltesttwo.theme.VnOutlinedButton
import com.olegkos.vnengine.engine.EngineOutput

private val Accent = Color(0xFFBBDEFB)
private val BaseBg = Color(0xFF10141C)
private val Divider = Color(0x33FFFFFF)

private val UnlockAccent = Color(0xFFE1BEE7)

@Composable
fun AcademyHubScreen(
  output: EngineOutput.ShowAcademyHub,
  viewModel: GameViewModel,
) {
  var buildMenuOpen by remember { mutableStateOf(false) }

  BoxWithConstraints(Modifier.fillMaxSize().background(BaseBg)) {
    val typography = remember(maxHeight, maxWidth) {
      AcademyHubTypography.fromViewport(maxHeight.value, maxWidth.value)
    }
    CompositionLocalProvider(LocalAcademyHubTypography provides typography) {
  Column(Modifier.fillMaxSize()) {
    SkikoSafeText(
      text = "День ${output.day}",
      fontSize = typography.dayHeader,
      color = Color.White,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
    )

    Row(
      Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      output.timeSlots.forEach { slot ->
        PhaseColumn(
          slot = slot,
          modifier = Modifier.weight(1f),
          onSelectActivity = { viewModel.academySetActivity(slot.phaseId, it) },
        )
      }

      ResourcesAndBuildColumn(
        modifier = Modifier.weight(1f),
        output = output,
        onOpenBuildMenu = { buildMenuOpen = true },
        onCommitDay = { viewModel.academyCommitDay() },
      )
    }
  }

    if (buildMenuOpen) {
      AcademyBuildMenuOverlay(
        output = output,
        onDismiss = { buildMenuOpen = false },
        onSelectBuilding = { id, selected ->
          if (selected) viewModel.academySelectBuilding(null)
          else viewModel.academySelectBuilding(id)
        },
        onQueueUnlock = { viewModel.academyQueueUnlock(it) },
        onEnactLaw = { viewModel.academyEnactLaw(it) },
      )
    }
    }
  }
}

@Composable
private fun PhaseColumn(
  slot: EngineOutput.AcademyTimeSlotUi,
  modifier: Modifier = Modifier,
  onSelectActivity: (String) -> Unit,
) {
  val typography = LocalAcademyHubTypography.current
  val buildings = slot.activities.filter { it.fromBuilding }
  val actions = slot.activities.filter { !it.fromBuilding }
  val hasSelection = slot.selectedActivityId != null
  val zoneColor = phaseZoneColor(slot.phaseId)
  val borderColor = if (hasSelection) Accent else Divider

  Column(
    modifier
      .fillMaxHeight()
      .background(zoneColor, RoundedCornerShape(10.dp))
      .border(1.dp, borderColor, RoundedCornerShape(10.dp))
      .padding(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    SkikoSafeText(
      text = slot.label,
      fontSize = typography.phaseHeader,
      color = Color.White,
      modifier = Modifier.padding(bottom = 6.dp),
    )

    PhaseHalfPanel(
      title = "Строения",
      items = buildings,
      selectedActivityId = slot.selectedActivityId,
      isBuildingSection = true,
      emptyHint = "Нет строений",
      onSelectActivity = onSelectActivity,
      modifier = Modifier.weight(1f),
    )

    Spacer(Modifier.height(4.dp))
    Spacer(
      Modifier
        .fillMaxWidth()
        .height(1.dp)
        .background(Divider),
    )
    Spacer(Modifier.height(4.dp))

    PhaseHalfPanel(
      title = "Действия",
      items = actions,
      selectedActivityId = slot.selectedActivityId,
      isBuildingSection = false,
      emptyHint = "Нет действий",
      onSelectActivity = onSelectActivity,
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun PhaseHalfPanel(
  title: String,
  items: List<EngineOutput.AcademyActivityOptionUi>,
  selectedActivityId: String?,
  isBuildingSection: Boolean,
  emptyHint: String,
  onSelectActivity: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val typography = LocalAcademyHubTypography.current
  Column(modifier.fillMaxWidth()) {
    val sectionTitleColor = if (isBuildingSection && items.any { it.highlightBuilding }) {
      Color(0xFFC8E6C9)
    } else {
      Accent
    }
    SkikoSafeText(
      text = title,
      fontSize = typography.section,
      color = sectionTitleColor,
      modifier = Modifier.padding(bottom = 6.dp),
    )

    Column(
      Modifier
        .weight(1f)
        .fillMaxWidth()
        .verticalScroll(rememberScrollState()),
    ) {
      if (items.isEmpty()) {
        SkikoSafeText(emptyHint, fontSize = typography.hint, color = Color(0x99FFFFFF))
      } else {
        items.forEach { act ->
          PhaseActivityButton(
            act = act,
            selected = act.id == selectedActivityId,
            onClick = { onSelectActivity(act.id) },
          )
        }
      }
    }
  }
}

@Composable
private fun PhaseActivityButton(
  act: EngineOutput.AcademyActivityOptionUi,
  selected: Boolean,
  onClick: () -> Unit,
) {
  val typography = LocalAcademyHubTypography.current
  val isUnlock = act.fromUnlockable
  val showBuildingGreen = act.highlightBuilding
  val accentBorder = when {
    selected -> Accent
    showBuildingGreen -> Color(0xFF66BB6A)
    isUnlock -> UnlockAccent
    else -> null
  }
  VnOutlinedButton(
    onClick = onClick,
    selected = selected,
    surface = VnButtonSurface.Dark,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp)
      .then(
        if (!selected && accentBorder != null) {
          Modifier.border(1.dp, accentBorder, RoundedCornerShape(8.dp))
        } else {
          Modifier
        }
      ),
  ) {
    Text(
      text = act.label,
      fontSize = typography.body,
      color = when {
        selected -> Accent
        showBuildingGreen -> Color(0xFFC8E6C9)
        isUnlock -> UnlockAccent
        else -> Color.White
      },
    )
  }
}

@Composable
private fun AcademyBuildMenuOverlay(
  output: EngineOutput.ShowAcademyHub,
  onDismiss: () -> Unit,
  onSelectBuilding: (buildingId: String, currentlySelected: Boolean) -> Unit,
  onQueueUnlock: (unlockId: String?) -> Unit,
  onEnactLaw: (lawId: String) -> Unit,
) {
  val typography = LocalAcademyHubTypography.current
  var tab by remember { mutableStateOf(BuildMenuTab.Buildings) }

  Box(Modifier.fillMaxSize()) {
    Box(
      Modifier
        .fillMaxSize()
        .background(Color(0xCC000000))
        .clickable(
          indication = null,
          interactionSource = remember { MutableInteractionSource() },
        ) { onDismiss() },
    )

    Column(
      Modifier
        .align(Alignment.Center)
        .fillMaxWidth(0.92f)
        .fillMaxHeight(0.82f)
        .background(Color(0xFF1C2838), RoundedCornerShape(14.dp))
        .border(1.dp, Accent, RoundedCornerShape(14.dp))
        .padding(12.dp)
        .clickable(
          indication = null,
          interactionSource = remember { MutableInteractionSource() },
        ) { },
    ) {
      Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SkikoSafeText("Меню", fontSize = typography.menuTitle, color = Color.White)
        VnOutlinedButton(onClick = onDismiss, surface = VnButtonSurface.Dark) {
          Text("✕", fontSize = typography.menuClose, color = Color.White)
        }
      }

      BuildMenuTabRow(
        selected = tab,
        onSelect = { tab = it },
        modifier = Modifier.padding(vertical = 10.dp),
      )

      BuildMenuListPanel(
        modifier = Modifier.weight(1f),
      ) {
        when (tab) {
          BuildMenuTab.Buildings -> BuildingsMenuContent(output, onSelectBuilding)
          BuildMenuTab.Actions -> ActionsMenuContent(output, onQueueUnlock)
          BuildMenuTab.Laws -> LawsMenuContent(output, onEnactLaw)
        }
      }
    }
  }
}

private enum class BuildMenuTab { Buildings, Actions, Laws }

/** Ширина вкладки «Строения / Действия / Законы». */
private const val BuildMenuTabWidthFraction = 0.2f

@Composable
private fun BuildMenuTabRow(
  selected: BuildMenuTab,
  onSelect: (BuildMenuTab) -> Unit,
  modifier: Modifier = Modifier,
) {
  BoxWithConstraints(modifier.fillMaxWidth()) {
    val tabWidth = maxWidth * BuildMenuTabWidthFraction
    Row(
      Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      BuildMenuTab.entries.forEach { entry ->
        BuildMenuTabButton(
          label = when (entry) {
            BuildMenuTab.Buildings -> "Строения"
            BuildMenuTab.Actions -> "Действия"
            BuildMenuTab.Laws -> "Законы"
          },
          selected = selected == entry,
          onClick = { onSelect(entry) },
          modifier = Modifier
            .width(tabWidth)
            .padding(horizontal = 4.dp),
        )
      }
    }
  }
}

@Composable
private fun BuildMenuListPanel(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Column(
    modifier
      .fillMaxWidth()
      .fillMaxHeight()
      .verticalScroll(rememberScrollState()),
  ) {
    content()
  }
}

private val LawDoneGreen = Color(0xFF66BB6A)
private val LawDoneBg = Color(0xFF2E4A3A)
private val CardZoneTitleBg = Color(0xFF2A3A52)
private val CardZoneReqBg = Color(0xFF232D3F)
private val CardZoneDescBg = Color(0xFF1E2836)
private val CardZoneActionBg = Color(0xFF1A2230)

@Composable
private fun AcademyMenuItemCard(
  title: String,
  requirements: String,
  description: String,
  actionLabel: String,
  onAction: () -> Unit,
  actionEnabled: Boolean,
  actionSelected: Boolean = false,
  completed: Boolean = false,
  modifier: Modifier = Modifier,
) {
  val typography = LocalAcademyHubTypography.current
  val borderColor = when {
    completed -> LawDoneGreen
    actionSelected -> Accent
    else -> Divider
  }
  val titleColor = if (completed) Color(0xFFC8E6C9) else Color.White
  val bodyColor = Color(0xD9FFFFFF)
  val row1Bg = if (completed) LawDoneBg else CardZoneTitleBg
  val row2Bg = if (completed) LawDoneBg.copy(alpha = 0.85f) else CardZoneDescBg

  BoxWithConstraints(
    modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp)
      .clip(RoundedCornerShape(10.dp))
      .border(1.dp, borderColor, RoundedCornerShape(10.dp)),
  ) {
    val titleWidth = maxWidth * 0.25f
    val requirementsWidth = maxWidth * 0.75f
    val descriptionWidth = maxWidth * 0.8f
    val buttonWidth = maxWidth * 0.2f

    Column(Modifier.fillMaxWidth()) {
      Row(
        Modifier
          .fillMaxWidth()
          .heightIn(min = typography.cardRowMinHeight)
          .background(row1Bg),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          Modifier
            .width(titleWidth)
            .fillMaxHeight()
            .padding(horizontal = 6.dp, vertical = 8.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = title,
            fontSize = typography.cardTitle,
            fontWeight = FontWeight.Bold,
            color = titleColor,
            textAlign = TextAlign.Center,
            lineHeight = typography.lineHeight(typography.cardTitle),
          )
        }
        Box(
          Modifier
            .width(requirementsWidth)
            .fillMaxHeight()
            .background(if (completed) LawDoneBg else CardZoneReqBg)
            .padding(horizontal = 8.dp, vertical = 8.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = requirements,
            fontSize = typography.cardBody,
            color = bodyColor,
            textAlign = TextAlign.Center,
            lineHeight = typography.lineHeight(typography.cardBody),
          )
        }
      }

      Row(
        Modifier
          .fillMaxWidth()
          .heightIn(min = typography.cardActionRowMinHeight)
          .background(row2Bg),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          Modifier
            .width(descriptionWidth)
            .fillMaxHeight()
            .padding(horizontal = 10.dp, vertical = 8.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = description,
            fontSize = typography.cardBody,
            color = bodyColor,
            textAlign = TextAlign.Center,
            lineHeight = typography.lineHeight(typography.cardBody),
          )
        }
        Box(
          Modifier
            .width(buttonWidth)
            .fillMaxHeight()
            .background(if (completed) LawDoneBg else CardZoneActionBg)
            .padding(horizontal = 4.dp, vertical = 6.dp),
          contentAlignment = Alignment.Center,
        ) {
          VnOutlinedButton(
            onClick = onAction,
            enabled = actionEnabled,
            selected = actionSelected,
            surface = VnButtonSurface.Dark,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
          ) {
            Text(
              text = actionLabel,
              fontSize = typography.cardButton,
              textAlign = TextAlign.Center,
              maxLines = 2,
              color = when {
                completed -> Color(0xFFC8E6C9)
                actionSelected -> Accent
                actionEnabled -> Color.White
                else -> Color(0xFF6B7588)
              },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun BuildMenuTabButton(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val typography = LocalAcademyHubTypography.current
  VnOutlinedButton(
    onClick = onClick,
    selected = selected,
    surface = VnButtonSurface.Dark,
    modifier = modifier.fillMaxWidth(),
  ) {
    Text(
      text = label,
      fontSize = typography.tab,
      color = if (selected) Accent else Color.White,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
private fun BuildingsMenuContent(
  output: EngineOutput.ShowAcademyHub,
  onSelectBuilding: (buildingId: String, currentlySelected: Boolean) -> Unit,
) {
  val typography = LocalAcademyHubTypography.current
  SkikoSafeText(
    text = "Строительство (1× в день)",
    fontSize = typography.menuHint,
    color = Color(0xBBFFFFFF),
    modifier = Modifier.padding(bottom = 8.dp),
  )
  output.buildingGroups.forEach { group ->
    SkikoSafeText(
      group.label,
      fontSize = typography.hint,
      color = Color(0x99FFFFFF),
      modifier = Modifier.padding(top = 4.dp, bottom = 6.dp),
    )
    group.buildings.forEach { building ->
      AcademyMenuItemCard(
        title = building.label,
        requirements = building.requirementsText,
        description = building.descriptionText,
        actionLabel = building.actionLabel,
        onAction = {
          if (building.enabled || building.selected) {
            onSelectBuilding(building.id, building.selected)
          }
        },
        actionEnabled = building.enabled || building.selected,
        actionSelected = building.selected,
        completed = building.completed,
      )
    }
  }
}

@Composable
private fun LawsMenuContent(
  output: EngineOutput.ShowAcademyHub,
  onEnactLaw: (lawId: String) -> Unit,
) {
  val typography = LocalAcademyHubTypography.current
  SkikoSafeText(
    text = "Одноразовые законы (принять сразу)",
    fontSize = typography.menuHint,
    color = Color(0xBBFFFFFF),
    modifier = Modifier.padding(bottom = 8.dp),
  )

  if (output.laws.isEmpty()) {
    SkikoSafeText("Нет законов в конфиге", fontSize = typography.hint, color = Color(0x99FFFFFF))
    return
  }

  output.laws.forEach { law ->
    AcademyMenuItemCard(
      title = law.label,
      requirements = law.requirementsText,
      description = law.descriptionText,
      actionLabel = law.actionLabel,
      onAction = { if (law.actionEnabled) onEnactLaw(law.id) },
      actionEnabled = law.actionEnabled,
      completed = law.status == EngineOutput.AcademyLawStatus.ENACTED,
    )
  }
}

@Composable
private fun ActionsMenuContent(
  output: EngineOutput.ShowAcademyHub,
  onQueueUnlock: (unlockId: String?) -> Unit,
) {
  val typography = LocalAcademyHubTypography.current
  SkikoSafeText(
    text = "Разблокировка на следующий день",
    fontSize = typography.menuHint,
    color = Color(0xBBFFFFFF),
    modifier = Modifier.padding(bottom = 8.dp),
  )

  if (output.unlockableActions.isEmpty()) {
    SkikoSafeText("Пока нет доступных режимов", fontSize = typography.hint, color = Color(0x99FFFFFF))
    return
  }

  output.unlockableActions.forEach { unlock ->
    AcademyMenuItemCard(
      title = unlock.label,
      requirements = unlock.requirementsText,
      description = unlock.descriptionText,
      actionLabel = unlock.actionLabel,
      onAction = { if (unlock.actionEnabled) onQueueUnlock(unlock.id) },
      actionEnabled = unlock.actionEnabled,
      actionSelected = unlock.status == EngineOutput.AcademyUnlockableStatus.PENDING,
      completed = unlock.completed,
    )
  }
}

@Composable
private fun ResourcesAndBuildColumn(
  output: EngineOutput.ShowAcademyHub,
  modifier: Modifier = Modifier,
  onOpenBuildMenu: () -> Unit,
  onCommitDay: () -> Unit,
) {
  val typography = LocalAcademyHubTypography.current
  Column(
    modifier
      .fillMaxHeight()
      .background(Color(0xFF1C2838), RoundedCornerShape(10.dp))
      .border(1.dp, Divider, RoundedCornerShape(10.dp))
      .padding(8.dp)
      .verticalScroll(rememberScrollState()),
  ) {
    SkikoSafeText(
      "Показатели",
      fontSize = typography.panelTitle,
      color = Accent,
      modifier = Modifier.padding(bottom = 6.dp),
    )
    StatLine(output.resourcesLabel, output.resources.toString(), typography)
    output.stats.forEach { stat ->
      StatLine(stat.label, stat.displayValue, typography)
    }

    output.selectedBuildingId?.let { selectedId ->
      val label = output.buildingGroups
        .flatMap { it.buildings }
        .firstOrNull { it.id == selectedId }
        ?.label
      if (label != null) {
        SkikoSafeText(
          text = "На стройку: $label",
          fontSize = typography.hint,
          color = Color(0xFFC8E6C9),
          modifier = Modifier.padding(top = 6.dp),
        )
      }
    }

    output.pendingUnlockLabel?.let { label ->
      SkikoSafeText(
        text = "С завтра: $label",
        fontSize = typography.hint,
        color = UnlockAccent,
        modifier = Modifier.padding(top = 6.dp),
      )
    }

    Spacer(Modifier.height(12.dp))

    VnOutlinedButton(
      onClick = onOpenBuildMenu,
      enabled = output.planning,
      surface = VnButtonSurface.Dark,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text("Построить", fontSize = typography.button, color = Color.White)
    }

    output.commitBlockedReason?.let {
      SkikoSafeText(it, fontSize = typography.hint, color = Color(0xFFFFAB91), modifier = Modifier.padding(top = 8.dp))
    }

    VnOutlinedButton(
      onClick = onCommitDay,
      enabled = output.canCommit,
      selected = output.canCommit,
      surface = VnButtonSurface.Dark,
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 12.dp),
    ) {
      Text(
        "Подтвердить день",
        fontSize = typography.button,
        color = if (output.canCommit) Accent else Color(0xFF6B7588),
      )
    }
  }
}

@Composable
private fun StatLine(
  label: String,
  value: String,
  typography: AcademyHubTypography,
) {
  Row(
    Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(label, fontSize = typography.stat, color = Color(0xCCFFFFFF))
    Text(value, fontSize = typography.stat, color = Color.White)
  }
}

private fun phaseZoneColor(phaseId: String): Color = when (phaseId) {
  "morning" -> Color(0xFF243044)
  "day" -> Color(0xFF2A3A4F)
  "evening" -> Color(0xFF352838)
  "night" -> Color(0xFF1A2230)
  else -> Color(0xFF1E2836)
}
