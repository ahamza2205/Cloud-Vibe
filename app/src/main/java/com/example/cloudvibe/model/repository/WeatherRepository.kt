package com.example.cloudvibe.model.repository

import android.util.Log
import com.example.cloudvibe.model.database.WeatherDao
import com.example.cloudvibe.model.database.WeatherEntity
import com.example.cloudvibe.model.network.WeatherApiService
import com.example.cloudvibe.model.network.data.ForecastItem
import com.example.cloudvibe.model.network.data.WeatherResponse
import kotlinx.coroutines.flow.Flow

class WeatherRepository(
    private val weatherApiService: WeatherApiService,
    private val weatherDao: WeatherDao

) {

    private val TAG = "WeatherRepository"

    suspend fun getCurrentWeather(lat: Double, lon: Double, apiKey: String): Result<WeatherEntity> {
        Log.d(TAG, "Fetching weather for lat: $lat, lon: $lon")

        return try {
            val response = weatherApiService.getCurrentWeather(lat, lon, apiKey)

            // Log response data
            Log.d(TAG, "Weather API response: $response")

            // Map response to WeatherEntity
            val weatherEntity = mapWeatherResponseToEntity(response)

            // Log the weather entity being saved
            Log.d(TAG, "WeatherEntity to be saved: $weatherEntity")

            weatherDao.insertWeather(weatherEntity)
            Result.success(weatherEntity)
        } catch (e: Exception) {
            // Log exception details
            Log.e(TAG, "Error fetching weather", e)
            Result.failure(e)
        }
    }

    fun getSavedWeather(): Flow<List<WeatherEntity>> {
        Log.d(TAG, "Fetching saved weather data")
        return weatherDao.getAllWeather()
    }


    suspend fun fetchAndSaveWeather(lat: Double, lon: Double, apiKey: String) {
        if (apiKey.isBlank()) {
            Log.e("WeatherRepository", "API key is blank or null")
            return
        }

        try {
            val response = weatherApiService.getForecastWeather(lat, lon, apiKey)
            if (response.isSuccessful) {
                response.body()?.let { forecastResponse ->
                    Log.d(
                        "WeatherRepository",
                        "API call successful: $forecastResponse"
                    ) // Success log

                    val forecastList = forecastResponse.list
                    if (forecastList.isNotEmpty()) {
                        weatherDao.clearForecasts()
                        weatherDao.insertForecast(forecastList)
                        Log.d(
                            "WeatherRepository",
                            "Forecast list saved successfully to local database"
                        )
                    } else {
                        Log.e("WeatherRepository", "Forecast list is empty")
                    }
                } ?: Log.e("WeatherRepository", "Response body is null")
            } else {
                Log.e(
                    "WeatherRepository",
                    "API call failed: ${response.errorBody()?.string()}"
                ) // Failure log
            }
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Exception occurred: ${e.message}", e) // Exception log
        }
    }

    suspend fun getLocalForecasts(): List<ForecastItem> {
        val forecasts = weatherDao.getAllForecasts()
        if (forecasts.isNotEmpty()) {
            Log.d(
                "WeatherRepository",
                "Local forecasts retrieved successfully: $forecasts"
            ) // Success log
        } else {
            Log.e("WeatherRepository", "No local forecasts available") // Failure log
        }
        return forecasts
    }

    fun mapWeatherResponseToEntity(response: WeatherResponse): WeatherEntity {
        return WeatherEntity(
            locationName = response.name,
            country = response.sys.country,
            localtime = response.dt.toString(), // Convert timestamp to local time if needed
            temperature = response.main.temp,
            description = response.weather.firstOrNull()?.description ?: "",
            windSpeed = response.wind.speed,
            humidity = response.main.humidity,
            timestamp = response.dt,
            pressure = response.main.pressure,
            sunrise = response.sys.sunrise,
            sunset = response.sys.sunset,
            hourlyData = response.hourly
        )
    }
}