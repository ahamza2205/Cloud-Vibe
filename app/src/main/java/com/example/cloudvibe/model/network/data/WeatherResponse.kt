package com.example.cloudvibe.model.network.data


import androidx.room.Entity
import androidx.room.TypeConverters
import com.example.cloudvibe.model.database.Converters
import com.google.gson.annotations.SerializedName
@TypeConverters(Converters::class)
@Entity
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
    @SerializedName("hourly") val hourly: List<Hourly>? = null,
)
@TypeConverters(Converters::class)
@Entity
data class Coord(
    val lon: Double,
    val lat: Double
)
@TypeConverters(Converters::class)
@Entity
data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String,

) {
    constructor(icon: String, description: String) : this(  id = 0, main = "", description = description, icon = icon)
}
@TypeConverters(Converters::class)
@Entity
data class Main(
    val temp: Float,
    val feels_like: Double,
    val temp_min: Float,
    val temp_max: Float,
    val pressure: Int,
    val humidity: Int,
    val sea_level: Int,
    val grnd_level: Int
)
@TypeConverters(Converters::class)
@Entity
data class Wind(
    val speed: Double,
    val deg: Int,
    val gust: Double
)
@TypeConverters(Converters::class)
@Entity
data class Rain(
    @SerializedName("1h") val oneHour: Double?
)
@TypeConverters(Converters::class)
@Entity
data class Clouds(
    val all: Int
)
@TypeConverters(Converters::class)
@Entity
data class Sys(
    val type: Int,
    val id: Int,
    val country: String,
    val sunrise: Long,
    val sunset: Long,
    val pod: String // Example field, adjust based on your actual Sys class structure

)