package com.example.cloudvibe.model.network.data

data class CoordinatesResponse(
    val coord: Coordinates
)
data class Coordinates(
    val lat: Double,
    val lon: Double
)