package com.example.cloudvibe.sharedpreferences

import android.content.Context
import javax.inject.Inject

class SharedPreferencesHelper @Inject constructor(context: Context) {

    private val preferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    fun saveLocation(latitude: Double, longitude: Double) {
        with(preferences.edit()) {
            putFloat("latitude", latitude.toFloat())
            putFloat("longitude", longitude.toFloat())
            apply()
        }
    }

    fun getLocation(): Pair<Double, Double>? {
        val latitude = preferences.getFloat("latitude", Float.NaN)
        val longitude = preferences.getFloat("longitude", Float.NaN)
        return if (!latitude.isNaN() && !longitude.isNaN()) {
            Pair(latitude.toDouble(), longitude.toDouble())
        } else {
            null
        }
    }

    fun saveLanguage(language: String) {
        preferences.edit().putString("language", language).apply()
    }

    fun getLanguage(): String? {
        return preferences.getString("language", "en")
    }

    fun saveUnits(unit: String) {
        preferences.edit().putString("unit", unit).apply()
    }

    fun getUnits(): String? {
        return preferences.getString("unit", "C") // Default to Celsius (metric)
    }

    fun saveWindSpeedUnit(unit: String) {
        preferences.edit().putString("wind_speed_unit", unit).apply()
    }

    fun getWindSpeedUnit(): String? {
        return preferences.getString("wind_speed_unit", "km/h") // Default to km/h
    }
}
