package com.example.cloudvibe.sharedpreferences

import android.content.Context
import android.util.Log
import javax.inject.Inject

class SharedPreferencesHelper @Inject constructor(context: Context) {

    private val preferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    fun saveLocation(latitude: Double, longitude: Double) {
        with(preferences.edit()) {
            putFloat("latitude", latitude.toFloat())
            putFloat("longitude", longitude.toFloat())
            apply()

            // Log the saved values
            Log.d("SharedPreferences", "Location saved: Latitude = $latitude, Longitude = $longitude")
        }
    }
    fun getLocation(): Pair<Double, Double>? {
        val latitude = preferences.getFloat("latitude", Float.NaN)
        val longitude = preferences.getFloat("longitude", Float.NaN)

        // Log the fetched values
        Log.d("SharedPreferences", "Location fetched: Latitude = $latitude, Longitude = $longitude")

        return if (!latitude.isNaN() && !longitude.isNaN()) {
            Pair(latitude.toDouble(), longitude.toDouble())
        } else {
            Log.e("SharedPreferences", "Location not found, returning null")
            null
        }
    }
    fun saveLanguage(languageCode: String) {
        val editor = preferences.edit()
        editor.putString("language", languageCode)
        editor.apply()
    }
    fun getLanguage(): String? {
        return preferences.getString("language", "en") // Default to English
    }
    fun saveUnits(unit: String) {
        preferences.edit().putString("unit", unit).apply()
    }
    fun getUnits(): String? {
        return preferences.getString("unit", "°C") // Default to Celsius (metric)
    }
    fun saveWindSpeedUnit(unit: String) {
        preferences.edit().putString("wind_speed_unit", unit).apply()
    }
    fun getWindSpeedUnit(): String? {
        return preferences.getString("wind_speed_unit", "km/h") // Default to km/h
    }
    fun getLocationSetting(): String {
        return preferences.getString("location_setting", "gps") ?: "gps" // Default to GPS
    }
    fun saveLocationSetting(setting: String) {
        preferences.edit().putString("location_setting", setting).apply()
    }

}
