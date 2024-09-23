package com.example.cloudvibe.home.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cloudvibe.home.viewmodel.HomeViewModel
import com.example.cloudvibe.model.database.WeatherEntity
import com.example.cloudvibe.model.network.data.Hourly
import com.example.cloudvibe.sharedpreferences.SharedPreferencesHelper
import com.example.cloudvibe.databinding.FragmentHomeBinding
import com.example.cloudvibe.activity.SharedViewModel
import com.example.cloudvibe.model.database.toHourly
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONArray
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Suppress("DEPRECATION")
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var hourlyForecastAdapter: HourlyForecastAdapter
    private lateinit var dailyForecastAdapter: DailyAdapter
        private val homeViewModel: HomeViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels() // ViewModel shared with MapFragment
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var sharedpreferences: SharedPreferencesHelper
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupLocationServices()
        setupSharedPreferences()
        setupObservers()

        //// ---- MAP --- /////
        // Listen for selected location from MapFragment
        // Observe the selected location from SharedViewModel
        sharedViewModel.selectedLocation.observe(viewLifecycleOwner) { location ->
            val (latitude, longitude) = location
            // Use latitude and longitude to fetch weather data
            fetchWeatherData(latitude, longitude)
        }
        arguments?.getString("city_name")?.let { cityName ->
            fetchWeatherDataByCityName(cityName)

        }
    }


    private fun setupRecyclerViews() {
        hourlyForecastAdapter = HourlyForecastAdapter(mutableListOf(), "°C", "km/h")
        binding.recyclerViewForecast.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = hourlyForecastAdapter
        }

        dailyForecastAdapter = DailyAdapter(mutableListOf(), "°C", requireContext())
        binding.dayRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = dailyForecastAdapter
        }
    }

    private fun setupLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private fun setupSharedPreferences() {
        sharedpreferences = SharedPreferencesHelper(requireContext())
        val savedLocation = sharedpreferences.getLocation()

        if (savedLocation != null) {
            val (latitude, longitude) = savedLocation
            fetchWeatherData(latitude, longitude)
        } else {
            checkLocationPermission()
        }
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        Log.d("HomeFragment", "Fetching weather data for lat: $latitude, lon: $longitude")
        homeViewModel.fetchAndDisplayWeather(latitude, longitude, "metric", "en", "7af08d0e1d543aea9b340405ceed1c3d")
        homeViewModel.fetchAndDisplayForecast(latitude, longitude, "metric", "en", "7af08d0e1d543aea9b340405ceed1c3d")
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            getLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000L
            fastestInterval = 5000L
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location: Location in locationResult.locations) {
                    val lat = location.latitude
                    val lon = location.longitude
                    sharedpreferences.saveLocation(lat, lon)
                    fetchWeatherData(lat, lon)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.savedWeather.collect { weatherList ->
                    if (weatherList.isNotEmpty()) {
                        val weather = weatherList.last()
                        displayWeatherData(weather)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.savedForecast
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { forecastItems ->
                    val hourlyData: List<Hourly> = forecastItems.map { it.toHourly() }
                    hourlyForecastAdapter.updateList(hourlyData, "°C", "km/h")
                }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.savedForecast.collect { forecastItems ->
                val today = LocalDate.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                val dailyData = forecastItems
                    .filter { forecast ->
                        val forecastDate = LocalDate.parse(forecast.date.split(" ")[0], formatter)
                        !forecastDate.isBefore(today)
                    }
                    .groupBy { it.date.split(" ")[0] }
                    .map { (_, forecasts) -> forecasts.first() }
                    .take(6)

                dailyForecastAdapter.updateList(dailyData, "°C")
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.savedWeather.collect { weatherList ->
                if (weatherList.isNotEmpty()) {
                    val weather = weatherList.last()
                    displayWeatherData(weather)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun displayWeatherData(weatherEntity: WeatherEntity) {
        with(binding) {
            tvLocation.text = weatherEntity.locationName
            tvCountry.text = " ${weatherEntity.country}"
            tvLocalTime.text = convertUnixTimeToTime(weatherEntity.timestamp)
            tvTemperature.text = String.format("%.1f ", weatherEntity.temperature) + "°C"
            tvCondition.text = weatherEntity.description
            textViewWindspeed.text = " ${weatherEntity.windSpeed}km/h"
            textViewHumidity.text = " ${weatherEntity.humidity}%"
            textViewPressure.text = " ${weatherEntity.pressure}mBar "
            textViewSunrise.text = convertUnixTimeToTime(weatherEntity.sunrise)
            textViewSunset.text = convertUnixTimeToTime(weatherEntity.sunset)
        }
    }

    private fun convertUnixTimeToTime(unixTime: Long): String {
        val date = Date(unixTime * 1000)
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }

    override fun onStop() {
        super.onStop()
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    // Search View Functions to fetch weather data by city name
    private fun fetchWeatherDataByCityName(cityName: String) {
        Log.d("HomeFragment", "Searching for city: $cityName")
        getCoordinatesFromCityName(cityName)
    }

    private fun getCoordinatesFromCityName(cityName: String) {
        val url = "https://nominatim.openstreetmap.org/search?q=$cityName&format=json&addressdetails=1"

        val request = OkHttpClient().newCall(
            okhttp3.Request.Builder()
                .url(url)
                .build()
        )

        request.enqueue(object : okhttp3.Callback {
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val json = response.body?.string()
                if (json != null) {
                    val jsonArray = JSONArray(json)
                    if (jsonArray.length() > 0) {
                        val jsonObject = jsonArray.getJSONObject(0)
                        val latitude = jsonObject.getDouble("lat")
                        val longitude = jsonObject.getDouble("lon")
                        fetchWeatherData(latitude, longitude)
                    } else {
                        activity?.runOnUiThread {
                            Toast.makeText(requireContext(), "City not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Error fetching coordinates", Toast.LENGTH_SHORT).show()
                }
            }
        })

    }
}




