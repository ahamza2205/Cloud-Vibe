package com.example.cloudvibe.model.network.data

data class FiveDayForecastResponse(
    val city: City,
    val list: List<Forecast>
)

data class City(val name: String, val country: String)
data class Forecast(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val dt_txt: String
)
