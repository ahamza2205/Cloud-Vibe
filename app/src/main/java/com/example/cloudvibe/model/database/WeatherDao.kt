package com.example.cloudvibe.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cloudvibe.model.network.data.ForecastItem
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

    @Query("SELECT * FROM weather WHERE timestamp = :timestamp LIMIT 1")
    suspend fun getWeatherByTimestamp(timestamp: Long): WeatherEntity?

    @Query("SELECT * FROM weather ORDER BY timestamp DESC")
    fun getAllWeather(): Flow<List<WeatherEntity>>

    @Query("SELECT * FROM forecast_item")
    suspend fun getAllForecasts(): List<ForecastItem>

    @Query("DELETE FROM forecast_item")
    suspend fun clearForecasts()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecast(forecasts: List<ForecastItem>)
}
