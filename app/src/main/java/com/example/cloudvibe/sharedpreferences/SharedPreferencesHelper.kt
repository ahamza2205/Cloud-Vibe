package com.example.cloudvibe.sharedpreferences

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper (context : Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)


    // location
    // save latitude and longitude to shared preferences
    fun saveLocation(latitude: Double, longitude: Double){
         preferences.edit().apply {
             putString("latitude", latitude.toString())
             putString("longitude", longitude.toString())
             apply()
         }
    }
    // get latitude and longitude from shared preferences
    fun getLocation() : Pair<Double, Double>? {
      val lat = preferences.getString("latitude", null)
      val lon = preferences.getString("longitude", null)
      return if (lat != null && lon != null) {
          Pair(lat.toDouble(), lon.toDouble())
      } else {
             return null
         }
    }


    // units
    // save units to shared preferences
    fun saveUnits(units: String){
        preferences.edit().apply {
            putString("units", units)
            apply()
        }
    }
    // get units from shared preferences
    fun getUnits() : String {
        return preferences.getString("units", "metric")!! // default to metric
    }


    // language
    // save language to shared preferences
    fun saveLanguage(language: String){
        preferences.edit().apply {
            putString("language", language)
            apply()
        }
    }
    // get language from shared preferences
    fun getLanguage() : String {
        return preferences.getString("language", "en")!! // default to english
    }

}