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
                repository.getWeatherFromApiAndSaveToLocal(lat, lon,  language, ).collect { weatherList ->
                    _savedWeather.value = weatherList
                }
            } catch (e: Exception) {
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

    // Get saved location from repository
    fun saveLocation(latitude: Double, longitude: Double) {
        repository.saveLocation(latitude, longitude)
    }
    fun getUnits(): String {
        return repository.getUnits() ?: "metric"
    }

    fun getWindSpeedUnit(): String {
        return repository.getWindSpeedUnit() ?: "km/h"
    }


    fun updateSettings() {
        _tempUnit.value = repository.getUnits()
        }


    // Function to get coordinates (latitude, longitude) from city name
    fun getCoordinatesFromCityName(cityName: String, onCoordinatesFetched: (Double, Double) -> Unit) {
        val url = "https://nominatim.openstreetmap.org/search?q=$cityName&format=json&addressdetails=1"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        // Asynchronous network request to fetch coordinates
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("HomeViewModel", "Error fetching coordinates: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    val jsonArray = JSONArray(responseData)
                    if (jsonArray.length() > 0) {
                        val lat = jsonArray.getJSONObject(0).getDouble("lat")
                        val lon = jsonArray.getJSONObject(0).getDouble("lon")
                        // Pass the coordinates back using the callback
                        onCoordinatesFetched(lat, lon)
                    } else {
                        Log.d("HomeViewModel", "No results found for city: $cityName")
                    }
                }
            }
        })
    }
}
