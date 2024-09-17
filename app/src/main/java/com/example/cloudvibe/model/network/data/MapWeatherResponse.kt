package com.example.cloudvibe.model.network.data

data class MapWeatherResponse(
    val list: List<MapWeather>
)

data class MapWeather(
    val coord: Coord,
    val weather: List<Weather>,
    val main: Main
)
