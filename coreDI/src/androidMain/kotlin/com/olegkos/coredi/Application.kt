package com.olegkos.coredi

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

fun initKoin(app: Application) {
  startKoin {
    androidContext(app)
    modules(androidGameModule)
  }
}