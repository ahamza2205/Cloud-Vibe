package com.example.cloudvibe.utils


import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.library.BuildConfig
import timber.log.Timber

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}

