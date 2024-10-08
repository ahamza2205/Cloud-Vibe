package com.example.cloudvibe.favorit.favdetil

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cloudvibe.activity.SharedViewModel
import com.example.cloudvibe.databinding.FragmentFavoritWeatherBinding
import com.example.cloudvibe.home.view.DailyAdapter
import com.example.cloudvibe.home.view.HourlyForecastAdapter
import com.example.cloudvibe.home.viewmodel.HomeViewModel
import com.example.cloudvibe.model.database.WeatherEntity
import com.example.cloudvibe.model.database.toHourly
import com.example.cloudvibe.model.network.data.Hourly
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Button
import android.widget.Toast
import com.example.cloudvibe.R
import com.example.cloudvibe.home.view.ApiState

@AndroidEntryPoint
class FavoritWeatherFragment : Fragment() {
    private lateinit var binding: FragmentFavoritWeatherBinding
    private lateinit var hourlyForecastAdapter: HourlyForecastAdapter
    private lateinit var dailyForecastAdapter: DailyAdapter
    private val sharedViewModel: SharedViewModel by activityViewModels() // ViewModel shared with MapFragment
    private val favoritViewModel: HomeViewModel by viewModels()

    private lateinit var symbol: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoritWeatherBinding.inflate(inflater, container, false)
        favoritViewModel.updateSettings()
        lifecycleScope.launch {
            favoritViewModel.tempUnit.collect { unit ->
                symbol = unit
                Log.d("TAG1", "onCreateView: $symbol")
            }
        }
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isInternetAvailable()) {
            showCustomNoInternetDialog()
            return
        } else {
            // Continue with data fetching if internet is available
            if (sharedViewModel.detailsLocation.value != null) {
                fetchWeatherData(sharedViewModel.detailsLocation.value!!.latitude, sharedViewModel.detailsLocation.value!!.longitude)
            }

            setupRecyclerViews()
            setupObservers()
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
        Log.d("HomeFragment", "Fetching weather data for lat: $latitude, lon: $longitude")
        favoritViewModel.fetchAndDisplayWeather(
            latitude,
            longitude,
        )
        favoritViewModel.fetchAndDisplayForecast(
            latitude,
            longitude,
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            favoritViewModel.weatherState.collect { state ->
                when (state) {
                    is ApiState.Loading -> {
                        binding.lottieAnimationView.visibility = View.VISIBLE
                        binding.weatherDataLayout.visibility = View.GONE
                    }
                    is ApiState.Success -> {
                        binding.lottieAnimationView.visibility = View.GONE
                        binding.weatherDataLayout.visibility = View.VISIBLE
                        if (state.data.isNotEmpty()) {
                            displayWeatherData(state.data[0])
                        }                    }
                    is ApiState.Error -> {
                        binding.lottieAnimationView.visibility = View.GONE
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            favoritViewModel.savedForecast
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { forecastItems ->
                    val hourlyData: List<Hourly> = forecastItems.map { it.toHourly() }
                    hourlyForecastAdapter.updateList(hourlyData, symbol, "km/h")
                }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            favoritViewModel.savedForecast.collect { forecastItems ->
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

    private fun displayWeatherData(weatherEntity: WeatherEntity) {
        val savedUnit = favoritViewModel.getUnits()
        val convertedTemp = convertTemperature(weatherEntity.temperature, savedUnit)
        val unitSymbol = when (savedUnit) {
            "°F" -> "°F"
            "°K" -> "°K"
            else -> "°C"
        }

        binding.tvTemperature.text = String.format("%.1f ", convertedTemp) + unitSymbol

        val windSpeedUnit = favoritViewModel.getWindSpeedUnit()
        val convertedWindSpeed = convertWindSpeed(weatherEntity.windSpeed, windSpeedUnit)

        with(binding) {
            tvLocation.text = weatherEntity.locationName
            tvCountry.text = " ${weatherEntity.country}"
            tvLocalTime.text = convertUnixTimeToTime(weatherEntity.timestamp)
            tvCondition.text = weatherEntity.description
            textViewWindspeed.text = " ${convertedWindSpeed}$windSpeedUnit"
            textViewHumidity.text = " ${weatherEntity.humidity}%"
            textViewPressure.text = " ${weatherEntity.pressure}mBar"
            textViewSunrise.text = convertUnixTimeToTime(weatherEntity.sunrise)
            textViewSunset.text = convertUnixTimeToTime(weatherEntity.sunset)
        }
    }

    private fun convertTemperature(tempInCelsius: Float, unit: String): Number {
        return when (unit) {
            "°K" -> tempInCelsius + 273.15
            "°F" -> (tempInCelsius * 9 / 5) + 32
            else -> tempInCelsius
        }
    }

    private fun convertWindSpeed(speedInKmH: Double, unit: String): Double {
        val convertedSpeed = when (unit) {
            "m/s" -> speedInKmH / 3.6
            "mph" -> speedInKmH * 0.621371
            else -> speedInKmH
        }
        return (Math.round(convertedSpeed * 100.0) / 100.0)
    }

    private fun convertUnixTimeToTime(unixTime: Long): String {
        val date = Date(unixTime * 1000)
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }



    // Check if internet is available
    private fun isInternetAvailable(): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showCustomNoInternetDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_no_internet, null)

        val customDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val retryButton = dialogView.findViewById<Button>(R.id.dialogRetryButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.dialogButton)

        retryButton.setOnClickListener {
            customDialog.dismiss()
            if (isInternetAvailable()) {
                if (sharedViewModel.detailsLocation.value != null) {
                    fetchWeatherData(sharedViewModel.detailsLocation.value!!.latitude, sharedViewModel.detailsLocation.value!!.longitude)
                }
                setupRecyclerViews()
                setupObservers()
            } else {
                showCustomNoInternetDialog()
            }
        }

        cancelButton.setOnClickListener {
            customDialog.dismiss()
        }

        customDialog.show()
    }

}