package com.olegkos.coredi

import com.olegkos.virtualnovelapp.GameViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val androidGameModule = module {
  single { provideVnEngine() }
  viewModel { GameViewModel(get()) }
}