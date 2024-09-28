package com.example.cloudvibe.model.repository

import android.util.Log
import com.example.cloudvibe.model.database.AlarmData
import com.example.cloudvibe.model.database.FavoriteCity
import com.example.cloudvibe.model.database.ForecastData
import com.example.cloudvibe.model.database.WeatherDao
import com.example.cloudvibe.model.database.WeatherEntity
import com.example.cloudvibe.model.network.WeatherApiService
import com.example.cloudvibe.sharedpreferences.SharedPreferencesHelper
import com.example.cloudvibe.utils.WeatherMapper.mapForecastResponseToData
import com.example.cloudvibe.utils.WeatherMapper.mapWeatherResponseToEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApiService: WeatherApiService,
    private val weatherDao: WeatherDao,
    private val  sharedPreferencesHelper: SharedPreferencesHelper
) : WeatherRepositoryInterFace {
    suspend fun getWeatherFromApiAndSaveToLocal(
        lat: Double, lon: Double, language: String
    ): Flow<List<WeatherEntity>> {
        return flow {
            try {
                val response = weatherApiService.getCurrentWeather(lat, lon, language)

                val weatherEntity = mapWeatherResponseToEntity(response)
                weatherDao.deleteAllWeather()  // مسح البيانات القديمة
                weatherDao.insertWeather(weatherEntity)  // إضافة البيانات الجديدة

                emitAll(getSavedWeather())
            } catch (e: Exception) {
                Timber.e("Error fetching weather data: ${e.localizedMessage}")
                emitAll(getSavedWeather())
            }
        }
    }

     fun getSavedWeather(): Flow<List<WeatherEntity>> {
        return weatherDao.getAllWeather() ?: flowOf(emptyList())
    }

    suspend fun fetchForecastFromApiAndSave(
        lat: Double, lon: Double, language: String
    ): Flow<List<ForecastData>> {
        return flow {
            try {
                val response = weatherApiService.getForecastWeather(lat, lon, language)
                response.body()?.let { forecastResponse ->
                    val forecastList = mapForecastResponseToData(forecastResponse)
                    weatherDao.clearForecasts()  // مسح البيانات القديمة
                    weatherDao.insertForecast(forecastList)  // إضافة البيانات الجديدة
                }

                // Emit the newly saved forecast data
                emitAll(getSavedForecast())
            } catch (e: Exception) {
                Timber.e("Error fetching forecast data: ${e.localizedMessage}")
                emitAll(getSavedForecast())
            }
        }
    }

     fun getSavedForecast(): Flow<List<ForecastData>> {
        return weatherDao.getAllForecasts() ?: flowOf(emptyList())
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
        return sharedPreferencesHelper.getLocation()
    }

    fun saveLocation(latitude: Double, longitude: Double) {
        sharedPreferencesHelper.saveLocation(latitude, longitude)
    }
    fun getLanguage(): String? {
        return sharedPreferencesHelper.getLanguage()
    }

    fun getUnits(): String {
        return sharedPreferencesHelper.getUnits().toString()
    }

    fun getWindSpeedUnit(): String? {
        return sharedPreferencesHelper.getWindSpeedUnit()
    }

    // alarms table
    override fun getAllLocalAlarms(): Flow<List<AlarmData>> {
        return weatherDao.getAllAlarms()
    }

    override suspend fun insertAlarm(alarmData: AlarmData) {
        weatherDao.insertAlarm(alarmData)
    }

    override suspend fun deleteOldAlarms(currentTimeMillis: Long) {
        weatherDao.deleteOldAlarms(currentTimeMillis)
    }

}
