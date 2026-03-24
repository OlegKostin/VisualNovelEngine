package com.olegkos.coredi

import com.olegkos.save.DesktopSaveStorage
import com.olegkos.save.SaveManager
import com.olegkos.save.SaveStorage
import com.olegkos.virtualnovelapp.GameViewModel
import com.olegkos.vnengine.GameLoading.AssetReader
import com.olegkos.vnengine.GameLoading.DiceRoller
import com.olegkos.vnengine.GameLoading.JsonScenarioParser
import com.olegkos.vnengine.GameLoading.RandomDiceRoller
import com.olegkos.vnengine.GameLoading.ScenarioParser
import com.olegkos.vnengine.game.GameLoader
import com.olegkos.vnengine.DesktopAssetReader
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

actual val platformModule = module {

  single<SaveStorage> {
    DesktopSaveStorage()
  }

  single {
    SaveManager(get())
  }

  single<AssetReader> {
    DesktopAssetReader()
  }

  single<ScenarioParser> {
    JsonScenarioParser()
  }

  single {
    GameLoader(
      assetReader = get(),
      parser = get()
    )
  }

  single<DiceRoller> { RandomDiceRoller() }

  viewModel {
    GameViewModel(
      loader = get(),
      get(),
      dice = get(),
      assetReader = get(),
      saveManager = get()
    )
  }}