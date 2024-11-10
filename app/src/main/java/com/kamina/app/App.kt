package com.kamina.app

import android.app.Application
import io.github.edsuns.adfilter.AdFilter
import io.github.edsuns.adfilter.BuildConfig
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize AdFilter
        AdFilter.create(this)
    }
}
