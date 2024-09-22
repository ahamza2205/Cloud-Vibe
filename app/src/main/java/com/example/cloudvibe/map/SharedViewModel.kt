package com.example.cloudvibe.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _selectedLocation = MutableLiveData<Pair<Double, Double>>()
    val selectedLocation: LiveData<Pair<Double, Double>> = _selectedLocation

    fun setSelectedLocation(latitude: Double, longitude: Double) {
        _selectedLocation.value = Pair(latitude, longitude)
    }
}