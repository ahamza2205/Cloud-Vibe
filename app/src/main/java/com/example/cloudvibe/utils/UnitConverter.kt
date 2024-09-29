package com.example.cloudvibe.utils

import android.content.Context
import com.example.cloudvibe.R
import com.example.cloudvibe.model.sharedpreferences.SharedPreferencesHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object UnitConverter {
    fun kelvinToCelsius(kelvin: Number): Float {
        return kelvin.toFloat() - 273.15f
    }

    fun kelvinToFahrenheit(kelvin: Number): Float {
        return kelvin.toFloat() * 9/5 - 459.67f
    }

    fun meterPerSecondToMilesPerHour(mps: Double): Double {
        val mph = mps * 2.23694
        return mph.round(2)
    }

    fun meterPerSecondToKilometersPerHour(mps: Double): Double {
        val kph = mps * 3.6
        return kph.round(2)
    }

    private fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }

    fun getWeatherIconResource(iconCode: String): Int {
        return when (iconCode) {
            // Clear sky
            "01d" -> R.drawable.clear_sky
            "01n" -> R.drawable.clear_sky_night

            // Few clouds
            "02d" -> R.drawable.few_cloud
            "02n" -> R.drawable.few_cloud_night

            // Scattered clouds
            "03d" -> R.drawable.cloudy
            "03n" -> R.drawable.cloudy_night

            // Broken clouds
            "04d" -> R.drawable.broken_cloud
            "04n" -> R.drawable.broken_cloud_night

            // Shower rain
            "09d" -> R.drawable.rain
            "09n" -> R.drawable.rain_night

            // Rain
            "10d" -> R.drawable.rain
            "10n" -> R.drawable.rain_night

            // Thunderstorm
            "11d" -> R.drawable.thunderstorm
            "11n" -> R.drawable.thunderstorm_night

            // Snow
            "13d" -> R.drawable.snow
            "13n" -> R.drawable.snow_night

            // Mist
            "50d" -> R.drawable.mist_night
            "50n" -> R.drawable.mist_night

            // Default
            else -> R.drawable.default_weather_icon
                }
    }
    fun parseIntegerIntoArabic(number: String, context: Context): String {
        val sharedPreferencesHelper = SharedPreferencesHelper(context)

        // Check if the language is Arabic
        if (sharedPreferencesHelper.getLanguage() == "ar") {
            return number
                .replace("1", "١")
                .replace("2", "٢")
                .replace("3", "٣")
                .replace("4", "٤")
                .replace("5", "٥")
                .replace("6", "٦")
                .replace("7", "٧")
                .replace("8", "٨")
                .replace("9", "٩")
                .replace("0", "٠")
        } else {
            // Return the number as it is for other languages
            return number
        }
    }

    fun convertTemperature(tempInCelsius: Float, unit: String): Number {
        return when (unit) {
            "°K" -> tempInCelsius + 273.15
            "°F" -> (tempInCelsius * 9 / 5) + 32
            else -> tempInCelsius
        }
    }
    fun convertWindSpeed(speedInKmH: Double, unit: String): Double {
        val convertedSpeed = when (unit) {
            "m/s" -> speedInKmH / 3.6
            "mph" -> speedInKmH * 0.621371
            else -> speedInKmH
        }
        return (Math.round(convertedSpeed * 100.0) / 100.0)
    }
     fun convertUnixTimeToTime(unixTime: Long): String {
        val date = Date(unixTime * 1000)
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }


}
