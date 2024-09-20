package com.example.cloudvibe.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    // weather table
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

    @Query("SELECT * FROM weather WHERE timestamp = :timestamp LIMIT 1")
    suspend fun getWeatherByTimestamp(timestamp: Long): WeatherEntity?

    @Query("SELECT * FROM weather ORDER BY timestamp DESC")
    fun getAllWeather(): Flow<List<WeatherEntity>>


    // forecast data table
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecast(forecastData: List<ForecastData>)

    @Query("DELETE FROM forecast_data")
    suspend fun clearForecasts()

    @Query("SELECT * FROM forecast_data")
    fun getAllForecasts(): Flow<List<ForecastData>>
}


