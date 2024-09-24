package com.example.cloudvibe.model.repository

import android.util.Log
import com.example.cloudvibe.model.database.FavoriteCity
import com.example.cloudvibe.model.database.ForecastData
import com.example.cloudvibe.model.database.WeatherDao
import com.example.cloudvibe.model.database.WeatherEntity
import com.example.cloudvibe.model.network.WeatherApiService
import com.example.cloudvibe.sharedpreferences.SharedPreferencesHelper
import com.example.cloudvibe.sharedpreferences.SharedPreferencesManager
import com.example.cloudvibe.utils.WeatherMapper.mapForecastResponseToData
import com.example.cloudvibe.utils.WeatherMapper.mapWeatherResponseToEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApiService: WeatherApiService,
    private val weatherDao: WeatherDao,
    private val  SharedPreferencesHelper: SharedPreferencesHelper
) {
    private val TAG = "WeatherRepository"

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getWeatherFromApiAndSaveToLocal(
        lat: Double, lon: Double, language: String
    ): Flow<List<WeatherEntity>> {
        return flow {
            try {

                val response = weatherApiService.getCurrentWeather(lat, lon, language)

                val weatherEntity = mapWeatherResponseToEntity(response)

                weatherDao.deleteAllWeather()

                weatherDao.insertWeather(weatherEntity)
            } catch (e: Exception) {
            }

            emitAll(getSavedWeather())
        }.flatMapLatest {
            getSavedWeather()
        }
    }

    private fun getSavedWeather(): Flow<List<WeatherEntity>> {
        return weatherDao.getAllWeather()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun fetchForecastFromApiAndSave(lat: Double, lon: Double,  language: String): Flow<List<ForecastData>> {
        return flow {
            try {
                val response = weatherApiService.getForecastWeather(lat, lon,  language)
                response.body()?.let { forecastResponse ->
                    val forecastList = mapForecastResponseToData(forecastResponse)
                    weatherDao.clearForecasts()
                    weatherDao.insertForecast(forecastList)
                }
            } catch (e: Exception) {
            }
            emitAll(getSavedForecast())
        }.flatMapLatest {
            getSavedForecast()
        }
    }

    private fun getSavedForecast(): Flow<List<ForecastData>> {
        return weatherDao.getAllForecasts()
    }

    // Favorite cities
    suspend fun insertFavoriteCity(favoriteCity: FavoriteCity) {
        weatherDao.insert(favoriteCity)
    }

    fun getAllFavoriteCities(): Flow<List<FavoriteCity>> {
        return weatherDao.getAllLocal()
    }
    suspend fun deleteFavoriteCity(favoriteCity: FavoriteCity) {
        weatherDao.delete(favoriteCity)
    }

    // sheared preferences
    fun getLocation(): Pair<Double, Double>? {
        return SharedPreferencesHelper.getLocation()
    }

    fun saveLocation(latitude: Double, longitude: Double) {
        SharedPreferencesHelper.saveLocation(latitude, longitude)
    }
    fun getLanguage(): String? {
        return SharedPreferencesHelper.getLanguage()
    }

    fun getUnits(): String {
        return SharedPreferencesHelper.getUnits().toString()
    }

    fun getWindSpeedUnit(): String? {
        return SharedPreferencesHelper.getWindSpeedUnit()
    }
}
