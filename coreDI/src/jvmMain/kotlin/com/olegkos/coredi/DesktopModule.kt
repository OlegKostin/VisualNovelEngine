package com.olegkos.coredi

import com.olegkos.virtualnovelapp.GameViewModel
import com.olegkos.virtualnoveltesttwo.GameLoading.DevScenarioProvider
import com.olegkos.virtualnoveltesttwo.GameLoading.DiceRoller
import com.olegkos.virtualnoveltesttwo.GameLoading.RandomDiceRoller
import com.olegkos.virtualnoveltesttwo.GameLoading.ScenarioProvider
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

actual val platformModule: Module = module {

  single<ScenarioProvider> { DevScenarioProvider() }
  single<DiceRoller> { RandomDiceRoller() }

  viewModel {
    GameViewModel(
      provider = get(),
      dice = get()
    )
  }
}