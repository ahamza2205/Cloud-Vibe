package com.example.cloudvibe.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudvibe.model.database.ForecastData
import com.example.cloudvibe.model.database.WeatherEntity
import com.example.cloudvibe.model.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.IOException
import java.util.logging.Logger
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {
    constructor(repository: WeatherRepository?, logger: Logger?) : this( repository!!)

    private val _savedWeather = MutableStateFlow<List<WeatherEntity>>(emptyList())
    val savedWeather: StateFlow<List<WeatherEntity>> = _savedWeather

    private val _savedForecast = MutableStateFlow<List<ForecastData>>(emptyList())
    val savedForecast: StateFlow<List<ForecastData>> = _savedForecast

    private val _tempUnit = MutableStateFlow<String>("")
    val tempUnit: StateFlow<String> get()=_tempUnit



    fun fetchAndDisplayWeather(lat: Double, lon: Double ) {
        val language = repository.getLanguage() ?: "en"
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "Fetching weather data for lat: $lat, lon: $lon")
                repository.getWeatherFromApiAndSaveToLocal(lat, lon,  language).collect { weatherList ->
                    Log.d("HomeViewModel", "Weather data received: $weatherList")
                    _savedWeather.value = weatherList
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching weather data: ${e.message}")
                _savedWeather.value = emptyList()
            }
        }
    }

    fun fetchAndDisplayForecast(lat: Double, lon: Double ) {
        val language = repository.getLanguage() ?: "en"
        viewModelScope.launch {
            repository.fetchForecastFromApiAndSave(lat, lon, language).collect { forecastList ->
                _savedForecast.value = forecastList
            }
        }
    }

    fun saveLocation(latitude: Double, longitude: Double) {
        repository.saveLocation(latitude, longitude)
    }

    fun getUnits(): String {
        val units = repository.getUnits() ?: "metric"
        return units
    }

    fun getWindSpeedUnit(): String {
        val windSpeedUnit = repository.getWindSpeedUnit() ?: "km/h"
        return windSpeedUnit
    }

    fun updateSettings() {
        _tempUnit.value = repository.getUnits()
    }

    fun getLocation(): Pair<Double, Double>? {
        val location = repository.getLocation()
        return location
    }

    fun getCoordinatesFromCityName(cityName: String, onCoordinatesFetched: (Double, Double) -> Unit) {
        val url = "https://nominatim.openstreetmap.org/search?q=$cityName&format=json&addressdetails=1"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()


        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    val jsonArray = JSONArray(responseData)
                    if (jsonArray.length() > 0) {
                        val lat = jsonArray.getJSONObject(0).getDouble("lat")
                        val lon = jsonArray.getJSONObject(0).getDouble("lon")
                        onCoordinatesFetched(lat, lon)
                    }
                }
            }
        })
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        saveLocationToSharedPreferences(latitude, longitude)
    }

    private fun saveLocationToSharedPreferences(latitude: Double, longitude: Double) {
        repository.saveLocation(latitude, longitude)
    }
}

