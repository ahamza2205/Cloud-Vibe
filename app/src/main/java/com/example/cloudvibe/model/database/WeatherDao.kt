package com.example.cloudvibe.model.database

import androidx.room.Dao
import androidx.room.Delete
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

    @Query("DELETE FROM weather")
    suspend fun deleteAllWeather()


    // forecast data table
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecast(forecastData: List<ForecastData>)

    @Query("DELETE FROM forecast_data")
    suspend fun clearForecasts()

    @Query("SELECT * FROM forecast_data")
    fun getAllForecasts(): Flow<List<ForecastData>>


    // Favorite Cities

        @Query("SELECT * FROM favorite_cities")
        fun getAllLocal(): Flow<List<FavoriteCity>>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insert(favCity: FavoriteCity)

        @Delete
        suspend fun delete(favCity : FavoriteCity)


    // Get all alarms
    @Query("SELECT * FROM alarm_table")
    fun getAllAlarms(): Flow<List<AlarmData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarmData: AlarmData)

    @Delete
    suspend fun deleteAlarm(alarmData: AlarmData)

    @Query("DELETE FROM alarm_table WHERE time < :currentTimeMillis")
    suspend fun deleteOldAlarms(currentTimeMillis:Long)
}





