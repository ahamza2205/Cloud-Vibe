package com.example.cloudvibe.model.network


import com.example.cloudvibe.model.network.data.ForecastResponse
import com.example.cloudvibe.model.network.data.WeatherResponse

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("lang") language: String = "en",
        @Query("appid") apiKey : String = "7af08d0e1d543aea9b340405ceed1c3d"
    ): WeatherResponse

    @GET("forecast")
    suspend fun getForecastWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("lang") language: String = "en",
        @Query("appid") apiKey : String = "7af08d0e1d543aea9b340405ceed1c3d"
    ): Response<ForecastResponse>
}