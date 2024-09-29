package com.example.cloudvibe.model.network.data

import androidx.room.TypeConverters
import com.example.cloudvibe.model.database.Converters
import com.google.gson.annotations.SerializedName
@TypeConverters(Converters::class)
data class WeatherResponse(
    val coord: Coord,
    val weather: List<Weather>,
    val base: String,
    val main: Main,
    val wind: Wind,
    val rain: Rain?,
    val clouds: Clouds,
    val dt: Long,
    val sys: Sys,
    val timezone: Int,
    val id: Int,
    val name: String,
    val cod: Int,
    @SerializedName("hourly") val hourly: List<Hourly>?
)
@TypeConverters(Converters::class)
data class Coord(
    val lon: Double,
    val lat: Double
)
@TypeConverters(Converters::class)
data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String,

) {
    constructor(icon: String, description: String) : this(  id = 0, main = "", description = description, icon = icon)
    constructor(id: Int, main: String, description: String) : this(  id = id, main = main, description = description, icon = "")
}
@TypeConverters(Converters::class)
data class Main(
    val temp: Float,
    val feels_like: Double,
    val temp_min: Float,
    val temp_max: Float,
    val pressure: Int,
    val humidity: Int,
    val sea_level: Int,
    val grnd_level: Int
) {
    constructor(temp_max: Float, temp_min: Float) : this( temp = 0f, feels_like = 0.0, temp_min = temp_min, temp_max = temp_max, pressure = 0, humidity = 0, sea_level = 0, grnd_level = 0)
}

@TypeConverters(Converters::class)
data class Wind(
    val speed: Double,
    val deg: Int,
    val gust: Double
) {
    constructor(speed: Double, deg: Int) : this(speed = speed, deg = deg, gust = 0.0)
}

@TypeConverters(Converters::class)
data class Rain(
    @SerializedName("1h") val oneHour: Double?
)
@TypeConverters(Converters::class)
data class Clouds(
    val all: Int
)
@TypeConverters(Converters::class)
data class Sys(
    val type: Int,
    val id: Int,
    val country: String,
    val sunrise: Long,
    val sunset: Long,
    val pod: String // Example field, adjust based on your actual Sys class structure

) {
    constructor(type: Int, id: Int, country: String, sunrise: Long, sunset: Long) : this(    type = type, id = id, country = country, sunrise = sunrise, sunset = sunset, pod = "")
}