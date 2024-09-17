package com.example.cloudvibe.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudvibe.model.network.data.CurrentWeatherResponse
import com.example.cloudvibe.model.network.data.FiveDayForecastResponse
import com.example.cloudvibe.model.network.data.HourlyForecastResponse
import com.example.cloudvibe.model.repository.WeatherRepository
import kotlinx.coroutines.launch

class HomeViewModel (private val repository: WeatherRepository) : ViewModel() {

    private val _currentWeather = MutableLiveData<CurrentWeatherResponse>()
    val currentWeather: LiveData<CurrentWeatherResponse> get() = _currentWeather

    private val _hourlyForecast = MutableLiveData<HourlyForecastResponse>()
    val hourlyForecast: LiveData<HourlyForecastResponse> get() = _hourlyForecast

    private val _fiveDayForecast = MutableLiveData<FiveDayForecastResponse>()
    val fiveDayForecast: LiveData<FiveDayForecastResponse> get() = _fiveDayForecast

    fun getCurrentWeather(lat: Double, lon: Double, apiKey: String) = viewModelScope.launch {
        val response = repository.getCurrentWeather(lat, lon, apiKey)
        _currentWeather.postValue(response!!)
    }

    fun getHourlyForecast(lat: Double, lon: Double, apiKey: String) = viewModelScope.launch {
        val response = repository.getHourlyForecast(lat, lon, apiKey)
        _hourlyForecast.postValue(response!!)
    }

    fun getFiveDayForecast(lat: Double, lon: Double, apiKey: String) = viewModelScope.launch {
        val response = repository.getFiveDayForecast(lat, lon, apiKey)
        _fiveDayForecast.postValue(response!!)
    }
}