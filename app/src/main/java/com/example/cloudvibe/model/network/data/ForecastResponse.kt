package com.example.cloudvibe.model.network.data

import androidx.room.Entity
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
@Entity(tableName = "forecast_item")
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
) {
    // Access the temperature from the 'main' object
    val temperature: Float
        get() = main.temp

    // Access the icon and description from the first 'weather' object (assuming the list is not empty)
    val icon: String?
        get() = weather.firstOrNull()?.icon

    val description: String?
        get() = weather.firstOrNull()?.description

    fun toHourly(): Hourly {
        return Hourly(
            dt = this.dt,
            temp = this.temperature,  // Correctly mapped to the 'temperature' from 'main'
            weather = listOf(
                Weather(
                    icon = this.icon.orEmpty(),  // Ensures it's non-null
                    description = this.description.orEmpty() // Ensures it's non-null
                )
            )
        )
    }
}
data class Hourly(
    val dt: Long,
    val temp: Float?,
    val weather: List<Weather>,
)
