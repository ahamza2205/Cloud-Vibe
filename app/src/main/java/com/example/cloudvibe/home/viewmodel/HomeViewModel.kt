package com.example.cloudvibe.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudvibe.model.database.ForecastData
import com.example.cloudvibe.model.database.WeatherEntity
import com.example.cloudvibe.model.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _savedWeather = MutableStateFlow<List<WeatherEntity>>(emptyList())
    val savedWeather: StateFlow<List<WeatherEntity>> = _savedWeather

    private val _savedForecast = MutableStateFlow<List<ForecastData>>(emptyList())
    val savedForecast: StateFlow<List<ForecastData>> = _savedForecast

    fun fetchAndDisplayWeather(lat: Double, lon: Double, apiKey: String) {
        viewModelScope.launch {
            repository.getWeatherFromApiAndSaveToLocal(lat, lon, apiKey).collect { weatherList ->
                _savedWeather.value = weatherList
            }
        }
    }

    fun fetchAndDisplayForecast(lat: Double, lon: Double, apiKey: String) {
        viewModelScope.launch {
            repository.fetchForecastFromApiAndSave(lat, lon, apiKey).collect { forecastList ->
                _savedForecast.value = forecastList
            }
        }
    }
}
