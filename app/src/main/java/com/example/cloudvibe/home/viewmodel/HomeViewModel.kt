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
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

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
            Log.d("HomeViewModel", "Fetching forecast data for lat: $lat, lon: $lon")
            repository.fetchForecastFromApiAndSave(lat, lon, language).collect { forecastList ->
                Log.d("HomeViewModel", "Forecast data received: $forecastList")
                _savedForecast.value = forecastList
            }
        }
    }

    fun saveLocation(latitude: Double, longitude: Double) {
        Log.d("HomeViewModel", "Saving location lat: $latitude, lon: $longitude")
        repository.saveLocation(latitude, longitude)
    }

    fun getUnits(): String {
        val units = repository.getUnits() ?: "metric"
        Log.d("HomeViewModel", "Temperature unit: $units")
        return units
    }

    fun getWindSpeedUnit(): String {
        val windSpeedUnit = repository.getWindSpeedUnit() ?: "km/h"
        Log.d("HomeViewModel", "Wind speed unit: $windSpeedUnit")
        return windSpeedUnit
    }

    fun updateSettings() {
        _tempUnit.value = repository.getUnits()
        Log.d("HomeViewModel", "Temperature unit updated to: ${_tempUnit.value}")
    }

    fun getLocation(): Pair<Double, Double>? {
        val location = repository.getLocation()
        Log.d("HomeViewModel", "Retrieved location: $location")
        return location
    }

    fun getCoordinatesFromCityName(cityName: String, onCoordinatesFetched: (Double, Double) -> Unit) {
        val url = "https://nominatim.openstreetmap.org/search?q=$cityName&format=json&addressdetails=1"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        Log.d("HomeViewModel", "Fetching coordinates for city: $cityName")

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("HomeViewModel", "Error fetching coordinates: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    Log.d("HomeViewModel", "Response data for coordinates: $responseData")
                    val jsonArray = JSONArray(responseData)
                    if (jsonArray.length() > 0) {
                        val lat = jsonArray.getJSONObject(0).getDouble("lat")
                        val lon = jsonArray.getJSONObject(0).getDouble("lon")
                        Log.d("HomeViewModel", "Coordinates for city $cityName: lat: $lat, lon: $lon")
                        onCoordinatesFetched(lat, lon)
                    } else {
                        Log.d("HomeViewModel", "No results found for city: $cityName")
                    }
                } else {
                    Log.e("HomeViewModel", "Failed to fetch coordinates for city: $cityName")
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

