package com.olegkos.coredi

import com.olegkos.save.DesktopSaveStorage
import com.olegkos.save.SaveManager
import com.olegkos.save.SaveStorage
import com.olegkos.save.metaStorage.MetaManager
import com.olegkos.save.metaStorage.MetaStorage
import com.olegkos.virtualnovelapp.GameController
import com.olegkos.virtualnovelapp.GameViewModel
import com.olegkos.vnengine.DesktopAssetReader
import com.olegkos.vnengine.GameLoading.AssetReader
import com.olegkos.vnengine.GameLoading.DiceRoller
import com.olegkos.vnengine.GameLoading.JsonScenarioParser
import com.olegkos.vnengine.GameLoading.RandomDiceRoller
import com.olegkos.vnengine.GameLoading.ScenarioParser
import com.olegkos.vnengine.engine.cards.CardData
import com.olegkos.vnengine.engine.cards.CardManager
import com.olegkos.vnengine.game.GameLoader
import kotlinx.coroutines.Dispatchers
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
  single {
    GameController(
      loader = get(),
      parser = get(),
      dice = get(),
      assetReader = get(),
      saveManager = get(),
      cardManager = get(),
      metaManager = get(),
      ioDispatcher = Dispatchers.Default
    )
  }
  single {
    MetaStorage()
  }

  single {
    MetaManager(get())
  }
  single {
    CardManager( cards = get())
  }
  single<List<CardData>> {
    listOf(

    )
  }
  viewModel {
    GameViewModel(
      controller = get(),
      saveManager = get()
    )
  }
}