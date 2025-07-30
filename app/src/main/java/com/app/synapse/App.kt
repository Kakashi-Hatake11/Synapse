package com.app.synapse

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
// import timber.log.Timber // Uncomment if you add Timber for logging

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize libraries here if needed.
        // For example, Timber for logging in debug builds:
        // if (BuildConfig.DEBUG) { // Make sure BuildConfig is available
        //     Timber.plant(Timber.DebugTree())
        // }

        // Example: Initialize ThreeTenABP for date/time backport if you add it
        // import com.jakewharton.threetenabp.AndroidThreeTen
        // AndroidThreeTen.init(this)
    }
}
