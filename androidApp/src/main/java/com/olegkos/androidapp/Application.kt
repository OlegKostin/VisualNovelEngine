package com.olegkos.androidapp

import android.app.Application
import com.olegkos.coredi.initKoin

class Application : Application() {
  override fun onCreate() {
    super.onCreate()
    initKoin(this)
  }
}