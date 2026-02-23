package com.olegkos.androidapp

import android.app.Application
import com.olegkos.coredi.platformModule
import org.koin.core.context.startKoin


class Application : Application() {
  override fun onCreate() {
    super.onCreate()
    startKoin { modules(platformModule) }
  }
}