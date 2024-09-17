package com.example.cloudvibe.model.repository

import com.example.cloudvibe.model.network.WeatherApi
import com.example.cloudvibe.model.network.data.CurrentWeatherResponse
import com.example.cloudvibe.model.network.data.FiveDayForecastResponse
import com.example.cloudvibe.model.network.data.HourlyForecastResponse
import com.example.cloudvibe.model.network.data.LocationResponse

class WeatherRepository(private val api: WeatherApi) {

    suspend fun getCurrentWeather(lat: Double, lon: Double, apiKey: String): CurrentWeatherResponse? {
        val response = api.getCurrentWeather(lat, lon, apiKey)
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }

    suspend fun getHourlyForecast(lat: Double, lon: Double, apiKey: String): HourlyForecastResponse? {
        val response = api.getHourlyForecast(lat, lon, apiKey)
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }

    suspend fun getFiveDayForecast(lat: Double, lon: Double, apiKey: String): FiveDayForecastResponse? {
        val response = api.getFiveDayForecast(lat, lon, apiKey)
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }

    suspend fun getLocationByName(locationName: String, apiKey: String): List<LocationResponse>? {
        val response = api.getLocation(locationName, apiKey)
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }
}