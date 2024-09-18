package com.example.cloudvibe.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.cloudvibe.model.network.data.Hourly

@TypeConverters(Converters::class)
@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val locationName: String,
    val country: String,
    val localtime: String,
    val temperature: Float,
    val description: String,
    val windSpeed: Double,
    val humidity: Int,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    val pressure: Int,
    val sunrise: Long,
    val sunset: Long,
    @ColumnInfo(name = "hourly_data") val hourlyData: List<Hourly>?
)
