package com.example.cloudvibe.model.network.data

data class HourlyForecastResponse(
    val lat: Double,
    val lon: Double,
    val hourly: List<HourlyWeather>
):Any()

data class HourlyWeather(
    val dt: Long,
    val temp: Double,
    val weather: List<Weather>,
    val wind_speed: Double
)
