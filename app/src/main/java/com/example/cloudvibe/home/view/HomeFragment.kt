package com.example.cloudvibe.home.view
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import com.example.cloudvibe.databinding.FragmentHomeBinding
import com.example.cloudvibe.activity.SharedViewModel
import com.example.cloudvibe.model.database.toHourly
import com.example.cloudvibe.utils.UnitConverter.convertTemperature
import com.example.cloudvibe.utils.UnitConverter.convertUnixTimeToTime
import com.example.cloudvibe.utils.UnitConverter.convertWindSpeed
import com.example.cloudvibe.utils.UnitConverter.parseIntegerIntoArabic
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var hourlyForecastAdapter: HourlyForecastAdapter
    private lateinit var dailyForecastAdapter: DailyAdapter
    private val homeViewModel: HomeViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var symbol: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val latArg = arguments?.getDouble("lat")
        val lonArg = arguments?.getDouble("lon")
        if (latArg != null && lonArg != null && latArg != 0.0 && lonArg != 0.0) {
            homeViewModel.fetchAndDisplayWeather(latArg, lonArg)
            homeViewModel.fetchAndDisplayForecast(latArg, lonArg)
        } else {
            val location = homeViewModel.getLocation()
            if (location != null) {
                val latitude = location.first
                val longitude = location.second
                homeViewModel.updateLocation(latitude, longitude)
                homeViewModel.fetchAndDisplayWeather(latitude, longitude)
                homeViewModel.fetchAndDisplayForecast(latitude, longitude)
            }
        }
        homeViewModel.updateSettings()
        lifecycleScope.launch {
            homeViewModel.tempUnit.collect { unit ->
                symbol = unit
            }
        }
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupObservers()

        sharedViewModel.selectedLocation.observe(viewLifecycleOwner) { location ->
            val (latitude, longitude) = location
            fetchWeatherData(latitude, longitude)
        }

        arguments?.getString("city_name")?.let { cityName ->
            fetchWeatherDataByCityName(cityName)
        }

    }
    private fun setupRecyclerViews() {
        hourlyForecastAdapter = HourlyForecastAdapter(mutableListOf(), symbol, "km/h")
        binding.recyclerViewForecast.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = hourlyForecastAdapter
        }

        dailyForecastAdapter = DailyAdapter(mutableListOf(), symbol, requireContext())
        binding.dayRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = dailyForecastAdapter
        }
    }
    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        homeViewModel.fetchAndDisplayWeather(latitude, longitude)
        homeViewModel.fetchAndDisplayForecast(latitude, longitude)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.weatherState.collect { state ->
                when (state) {
                    is ApiState.Loading -> {
                        Log.d("ProgressBar", "Loading state triggered")
                        binding.progressBar.visibility = View.VISIBLE
                        binding.weatherDataLayout.visibility = View.GONE
                    }
                    is ApiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.weatherDataLayout.visibility = View.VISIBLE
                        if (state.data.isNotEmpty()) {
                            displayWeatherData(state.data[0])
                        }
                    }
                    is ApiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.savedForecast
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { forecastItems ->
                    val hourlyData: List<Hourly> = forecastItems.map { it.toHourly() }
                    hourlyForecastAdapter.updateList(hourlyData, symbol, "km/h")
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
                    .map { (_, forecasts) -> forecasts.random() }
                    .take(6)

                dailyForecastAdapter.updateList(dailyData, symbol)
            }
        }
    }
    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun displayWeatherData(weatherEntity: WeatherEntity) {
        val savedUnit = homeViewModel.getUnits()
        val convertedTemp = convertTemperature(weatherEntity.temperature, savedUnit)
        val unitSymbol = when (savedUnit) {
            "°F" -> "°F"
            "°K" -> "°K"
            else -> "°C"
        }
        binding.tvTemperature.text = String.format("%.1f ", convertedTemp) + unitSymbol
        val windSpeedUnit = homeViewModel.getWindSpeedUnit()
        val convertedWindSpeed = convertWindSpeed(weatherEntity.windSpeed, windSpeedUnit)
        with(binding) {
            tvLocation.text = weatherEntity.locationName
            tvCountry.text = " ${weatherEntity.country}"
            tvLocalTime.text = convertUnixTimeToTime(weatherEntity.timestamp)
            tvCondition.text = weatherEntity.description
            textViewWindspeed.text = "${parseIntegerIntoArabic(convertedWindSpeed.toString(),requireContext())}$windSpeedUnit"
            textViewHumidity.text = "${parseIntegerIntoArabic(weatherEntity.humidity.toString(),requireContext())}%"
            textViewPressure.text = "${parseIntegerIntoArabic(weatherEntity.pressure.toString(),requireContext())}mBar"
            textViewSunrise.text = convertUnixTimeToTime(weatherEntity.sunrise)
            textViewSunset.text = convertUnixTimeToTime(weatherEntity.sunset)
        }
    }
    private fun fetchWeatherDataByCityName(cityName: String) {
        // Use the ViewModel to get coordinates and fetch weather
        homeViewModel.getCoordinatesFromCityName(cityName) { lat, lon ->
            fetchWeatherData(lat, lon)
        }
    }
}
