package com.example.cloudvibe.model.network.data

import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.cloudvibe.model.database.Converters

data class ForecastResponse(
    val dt: Int,
    val city: City,
    val list: List<ForecastItem>
)

data class City(
    val id: Int,
    val name: String,
    val coord: Coord,
    val country: String,
    val population: Int,
    val timezone: Int,
    val sunrise: Long,
    val sunset: Long
)
@TypeConverters(Converters::class)
data class ForecastItem(
    @PrimaryKey val dt: Long,
    @TypeConverters(Converters::class) val main: Main,
    @TypeConverters(Converters::class) val weather: List<Weather>,
    val clouds: Clouds,
    val wind: Wind,
    val visibility: Int,
    val pop: Double,
    @TypeConverters(Converters::class) val sys: Sys, // Add the converter for Sys
    val dt_txt: String // Ensure this field is non-null or handle it accordingly
)
data class Hourly(
    val dt: Long,
    val temp: Float?,
    val weather: List<Weather>,
)
