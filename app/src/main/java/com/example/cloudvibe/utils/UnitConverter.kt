package com.example.cloudvibe.utils

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
}
