package com.example.cloudvibe.home.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudvibe.R
import com.example.cloudvibe.home.viewmodel.HomeViewModel
import com.example.cloudvibe.home.viewmodel.HomeViewModelFactory
import com.example.cloudvibe.model.database.WeatherDatabase
import com.example.cloudvibe.model.repository.WeatherRepository
import com.example.cloudvibe.utils.RetrofitInstance
import com.google.android.gms.location.*
import android.location.Location
import com.example.cloudvibe.databinding.FragmentHomeBinding
import com.example.cloudvibe.model.network.data.Hourly
import com.example.cloudvibe.utils.UnitConverter.kelvinToCelsius
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class HomeFragment : Fragment() {


    private lateinit var binding: FragmentHomeBinding

    private lateinit var hourlyForecastRecyclerView: RecyclerView
    private lateinit var hourlyForecastAdapter: HourlyForecastAdapter
    private lateinit var homeViewModel: HomeViewModel

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the RecyclerView
        hourlyForecastRecyclerView = view.findViewById(R.id.recyclerViewForecast)
        hourlyForecastAdapter = HourlyForecastAdapter(mutableListOf(), "°C", "km/h")
        hourlyForecastRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        hourlyForecastRecyclerView.adapter = hourlyForecastAdapter

        // Set up ViewModel and repository
        val weatherApiService = RetrofitInstance.api
        val weatherDao = WeatherDatabase.getDatabase(requireContext()).weatherDao()
        val weatherRepository = WeatherRepository(weatherApiService, weatherDao)
        val viewModelFactory = HomeViewModelFactory(weatherRepository)
        homeViewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)

        // Observe forecast data and update the adapter
        homeViewModel.forecastWeather.observe(viewLifecycleOwner, Observer { forecastItems ->
            // Convert ForecastItem list to Hourly list for the adapter
            val hourlyData: List<Hourly> = forecastItems.map { it.toHourly() }
            hourlyForecastAdapter.updateList(hourlyData, "°C", "km/h")
        })

        // Set up location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        checkLocationPermission()
        setupObservers()
    }

    // Check and request location permission
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getLocation()
        }
    }

    // Handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Get the current location
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
                    // Fetch current weather and forecast data
                    homeViewModel.fetchCurrentWeather(lat = lat, lon = lon, apiKey = "7af08d0e1d543aea9b340405ceed1c3d")
                    homeViewModel.loadWeather(lat = lat, lon = lon, apiKey = "7af08d0e1d543aea9b340405ceed1c3d")
                    Toast.makeText(requireContext(), "Location: $lat, $lon", Toast.LENGTH_LONG).show()
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

/*    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }*/

    private fun setupObservers() {


        // Observer for current weather
        homeViewModel.currentWeather.observe(viewLifecycleOwner, { result ->
            result.onSuccess { weather ->
                val temperatureInCelsius = kelvinToCelsius(weather.temperature)
                binding.tvLocation.text = weather.locationName
                binding.tvCountry.text = " ${weather.country}"
                binding.tvLocalTime.text = formatTimestampToLocalTime(weather.timestamp)
                binding.tvTemperature.text = String.format("%.2f °C", temperatureInCelsius)
                binding.tvCondition.text = weather.description
                binding.tvWindSpeed.text = "Wind: ${weather.windSpeed} km/h"
                binding.tvHumidity.text = "Humidity: ${weather.humidity} %"
                binding.tvPressure.text = "Pressure: ${weather.pressure} mBar"
            }
            result.onFailure {
                Toast.makeText(requireContext(), "Error fetching weather", Toast.LENGTH_SHORT).show()
            }
        })

        // Observer for forecast weather
        homeViewModel.forecastWeather.observe(viewLifecycleOwner, { forecast ->
            hourlyForecastAdapter.updateList(forecast.map { it.toHourly() }, "", "")
        })
    }
    fun formatTimestampToLocalTime(timestamp: Long): String {
        val date = Date(timestamp * 1000L)  // Convert seconds to milliseconds
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) // Set the desired format
        sdf.timeZone = TimeZone.getDefault() // Set the default time zone
        return sdf.format(date)
    }
}
