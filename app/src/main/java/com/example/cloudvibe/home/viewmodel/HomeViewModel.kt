package com.example.cloudvibe.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.cloudvibe.model.repository.WeatherRepository
import com.example.cloudvibe.model.database.WeatherEntity
import com.example.cloudvibe.model.network.data.ForecastItem
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _currentWeather = MutableLiveData<Result<WeatherEntity>>()
    val currentWeather: LiveData<Result<WeatherEntity>> get() = _currentWeather

    private val _forecastWeather = MutableLiveData<List<ForecastItem>>()
    val forecastWeather: LiveData<List<ForecastItem>> get() = _forecastWeather

    val savedWeather: LiveData<List<WeatherEntity>> = repository.getSavedWeather().asLiveData()

    fun fetchCurrentWeather(lat: Double, lon: Double, apiKey: String) {
        viewModelScope.launch {
            val result = repository.getCurrentWeather(lat, lon, apiKey)
            _currentWeather.postValue(result)
        }
    }

    fun loadWeather(lat: Double, lon: Double, apiKey: String) {
        viewModelScope.launch {
            repository.fetchAndSaveWeather(lat, lon, apiKey)
            _forecastWeather.postValue(repository.getLocalForecasts())
        }
    }

    fun loadLocalForecasts() {
        viewModelScope.launch {
            try {
                val data = repository.getLocalForecasts()
                _forecastWeather.postValue(data)
            } catch (e: Exception) {
                // Handle error
                Log.e("WeatherViewModel", "Error loading local forecasts: ${e.message}", e)
            }
        }
    }
}
