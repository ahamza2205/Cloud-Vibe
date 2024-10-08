package com.example.cloudvibe.favorit.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.cloudvibe.model.database.FavoriteCity
import com.example.cloudvibe.model.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
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

    private val _cityList = MutableSharedFlow<List<String>>(replay = 1)

    private val _filteredCityList = MutableSharedFlow<List<String>>(replay = 1)
    val filteredCityList: SharedFlow<List<String>> get() = _filteredCityList

    init {
        viewModelScope.launch {
            _cityList.emit(
                listOf(
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
            "Lagos, Nigeria", "Accra, Ghana", "Dakar, Senegal", "Abuja, Nigeria",

            // Arabic translations
            "القاهرة، مصر", "الإسكندرية، مصر", "الجيزة، مصر", "بورسعيد، مصر",
            "السويس، مصر", "الأقصر، مصر", "أسوان، مصر", "أسيوط، مصر",
            "البحيرة، مصر", "بني سويف، مصر", "الدقهلية، مصر", "دمياط، مصر",
            "الفيوم، مصر", "الغربية، مصر", "الإسماعيلية، مصر", "كفر الشيخ، مصر",
            "المنيا، مصر", "المنوفية، مصر", "الوادي الجديد، مصر", "القليوبية، مصر",
            "قنا، مصر", "البحر الأحمر، مصر", "الشرقية، مصر", "سوهاج، مصر",
            "جنوب سيناء، مصر", "شمال سيناء، مصر", "مطروح، مصر", "لندن، المملكة المتحدة", "باريس، فرنسا", "نيويورك، الولايات المتحدة", "برلين، ألمانيا",
            "طوكيو، اليابان", "سيدني، أستراليا", "روما، إيطاليا", "مدريد، إسبانيا",
            "موسكو، روسيا", "تورنتو، كندا", "دبي، الإمارات العربية المتحدة", "مومباي، الهند",
            "بكين، الصين", "بانكوك، تايلاند", "إسطنبول، تركيا", "سيول، كوريا الجنوبية",
            "مكسيكو سيتي، المكسيك", "ساو باولو، البرازيل", "بوينس آيرس، الأرجنتين", "كيب تاون، جنوب أفريقيا",
            "لوس أنجلوس، الولايات المتحدة", "شيكاغو، الولايات المتحدة", "سان فرانسيسكو، الولايات المتحدة", "ميامي، الولايات المتحدة",
            "لاس فيغاس، الولايات المتحدة", "بوسطن، الولايات المتحدة", "هيوستن، الولايات المتحدة", "دالاس، الولايات المتحدة",
            "فيلادلفيا، الولايات المتحدة", "أتلانتا، الولايات المتحدة", "سياتل، الولايات المتحدة", "دنفر، الولايات المتحدة",
            "فينيكس، الولايات المتحدة", "سان دييغو، الولايات المتحدة", "أورلاندو، الولايات المتحدة", "أوستن، الولايات المتحدة",
            "بريسبين، أستراليا", "ملبورن، أستراليا", "بيرث، أستراليا", "أديليد، أستراليا",
            "فيينا، النمسا", "زيورخ، سويسرا", "جنيف، سويسرا", "أمستردام، هولندا",
            "روتردام، هولندا", "أنتويرب، بلجيكا", "بروكسل، بلجيكا", "كوبنهاغن، الدنمارك",
            "ستوكهولم، السويد", "أوسلو، النرويج", "هلسنكي، فنلندا", "ريكيافيك، آيسلندا",
            "دبلن، أيرلندا", "إدنبرة، المملكة المتحدة", "مانشستر، المملكة المتحدة", "برمنغهام، المملكة المتحدة",
            "غلاسكو، المملكة المتحدة", "كارديف، المملكة المتحدة", "لشبونة، البرتغال", "بورتو، البرتغال",
            "برشلونة، إسبانيا", "فالنسيا، إسبانيا", "إشبيلية، إسبانيا", "بيلباو، إسبانيا",
            "فلورنسا، إيطاليا", "نابولي، إيطاليا", "البندقية، إيطاليا", "ميلانو، إيطاليا",
            "هامبورغ، ألمانيا", "ميونخ، ألمانيا", "فرانكفورت، ألمانيا", "كولونيا، ألمانيا",
            "وارسو، بولندا", "كراكوف، بولندا", "براغ، جمهورية التشيك", "بودابست، المجر",
            "بوخارست، رومانيا", "صوفيا، بلغاريا", "أثينا، اليونان", "ثيسالونيكي، اليونان",
            "إسطنبول، تركيا", "أنقرة، تركيا", "إزمير، تركيا", "مراكش، المغرب",
            "الدار البيضاء، المغرب", "الرباط، المغرب", "فاس، المغرب", "الجزائر، الجزائر",
            "تونس، تونس", "طرابلس، ليبيا", "الخرطوم، السودان", "نيروبي، كينيا",
            "لاغوس، نيجيريا", "أكرا، غانا", "داكار، السنغال", "أبوجا، نيجيريا"
                )
            )
        }
    }

    fun filterCityList(query: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val filteredList = if (query.isEmpty()) {
                emptyList()
            } else {
                _cityList.replayCache.firstOrNull()?.filter {
                    it.contains(query, ignoreCase = true)
                } ?: emptyList()
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
