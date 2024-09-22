package com.example.cloudvibe.home.viewmodel

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
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _savedWeather = MutableStateFlow<List<WeatherEntity>>(emptyList())
    val savedWeather: StateFlow<List<WeatherEntity>> = _savedWeather

    private val _savedForecast = MutableStateFlow<List<ForecastData>>(emptyList())
    val savedForecast: StateFlow<List<ForecastData>> = _savedForecast






    fun fetchAndDisplayWeather(lat: Double, lon: Double, units: String, language: String, apiKey: String) {
        viewModelScope.launch {
            try {
                repository.getWeatherFromApiAndSaveToLocal(lat, lon, units, language, apiKey).collect { weatherList ->
                    _savedWeather.value = weatherList
                }
            } catch (e: Exception) {
                _savedWeather.value = emptyList()
            }
        }
    }

    fun fetchAndDisplayForecast(lat: Double, lon: Double, units: String, language: String, apiKey: String) {
        viewModelScope.launch {
            repository.fetchForecastFromApiAndSave(lat, lon, units, language, apiKey).collect { forecastList ->
                _savedForecast.value = forecastList
            }
        }
    }



}
