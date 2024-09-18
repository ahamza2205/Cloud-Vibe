package com.example.cloudvibe.home.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudvibe.R
import com.example.cloudvibe.home.viewmodel.HomeViewModel
import com.example.cloudvibe.home.viewmodel.HomeViewModelFactory
import com.example.cloudvibe.model.database.WeatherDatabase
import com.example.cloudvibe.model.network.WeatherApiService
import com.example.cloudvibe.model.repository.WeatherRepository
import com.example.cloudvibe.utils.RetrofitInstance
import com.example.cloudvibe.home.view.HourlyForecastAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import com.example.cloudvibe.model.network.data.ForecastItem

class HomeFragment : Fragment() {

    private lateinit var hourlyForecastRecyclerView: RecyclerView
    private lateinit var hourlyForecastAdapter: HourlyForecastAdapter
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get WeatherApiService from RetrofitInstance
        val weatherApiService = RetrofitInstance.api

        // Get WeatherDao instance
        val weatherDao = WeatherDatabase.getDatabase(requireContext()).weatherDao()

        // Initialize WeatherRepository with both WeatherApiService and WeatherDao
        val weatherRepository = WeatherRepository(weatherApiService, weatherDao)

        // Create ViewModelFactory
        val viewModelFactory = HomeViewModelFactory(weatherRepository)

        // Initialize HomeViewModel
        homeViewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)

        // Initialize RecyclerView and Adapter
        hourlyForecastRecyclerView = view.findViewById(R.id.recyclerViewForecast)
        hourlyForecastAdapter = HourlyForecastAdapter(mutableListOf(), "°C", "km/h")

        // Set up RecyclerView with a LinearLayoutManager (horizontal orientation)
        hourlyForecastRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        hourlyForecastRecyclerView.adapter = hourlyForecastAdapter

        // Observe ViewModel data
        homeViewModel.forecastWeather.observe(viewLifecycleOwner, Observer { forecastItems ->
            // Assuming forecastItems are of type List<ForecastItem>
            // Convert ForecastItem to Hourly if necessary
          /*  val hourlyData = forecastItems.map { it.toHourly() } // Ensure toHourly() is defined and works correctly
            hourlyForecastAdapter.updateList(hourlyData, "°C", "km/h")*/
        })

        // Fetch data for the first time (replace lat, lon, and apiKey with actual values)
        homeViewModel.fetchCurrentWeather(lat = 37.7749, lon = -122.4194, apiKey = "7af08d0e1d543aea9b340405ceed1c3d")
        homeViewModel.loadWeather(lat = 44.34, lon = 10.99, apiKey = "7af08d0e1d543aea9b340405ceed1c3d")
        // Load local forecasts
        homeViewModel.loadLocalForecasts()
    }
}
