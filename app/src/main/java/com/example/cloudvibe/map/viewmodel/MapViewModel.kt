package com.example.cloudvibe.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.cloudvibe.model.database.FavoriteCity
import com.example.cloudvibe.model.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _cityCoordinates = MutableLiveData<GeoPoint>()
    val cityCoordinates: LiveData<GeoPoint> get() = _cityCoordinates

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // Insert favorite city
    fun insertFavoriteCity(favoriteCity: FavoriteCity) {
        viewModelScope.launch {
            repository.insertFavoriteCity(favoriteCity)
        }
    }

    // Get all favorite cities
    fun getAllFavoriteCities(): LiveData<List<FavoriteCity>> = repository.getAllFavoriteCities().asLiveData()

    // Search for city using Nominatim API
    fun searchForCity(cityName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val url = "https://nominatim.openstreetmap.org/search?format=json&q=$cityName"
            val request = Request.Builder().url(url).build()

            try {
                val response = OkHttpClient().newCall(request).execute() // طلب الشبكة

                if (response.isSuccessful) {
                    val json = response.body?.string()
                    val jsonArray = JSONArray(json)
                    if (jsonArray.length() > 0) {
                        val jsonObject = jsonArray.getJSONObject(0)
                        val lat = jsonObject.getString("lat").toDouble()
                        val lon = jsonObject.getString("lon").toDouble()
                        val geoPoint = GeoPoint(lat, lon)
                        _cityCoordinates.postValue(geoPoint) // تحديث LiveData على Main Thread
                    } else {
                        _error.postValue("City not found")
                    }
                } else {
                    _error.postValue("Failed to search for city")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}") // التعامل مع الاستثناءات
            }
        }
    }

}
