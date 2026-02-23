package com.olegkos.coredi

import android.app.Application
import com.olegkos.virtualnovelapp.GameViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

// Модуль Koin
val androidModule = module {
  viewModel { GameViewModel(get()) }
}

// Инициализация Koin
fun initKoin(app: Application) {
  startKoin {
    androidContext(app)
    modules(androidModule,
      shareModule    )
  }
}