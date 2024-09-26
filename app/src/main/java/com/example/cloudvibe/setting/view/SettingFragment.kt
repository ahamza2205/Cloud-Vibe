package com.example.cloudvibe.setting.view

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.cloudvibe.R
import com.example.cloudvibe.activity.MainActivity
import com.example.cloudvibe.favorit.view.MapFragment
import com.example.cloudvibe.sharedpreferences.SharedPreferencesHelper
import com.google.android.gms.location.*

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
        // Initialize SharedPreferencesHelper
        sharedPreferencesHelper = SharedPreferencesHelper(requireContext())
        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        // Set up LocationRequest
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setWaitForAccurateLocation(true)
            .setMaxUpdates(1)
            .build()
        // Set up LocationCallback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location: Location? = locationResult.lastLocation
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    // Log and save location
                    sharedPreferencesHelper.saveLocation(latitude, longitude)
                } else {
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
            }
        }
        // Set up settings options
        setupLocationSetting(view)
        setupUnitsSetting(view)
        setupLanguageSetting(view)
        setupWindSetting(view)
        setupNotificationSetting(view)
        return view
    }
    // Set up Location setting (GPS/Map)
    private fun setupLocationSetting(view: View) {
        val locationGroup = view.findViewById<RadioGroup>(R.id.radio_group_location)
        locationGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_gps -> {
                    if (checkLocationPermission()) {
                        startLocationUpdates()
                    } else {
                        requestLocationPermission()
                    }
                }
                R.id.rb_map -> {
                    openMapFragment("setting")
                }
            }
        }
    }
    private fun checkLocationPermission(): Boolean {
        val hasPermission = ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return hasPermission
    }
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            100
        )
    }
    private fun startLocationUpdates() {
        if (checkLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        } else {
        }
        if (requestCode == 1002) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Notification Permission Granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Notification Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    // Open MapFragment
    private fun openMapFragment(comeFrom: String) {
        val bundle = Bundle().apply {
            putString("comeFrom", comeFrom)
        }

        val mapFragment = MapFragment().apply {
            arguments = bundle
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, mapFragment)
            .addToBackStack(null)
            .commit()

    }
    // Setup temperature unit setting
    private fun setupUnitsSetting(view: View) {
        val tempGroup = view.findViewById<RadioGroup>(R.id.radio_group_temperature)
        tempGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_celsius -> {
                    sharedPreferencesHelper.saveUnits("°C")
                }
                R.id.rb_kelvin -> {
                    sharedPreferencesHelper.saveUnits("°K")
                }
                R.id.rb_fahrenheit -> {
                    sharedPreferencesHelper.saveUnits("°F")
                }
            }
        }
    }
    // Setup language setting
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
    // Setup wind speed unit setting
    private fun setupWindSetting(view: View) {
        val windGroup = view.findViewById<RadioGroup>(R.id.radio_group_wind_speed)
        windGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_meter_sec -> {
                    sharedPreferencesHelper.saveWindSpeedUnit("m/s")
                }
                R.id.rb_mile_hour -> {
                    sharedPreferencesHelper.saveWindSpeedUnit("mph")
                }
                R.id.rb_km_hour -> {
                    sharedPreferencesHelper.saveWindSpeedUnit("km/h")
                }
            }
        }
    }

    private fun setupNotificationSetting(view: View) {
        val notificationGroup = view.findViewById<RadioGroup>(R.id.radio_group_notifications)

        notificationGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_enable_notifications -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestNotificationPermission()
                    } else {
                        Toast.makeText(requireContext(), "Notifications enabled", Toast.LENGTH_SHORT).show()
                    }
                }
                R.id.rb_disable_notifications -> {
                    disableNotifications()
                    Toast.makeText(requireContext(), "Notifications disabled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1002
            )
        } else {
            Toast.makeText(requireContext(), "Notification Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }
    private fun disableNotifications() {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    // Restore previously selected options
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Restore location setting
        val savedLocationSetting = sharedPreferencesHelper.getLocationSetting()
        if (savedLocationSetting == "gps") {
            view.findViewById<RadioButton>(R.id.rb_gps).isChecked = true
        } else {
            view.findViewById<RadioButton>(R.id.rb_map).isChecked = true
        }

        // Restore units setting
        val savedUnits = sharedPreferencesHelper.getUnits()
        when (savedUnits) {
            "°C" -> view.findViewById<RadioButton>(R.id.rb_celsius).isChecked = true
            "°K" -> view.findViewById<RadioButton>(R.id.rb_kelvin).isChecked = true
            "°F" -> view.findViewById<RadioButton>(R.id.rb_fahrenheit).isChecked = true
        }

        // Restore wind speed unit setting
        val savedWindSpeedUnit = sharedPreferencesHelper.getWindSpeedUnit()
        when (savedWindSpeedUnit) {
            "m/s" -> view.findViewById<RadioButton>(R.id.rb_meter_sec).isChecked = true
            "mph" -> view.findViewById<RadioButton>(R.id.rb_mile_hour).isChecked = true
            "km/h" -> view.findViewById<RadioButton>(R.id.rb_km_hour).isChecked = true
        }

        // Restore language setting
        val savedLanguage = sharedPreferencesHelper.getLanguage()
        Log.d("SettingFragment", "Restored language setting: $savedLanguage")
        when (savedLanguage) {
            "en" -> view.findViewById<RadioButton>(R.id.rb_english).isChecked = true
            "ar" -> view.findViewById<RadioButton>(R.id.rb_arabic).isChecked = true
        }
    }
}
