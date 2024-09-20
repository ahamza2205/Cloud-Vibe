
package com.example.cloudvibe.model.repository

import android.util.Log
import com.example.cloudvibe.model.database.ForecastData
import com.example.cloudvibe.model.database.WeatherDao
import com.example.cloudvibe.model.database.WeatherEntity
import com.example.cloudvibe.model.network.WeatherApiService
import com.example.cloudvibe.utils.WeatherMapper.mapForecastResponseToData
import com.example.cloudvibe.utils.WeatherMapper.mapWeatherResponseToEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

class WeatherRepository(
    private val weatherApiService: WeatherApiService,
    private val weatherDao: WeatherDao
) {
    private val TAG = "WeatherRepository"

    // Weather data
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getWeatherFromApiAndSaveToLocal(lat: Double, lon: Double, units: String ,language: String,apiKey: String) : Flow<List<WeatherEntity>> {
        return flow {
            try {
                Log.d(TAG, "Fetching current weather and saving to local for lat: $lat, lon: $lon")
                val response = weatherApiService.getCurrentWeather(lat, lon,units,language , apiKey)
                Log.d(TAG, "Received weather response for saving: $response")
                val weatherEntity = mapWeatherResponseToEntity(response)
                weatherDao.insertWeather(weatherEntity)
                Log.d(TAG, "Weather data saved to local database: $weatherEntity")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching weather for saving", e)
            }
            // Emit the saved weather data from the local database after saving
            emitAll(getSavedWeather())
        }.flatMapLatest {
            getSavedWeather()
        }
    }

    fun getSavedWeather(): Flow<List<WeatherEntity>> {
        Log.d(TAG, "Fetching saved weather from local database")
        return weatherDao.getAllWeather()
    }

    // Forecast data
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun fetchForecastFromApiAndSave(lat: Double, lon: Double, units: String ,language: String,apiKey: String): Flow<List<ForecastData>> {
        return flow {
            Log.d(TAG, "Fetching forecast weather from API for lat: $lat, lon: $lon")
            try {
                val response = weatherApiService.getForecastWeather(lat, lon,units,language , apiKey)
                response.body()?.let { forecastResponse ->
                    val forecastList = mapForecastResponseToData(forecastResponse)
                    Log.d(TAG, "Received forecast response: $forecastResponse")
                    weatherDao.clearForecasts()  // Delete old data from Room
                    Log.d(TAG, "Cleared old forecasts from local database")
                    weatherDao.insertForecast(forecastList)  // Add new data to Room
                    Log.d(TAG, "Forecast data saved to local database: $forecastList")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching forecast weather from API", e)
            }
            // Emit the saved forecast data from the local database after saving
            emitAll(getSavedForecast())
        }.flatMapLatest {
            getSavedForecast()
        }
    }

    fun getSavedForecast(): Flow<List<ForecastData>> {
        Log.d(TAG, "Fetching saved forecast from local database")
        return weatherDao.getAllForecasts()
    }
}
