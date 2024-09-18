package com.example.cloudvibe.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.cloudvibe.model.network.data.Hourly
import com.example.cloudvibe.model.network.data.Wind
@TypeConverters(Converters::class)
@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val temperature: Double,
    val description: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    val wind: Wind,
    val timezone: Int,
    val pressure: Int,
    val humidity: Int,
    val country: String,
    val sunrise: Long,
    val sunset: Long,
    @ColumnInfo(name = "hourly_data") val hourlyData: List<Hourly>?

    )
