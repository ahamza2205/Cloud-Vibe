package com.example.cloudvibe.setting.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.health.connect.datatypes.ExerciseRoute
import android.location.Location

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import androidx.fragment.app.Fragment
import com.example.cloudvibe.R
import com.example.cloudvibe.activity.MainActivity
import com.example.cloudvibe.favorit.view.MapFragment
import com.example.cloudvibe.sharedpreferences.SharedPreferencesHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority


class SettingFragment : Fragment() {

    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)
        // Check if location permission is granted at the beginning
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission if not granted
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }
        // Initialize the SharedPreferencesHelper
        sharedPreferencesHelper = SharedPreferencesHelper(requireContext())
        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location: Location? = locationResult.lastLocation
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude

                    sharedPreferencesHelper.saveLocation(latitude, longitude)

                    Log.d("LocationCallback", "Latitude: $latitude, Longitude: $longitude")
                } ?: run {
                    Log.e("LocationCallback", "Failed to get location")
                }
            }
        }
        // Set listeners for each setting option
        setupLocationSetting(view)
        setupUnitsSetting(view)
        setupLanguageSetting(view)
        setupWindSetting(view) // New wind setting

        return view
    }
    // Function to handle Location setting (GPS/Map)
    private fun setupLocationSetting(view: View) {
        val locationGroup = view.findViewById<RadioGroup>(R.id.radio_group_location)
        locationGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_gps -> {
                    // Check for location permission
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // Request permission if not granted
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            100
                        )
                        return@setOnCheckedChangeListener
                    }

                    startLocationUpdates()
                }
                R.id.rb_map -> {
                    openMapFragment("setting")
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
    }
    override fun onStop() {
        super.onStop()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun openMapFragment(comeFrom : String) {
        val bundle = Bundle().apply {
            putString("comeFrom", comeFrom)
        }

        val mapFragment = MapFragment().apply {
            arguments = bundle
        }

        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, mapFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    // Function to save latitude and longitude to SharedPreferences
    private fun saveLocationToPreferences(latitude: Double, longitude: Double) {
        val sharedPreferences = requireActivity().getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putFloat("latitude", latitude.toFloat())
            putFloat("longitude", longitude.toFloat())
            apply()
        }
    }

    private fun setupUnitsSetting(view: View) {
        val tempGroup = view.findViewById<RadioGroup>(R.id.radio_group_temperature)
        tempGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_celsius -> sharedPreferencesHelper.saveUnits("°C")
                R.id.rb_kelvin -> sharedPreferencesHelper.saveUnits("°K")
                R.id.rb_fahrenheit -> sharedPreferencesHelper.saveUnits("°F")
            }
        }
    }

    // Function to handle Language setting (English/Arabic)
    private fun setupLanguageSetting(view: View) {
        val languageGroup = view.findViewById<RadioGroup>(R.id.radio_group_language)
        languageGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_english -> {
                    sharedPreferencesHelper.saveLanguage("en")
                    (requireActivity() as MainActivity).checkAndChangLocality()
                }
                R.id.rb_arabic -> {
                    sharedPreferencesHelper.saveLanguage("ar")
                    (requireActivity() as MainActivity).checkAndChangLocality()
                }
            }
        }
    }

    // Function to handle Wind setting (Meter/sec or Miles/hour)
    private fun setupWindSetting(view: View) {
        val windGroup = view.findViewById<RadioGroup>(R.id.radio_group_wind_speed)
        windGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_meter_sec -> sharedPreferencesHelper.saveWindSpeedUnit("m/s")
                R.id.rb_mile_hour -> sharedPreferencesHelper.saveWindSpeedUnit("mph")  // Change to "mph"
                R.id.rb_km_hour -> sharedPreferencesHelper.saveWindSpeedUnit("km/h")
            }
        }
    }


    // Retrieve and set the previously selected options
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve saved units and set the corresponding RadioButton
        val savedUnits = sharedPreferencesHelper.getUnits()
        when (savedUnits) {
            "°C" -> view.findViewById<RadioButton>(R.id.rb_celsius).isChecked = true
            "°K" -> view.findViewById<RadioButton>(R.id.rb_kelvin).isChecked = true
            "°F" -> view.findViewById<RadioButton>(R.id.rb_fahrenheit).isChecked = true
            else -> {
                // If no preference is saved, set Celsius as default
                view.findViewById<RadioButton>(R.id.rb_celsius).isChecked = true
                sharedPreferencesHelper.saveUnits("°C") // Save default as Celsius
            }
        }

        // Retrieve saved wind speed unit and set the corresponding RadioButton
        val savedWindSpeedUnit = sharedPreferencesHelper.getWindSpeedUnit()
        when (savedWindSpeedUnit) {
            "m/s" -> view.findViewById<RadioButton>(R.id.rb_meter_sec).isChecked = true
            "mph" -> view.findViewById<RadioButton>(R.id.rb_mile_hour).isChecked = true
            else -> {
                // If no preference is saved, set km/h as default
                view.findViewById<RadioButton>(R.id.rb_km_hour).isChecked = true
                sharedPreferencesHelper.saveWindSpeedUnit("km/h") // Save default as km/h
            }
        }

        // Retrieve saved language and set the corresponding RadioButton
        val savedLanguage = sharedPreferencesHelper.getLanguage()
        when (savedLanguage) {
            "en" -> view.findViewById<RadioButton>(R.id.rb_english).isChecked = true
            "ar" -> view.findViewById<RadioButton>(R.id.rb_arabic).isChecked = true
        }

        // Optionally, retrieve the saved location and use it (for GPS or Map option)
        val savedLocation = sharedPreferencesHelper.getLocation()
        savedLocation?.let {
            // Handle the saved location if necessary (e.g., display in the UI)
        }
    }

}
