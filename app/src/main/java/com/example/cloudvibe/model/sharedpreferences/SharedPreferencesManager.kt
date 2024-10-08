package com.example.cloudvibe.model.sharedpreferences

interface SharedPreferencesManager {
    fun getLocation(): Pair<Double, Double>?
    fun saveLocation(latitude: Double, longitude: Double)
    fun getLanguage(): String?
    fun getUnits(): String?
    fun getWindSpeedUnit(): String?
}