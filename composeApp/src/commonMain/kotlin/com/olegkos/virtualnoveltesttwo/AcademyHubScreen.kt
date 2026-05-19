package com.olegkos.virtualnoveltesttwo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olegkos.virtualnovelapp.GameViewModel
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

  Box(Modifier.fillMaxSize().background(BaseBg)) {
  Column(Modifier.fillMaxSize()) {
    SkikoSafeText(
      text = "День ${output.day}",
      fontSize = 18.sp,
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
      )
    }
  }
}

@Composable
private fun PhaseColumn(
  slot: EngineOutput.AcademyTimeSlotUi,
  modifier: Modifier = Modifier,
  onSelectActivity: (String) -> Unit,
) {
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
      fontSize = 15.sp,
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
  Column(modifier.fillMaxWidth()) {
    SkikoSafeText(
      text = title,
      fontSize = 12.sp,
      color = if (isBuildingSection) Color(0xFFC8E6C9) else Accent,
      modifier = Modifier.padding(bottom = 6.dp),
    )

    Column(
      Modifier
        .weight(1f)
        .fillMaxWidth()
        .verticalScroll(rememberScrollState()),
    ) {
      if (items.isEmpty()) {
        SkikoSafeText(emptyHint, fontSize = 10.sp, color = Color(0x99FFFFFF))
      } else {
        items.forEach { act ->
          PhaseActivityButton(
            act = act,
            selected = act.id == selectedActivityId,
            isBuildingSection = isBuildingSection,
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
  isBuildingSection: Boolean,
  onClick: () -> Unit,
) {
  val isUnlock = act.fromUnlockable
  val accentBorder = when {
    selected -> Accent
    isBuildingSection -> Color(0xFF66BB6A)
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
      fontSize = 12.sp,
      color = when {
        selected -> Accent
        isBuildingSection -> Color(0xFFC8E6C9)
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
) {
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
        SkikoSafeText("Меню", fontSize = 17.sp, color = Color.White)
        VnOutlinedButton(onClick = onDismiss, surface = VnButtonSurface.Dark) {
          Text("✕", fontSize = 14.sp, color = Color.White)
        }
      }

      Row(
        Modifier
          .fillMaxWidth()
          .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        BuildMenuTabButton(
          label = "Строения",
          selected = tab == BuildMenuTab.Buildings,
          onClick = { tab = BuildMenuTab.Buildings },
          modifier = Modifier.weight(1f),
        )
        BuildMenuTabButton(
          label = "Действия",
          selected = tab == BuildMenuTab.Actions,
          onClick = { tab = BuildMenuTab.Actions },
          modifier = Modifier.weight(1f),
        )
      }

      Column(
        Modifier
          .weight(1f)
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
      ) {
        when (tab) {
          BuildMenuTab.Buildings -> BuildingsMenuContent(output, onSelectBuilding)
          BuildMenuTab.Actions -> ActionsMenuContent(output, onQueueUnlock)
        }
      }
    }
  }
}

private enum class BuildMenuTab { Buildings, Actions }

@Composable
private fun BuildMenuTabButton(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  VnOutlinedButton(
    onClick = onClick,
    selected = selected,
    surface = VnButtonSurface.Dark,
    modifier = modifier,
  ) {
    Text(
      text = label,
      fontSize = 13.sp,
      color = if (selected) Accent else Color.White,
    )
  }
}

@Composable
private fun BuildingsMenuContent(
  output: EngineOutput.ShowAcademyHub,
  onSelectBuilding: (buildingId: String, currentlySelected: Boolean) -> Unit,
) {
  SkikoSafeText(
    text = "Строительство (1× в день)",
    fontSize = 12.sp,
    color = Color(0xBBFFFFFF),
    modifier = Modifier.padding(bottom = 8.dp),
  )
  output.buildingGroups.forEach { group ->
    SkikoSafeText(
      group.label,
      fontSize = 11.sp,
      color = Color(0x99FFFFFF),
      modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
    )
    group.buildings.forEach { building ->
      if (building.isBuilt && !building.enabled) {
        BuiltBuildingRow(building)
      } else {
        VnOutlinedButton(
          onClick = {
            if (building.enabled) {
              onSelectBuilding(building.id, building.selected)
            }
          },
          enabled = building.enabled,
          selected = building.selected,
          surface = VnButtonSurface.Dark,
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        ) {
          BuildingRowContent(building)
        }
      }
    }
  }
}

@Composable
private fun ActionsMenuContent(
  output: EngineOutput.ShowAcademyHub,
  onQueueUnlock: (unlockId: String?) -> Unit,
) {
  SkikoSafeText(
    text = "Разблокировка на следующий день",
    fontSize = 12.sp,
    color = Color(0xBBFFFFFF),
    modifier = Modifier.padding(bottom = 8.dp),
  )

  if (output.unlockableActions.isEmpty()) {
    SkikoSafeText("Пока нет доступных режимов", fontSize = 11.sp, color = Color(0x99FFFFFF))
    return
  }

  output.unlockableActions.forEach { unlock ->
    val canTap = unlock.status == EngineOutput.AcademyUnlockableStatus.CAN_QUEUE ||
      unlock.status == EngineOutput.AcademyUnlockableStatus.PENDING
    VnOutlinedButton(
      onClick = { if (canTap) onQueueUnlock(unlock.id) },
      enabled = canTap,
      selected = unlock.status == EngineOutput.AcademyUnlockableStatus.PENDING,
      surface = VnButtonSurface.Dark,
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 3.dp)
        .then(
          if (unlock.status == EngineOutput.AcademyUnlockableStatus.ACTIVE) {
            Modifier.border(1.dp, UnlockAccent, RoundedCornerShape(8.dp))
          } else {
            Modifier
          }
        ),
    ) {
      Column(Modifier.fillMaxWidth()) {
        Text(
          text = unlock.label,
          fontSize = 12.sp,
          color = when (unlock.status) {
            EngineOutput.AcademyUnlockableStatus.ACTIVE -> UnlockAccent
            EngineOutput.AcademyUnlockableStatus.PENDING -> Accent
            EngineOutput.AcademyUnlockableStatus.CAN_QUEUE -> Color.White
            EngineOutput.AcademyUnlockableStatus.LOCKED -> Color(0x88FFFFFF)
          },
        )
        unlock.lockedReason?.let {
          Text(it, fontSize = 10.sp, color = Color(0x99FFFFFF))
        }
      }
    }
  }
}

@Composable
private fun ResourcesAndBuildColumn(
  output: EngineOutput.ShowAcademyHub,
  modifier: Modifier = Modifier,
  onOpenBuildMenu: () -> Unit,
  onCommitDay: () -> Unit,
) {
  Column(
    modifier
      .fillMaxHeight()
      .background(Color(0xFF1C2838), RoundedCornerShape(10.dp))
      .border(1.dp, Divider, RoundedCornerShape(10.dp))
      .padding(8.dp)
      .verticalScroll(rememberScrollState()),
  ) {
    SkikoSafeText("Ресурсы", fontSize = 15.sp, color = Accent, modifier = Modifier.padding(bottom = 6.dp))
    StatLine("В наличии", output.resources.toString())

    output.selectedBuildingId?.let { selectedId ->
      val label = output.buildingGroups
        .flatMap { it.buildings }
        .firstOrNull { it.id == selectedId }
        ?.label
      if (label != null) {
        SkikoSafeText(
          text = "На стройку: $label",
          fontSize = 11.sp,
          color = Color(0xFFC8E6C9),
          modifier = Modifier.padding(top = 6.dp),
        )
      }
    }

    output.pendingUnlockLabel?.let { label ->
      SkikoSafeText(
        text = "С завтра: $label",
        fontSize = 11.sp,
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
      Text("Построить", fontSize = 13.sp, color = Color.White)
    }

    output.commitBlockedReason?.let {
      SkikoSafeText(it, fontSize = 11.sp, color = Color(0xFFFFAB91), modifier = Modifier.padding(top = 8.dp))
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
        fontSize = 13.sp,
        color = if (output.canCommit) Accent else Color(0xFF6B7588),
      )
    }
  }
}

@Composable
private fun BuiltBuildingRow(building: EngineOutput.AcademyBuildingUi) {
  Column(
    Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp)
      .background(Color(0xFF2E4A3A), RoundedCornerShape(8.dp))
      .border(1.dp, Color(0xFF66BB6A), RoundedCornerShape(8.dp))
      .padding(horizontal = 10.dp, vertical = 8.dp),
  ) {
    BuildingRowContent(building, builtHighlight = true)
  }
}

@Composable
private fun BuildingRowContent(
  building: EngineOutput.AcademyBuildingUi,
  builtHighlight: Boolean = false,
) {
  Column(Modifier.fillMaxWidth()) {
    Text(
      text = building.label,
      fontSize = 12.sp,
      color = when {
        building.selected -> Accent
        builtHighlight || building.isBuilt -> Color(0xFFC8E6C9)
        else -> Color.White
      },
    )
    Text(
      text = buildString {
        append(building.statusLabel)
        building.buildCost?.let { append(" · $it рес.") }
      },
      fontSize = 10.sp,
      color = Color(0xBBFFFFFF),
    )
    building.lockedReason?.let {
      Text(it, fontSize = 9.sp, color = Color(0x99FFFFFF))
    }
  }
}

@Composable
private fun StatLine(label: String, value: String) {
  Row(
    Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(label, fontSize = 11.sp, color = Color(0xCCFFFFFF))
    Text(value, fontSize = 11.sp, color = Color.White)
  }
}

private fun phaseZoneColor(phaseId: String): Color = when (phaseId) {
  "morning" -> Color(0xFF243044)
  "day" -> Color(0xFF2A3A4F)
  "evening" -> Color(0xFF352838)
  "night" -> Color(0xFF1A2230)
  else -> Color(0xFF1E2836)
}
