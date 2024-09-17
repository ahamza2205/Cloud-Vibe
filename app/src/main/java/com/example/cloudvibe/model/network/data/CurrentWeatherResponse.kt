package com.example.cloudvibe.model.network.data

data class CurrentWeatherResponse(
    val coord: Coord,
    val weather: List<Weather>,
    val main: Main,
    val wind: Wind,
    val dt: Long,
    val sys: Sys,
    val name: String
)

data class Coord(val lon: Double, val lat: Double)
data class Weather(val id: Int, val main: String, val description: String, val icon: String)
data class Main(val temp: Double, val pressure: Int, val humidity: Int)
data class Wind(val speed: Double, val deg: Int)
data class Sys(val country: String, val sunrise: Long, val sunset: Long)