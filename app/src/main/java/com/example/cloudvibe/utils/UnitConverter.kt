package com.example.cloudvibe.utils

import com.example.cloudvibe.R

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
}
