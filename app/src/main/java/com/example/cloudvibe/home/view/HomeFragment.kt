package com.example.cloudvibe.home.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cloudvibe.home.viewmodel.HomeViewModel
import com.example.cloudvibe.home.viewmodel.HomeViewModelFactory
import com.example.cloudvibe.model.database.WeatherDatabase
import com.example.cloudvibe.model.repository.WeatherRepository
import com.example.cloudvibe.utils.RetrofitInstance
import com.google.android.gms.location.*
import com.example.cloudvibe.databinding.FragmentHomeBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.cloudvibe.model.database.WeatherEntity
import com.example.cloudvibe.model.database.toHourly
import com.example.cloudvibe.model.network.data.Hourly
import com.example.cloudvibe.sharedpreferences.SharedPreferencesHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class HomeFragment : Fragment() {


    private lateinit var binding: FragmentHomeBinding
    private lateinit var hourlyForecastAdapter: HourlyForecastAdapter
    private lateinit var dailyForecastAdapter: DailyAdapter
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var sharedpreferences : SharedPreferencesHelper

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

        // Setup RecyclerView
        val hourlyForecastRecyclerView = binding.recyclerViewForecast
        hourlyForecastAdapter = HourlyForecastAdapter(mutableListOf(), "°C", "km/h")
        hourlyForecastRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        hourlyForecastRecyclerView.adapter = hourlyForecastAdapter


        // Setup daily forecast RecyclerView (7-day forecast)
        val dailyForecastRecyclerView = binding.dayRecycler
        dailyForecastAdapter = DailyAdapter(mutableListOf(), "°C", requireContext())
        dailyForecastRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        dailyForecastRecyclerView.adapter = dailyForecastAdapter

        // Setup ViewModel and Repository
        val weatherApiService = RetrofitInstance.api
        val weatherDao = WeatherDatabase.getDatabase(requireContext()).weatherDao()
        val weatherRepository = WeatherRepository(weatherApiService, weatherDao)
        val viewModelFactory = HomeViewModelFactory(weatherRepository)
        homeViewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)

        // Setup location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Setup SharedPreferences
        sharedpreferences = SharedPreferencesHelper(requireContext())

         // Get saved location from shared preferences
        val savedLocation = sharedpreferences.getLocation()

        if (savedLocation != null) {
            // Fetch weather and forecast
            val (latitude, longitude) = savedLocation
            homeViewModel.fetchAndDisplayWeather(latitude, longitude, "metric", "en", "7af08d0e1d543aea9b340405ceed1c3d")
            homeViewModel.fetchAndDisplayForecast(latitude, longitude, "metric", "en", "7af08d0e1d543aea9b340405ceed1c3d")
        } else {
            checkLocationPermission()
        }
        setupObservers()
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

                    // Save location to shared preferences for future use in the app
                    sharedpreferences.saveLocation(lat, lon)
                    // Fetch weather and forecast data with the new location
                    homeViewModel.fetchAndDisplayWeather(lat, lon, "metric", "en", "7af08d0e1d543aea9b340405ceed1c3d")
                    homeViewModel.fetchAndDisplayForecast(lat, lon, "metric", "en", "7af08d0e1d543aea9b340405ceed1c3d")
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupObservers() {
        // Observe saved data from Room (Weather data)
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.savedWeather.collect { weatherList ->
                if (weatherList.isNotEmpty()) {
                    val weather = weatherList.last()
                    displayWeatherData(weather)
                } else {
                    Toast.makeText(requireContext(), "No weather data available", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observe forecast data and update the Adapter
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.savedForecast.collect { forecastItems ->
                val hourlyData: List<Hourly> = forecastItems.map { it.toHourly() }
                hourlyForecastAdapter.updateList(hourlyData, "°C", "km/h")
            }
        }

        // Observe daily forecast data (5-day forecast)
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.savedForecast.collect { forecastItems ->
                val today = LocalDate.now() // Get the current date
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Define date format

                // Filter out today's forecasts and group by date to take only the first forecast of each day
                val dailyData = forecastItems
                    .filter { forecast ->
                        // Parse the date and compare it to today's date
                        val forecastDate = LocalDate.parse(forecast.date.split(" ")[0], formatter)
                        forecastDate.isAfter(today) // Only keep dates after today
                    }
                    .groupBy { it.date.split(" ")[0] } // Group by date (without time)
                    .map { (_, forecasts) -> forecasts.first() } // Take the first forecast for each day
                    .take(5) // Limiting to next 5 days

                // Update the adapter with filtered daily data
                dailyForecastAdapter.updateList(dailyData, "°C")
            }
        }

    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun displayWeatherData(weatherEntity: WeatherEntity) {
        with(binding) {
            tvLocation.text = weatherEntity.locationName
            tvCountry.text = " ${weatherEntity.country}"
            tvLocalTime.text = convertUnixTimeToTime(weatherEntity.timestamp)

            // Pass the temperature value to String.format
            tvTemperature.text = String.format("%.1f ", weatherEntity.temperature)

            tvCondition.text = weatherEntity.description

            textViewWindspeed.text = " ${weatherEntity.windSpeed}km/h"
            textViewHumidity.text = " ${weatherEntity.humidity}%"
            textViewPressure.text = " ${weatherEntity.pressure}mBar "
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

}
