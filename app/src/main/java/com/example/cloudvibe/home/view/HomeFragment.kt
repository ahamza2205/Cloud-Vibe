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
import com.example.cloudvibe.model.network.WeatherApiService
import com.example.cloudvibe.model.repository.WeatherRepository
import com.example.cloudvibe.utils.RetrofitInstance
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import android.location.Location

class HomeFragment : Fragment() {

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
        return inflater.inflate(R.layout.fragment_home, container, false)
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

        // Observe forecast data
        homeViewModel.forecastWeather.observe(viewLifecycleOwner, Observer { forecastItems ->
            // Assuming forecastItems is a list of ForecastItem
            // hourlyForecastAdapter.updateList(hourlyData, "°C", "km/h")
        })

        // Set up location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        checkLocationPermission()
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
        val locationRequest = LocationRequest.Builder(10000L)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location: Location in locationResult.locations) {
                    val lat = location.latitude
                    val lon = location.longitude
                    homeViewModel.fetchCurrentWeather(lat = lat, lon = lon, apiKey = "7af08d0e1d543aea9b340405ceed1c3d")
                    homeViewModel.loadWeather(lat = lat, lon = lon, apiKey = "7af08d0e1d543aea9b340405ceed1c3d")
                    Toast.makeText(requireContext(), "Location: $lat, $lon", Toast.LENGTH_LONG).show()
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
