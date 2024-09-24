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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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



    private val _cityList = MutableStateFlow<List<String>>(emptyList())  // قائمة المدن
    val cityList: StateFlow<List<String>> get() = _cityList.asStateFlow()

    private val _filteredCityList = MutableStateFlow<List<String>>(emptyList())  // قائمة المدن المفلترة
    val filteredCityList: StateFlow<List<String>> get() = _filteredCityList.asStateFlow()

    init {
        _cityList.value = listOf(
            "Cairo, Egypt", "Alexandria, Egypt", "Giza, Egypt", "Port Said, Egypt",
            "Suez, Egypt", "Luxor, Egypt", "Aswan, Egypt", "Asyut, Egypt",
            "Beheira, Egypt", "Beni Suef, Egypt", "Dakahlia, Egypt", "Damietta, Egypt",
            "Fayoum, Egypt", "Gharbia, Egypt", "Ismailia, Egypt", "Kafr El Sheikh, Egypt",
            "Minya, Egypt", "Monufia, Egypt", "New Valley, Egypt", "Qalyubia, Egypt",
            "Qena, Egypt", "Red Sea, Egypt", "Sharqia, Egypt", "Sohag, Egypt",
            "South Sinai, Egypt", "North Sinai, Egypt", "Matrouh, Egypt","London, UK", "Paris, France", "New York, USA", "Berlin, Germany",
            "Tokyo, Japan", "Sydney, Australia", "Rome, Italy", "Madrid, Spain",
            "Moscow, Russia", "Toronto, Canada", "Dubai, UAE", "Mumbai, India",
            "Beijing, China", "Bangkok, Thailand", "Istanbul, Turkey", "Seoul, South Korea",
            "Mexico City, Mexico", "Sao Paulo, Brazil", "Buenos Aires, Argentina", "Cape Town, South Africa",
            "Los Angeles, USA", "Chicago, USA", "San Francisco, USA", "Miami, USA",
            "Las Vegas, USA", "Boston, USA", "Houston, USA", "Dallas, USA",
            "Philadelphia, USA", "Atlanta, USA", "Seattle, USA", "Denver, USA",
            "Phoenix, USA", "San Diego, USA", "Orlando, USA", "Austin, USA",
            "Brisbane, Australia", "Melbourne, Australia", "Perth, Australia", "Adelaide, Australia",
            "Vienna, Austria", "Zurich, Switzerland", "Geneva, Switzerland", "Amsterdam, Netherlands",
            "Rotterdam, Netherlands", "Antwerp, Belgium", "Brussels, Belgium", "Copenhagen, Denmark",
            "Stockholm, Sweden", "Oslo, Norway", "Helsinki, Finland", "Reykjavik, Iceland",
            "Dublin, Ireland", "Edinburgh, UK", "Manchester, UK", "Birmingham, UK",
            "Glasgow, UK", "Cardiff, UK", "Lisbon, Portugal", "Porto, Portugal",
            "Barcelona, Spain", "Valencia, Spain", "Seville, Spain", "Bilbao, Spain",
            "Florence, Italy", "Naples, Italy", "Venice, Italy", "Milan, Italy",
            "Hamburg, Germany", "Munich, Germany", "Frankfurt, Germany", "Cologne, Germany",
            "Warsaw, Poland", "Krakow, Poland", "Prague, Czech Republic", "Budapest, Hungary",
            "Bucharest, Romania", "Sofia, Bulgaria", "Athens, Greece", "Thessaloniki, Greece",
            "Istanbul, Turkey", "Ankara, Turkey", "Izmir, Turkey", "Marrakech, Morocco",
            "Casablanca, Morocco", "Rabat, Morocco", "Fes, Morocco", "Algiers, Algeria",
            "Tunis, Tunisia", "Tripoli, Libya", "Khartoum, Sudan", "Nairobi, Kenya",
            "Lagos, Nigeria", "Accra, Ghana", "Dakar, Senegal", "Abuja, Nigeria"
        )

    }

    fun filterCityList(query: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val filteredList = if (query.isEmpty()) {
                emptyList()
            } else {
                _cityList.value.filter {
                    it.contains(query, ignoreCase = true)
                }
            }
            _filteredCityList.emit(filteredList)
        }
    }


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
