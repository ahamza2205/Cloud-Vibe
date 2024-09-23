package com.example.cloudvibe.model.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.cloudvibe.model.network.data.Clouds
import com.example.cloudvibe.model.network.data.Hourly
import com.example.cloudvibe.model.network.data.Main
import com.example.cloudvibe.model.network.data.Sys
import com.example.cloudvibe.model.network.data.Weather
import com.example.cloudvibe.model.network.data.Wind

@TypeConverters(Converters::class)
@Entity(tableName = "forecast_data")
data class ForecastData(
    @PrimaryKey val dt: Long,
    val maxTemp: Float,
    val minTemp: Float,
    val date: String,
    val time: String,
    val weather: List<Weather>

)

fun ForecastData.toHourly(): Hourly {
    return Hourly(
        dt = this.dt,
        temp = (this.maxTemp + this.minTemp) / 2,
        weather = this.weather
    )
}



