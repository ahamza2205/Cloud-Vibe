package com.example.cloudvibe.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cloudvibe.model.database.FavoriteCity
import com.example.cloudvibe.model.database.WeatherEntity

class SharedViewModel : ViewModel() {
    private val _selectedLocation = MutableLiveData<Pair<Double, Double>>()
    val selectedLocation: LiveData<Pair<Double, Double>> = _selectedLocation

    //details location
    val detailsLocation = MutableLiveData<FavoriteCity>()

    fun setSelectedLocation(latitude: Double, longitude: Double) {
        _selectedLocation.value = Pair(latitude, longitude)
    }
}