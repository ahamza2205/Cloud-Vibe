package com.example.cloudvibe.model.network.data

data class LocationResponse(
    val name: String,
    val local_names: Map<String, String>,
    val lat: Double,
    val lon: Double,
    val country: String
): Any()
