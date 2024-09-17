package com.example.cloudvibe.model.network

import com.example.cloudvibe.model.network.data.CurrentWeatherResponse
import com.example.cloudvibe.model.network.data.FiveDayForecastResponse
import com.example.cloudvibe.model.network.data.HourlyForecastResponse
import com.example.cloudvibe.model.network.data.LocationResponse
import com.example.cloudvibe.model.network.data.MapWeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): Response<CurrentWeatherResponse>

    @GET("forecast/hourly")
    suspend fun getHourlyForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): Response<HourlyForecastResponse>

    @GET("forecast")
    suspend fun getFiveDayForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): Response<FiveDayForecastResponse>

    @GET("map")
    suspend fun getMapWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): Response<MapWeatherResponse>

    @GET("geocoding")
    suspend fun getLocation(
        @Query("q") query: String,
        @Query("appid") apiKey: String
    ): Response<List<LocationResponse>>
}
