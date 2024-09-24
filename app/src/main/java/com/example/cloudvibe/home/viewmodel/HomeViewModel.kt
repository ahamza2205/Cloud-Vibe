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
    fun getUnitsSymbol(): String {
        return when (getUnits()) {
            "imperial" -> "°F"
            "kelvin" -> "K"
            else -> "°C"
        }
    }

    fun updateSettings() {
        _tempUnit.value = repository.getUnits()
        }
}
