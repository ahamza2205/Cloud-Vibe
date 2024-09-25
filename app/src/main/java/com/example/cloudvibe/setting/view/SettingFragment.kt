package com.example.cloudvibe.setting.view

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import com.example.cloudvibe.R
import com.example.cloudvibe.activity.MainActivity
import com.example.cloudvibe.sharedpreferences.SharedPreferencesHelper
import com.example.cloudvibe.utils.LocaleHelper.updateLocale
import java.util.Locale

class SettingFragment : Fragment() {

    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        // Initialize the SharedPreferencesHelper
        sharedPreferencesHelper = SharedPreferencesHelper(requireContext())

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
                    // Save GPS location (dummy data for now)
                    sharedPreferencesHelper.saveLocation(31.2156, 29.9553) // Alexandria, Egypt coordinates
                }
                R.id.rb_map -> {
                    // Save Map location (dummy data for now)
                    sharedPreferencesHelper.saveLocation(31.2156, 29.9553) // Alexandria, Egypt coordinates
                }
            }
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
                    //updateLocaleAndRecreate("en")
                    (requireActivity() as MainActivity).checkAndChangLocality()
                }
                R.id.rb_arabic -> {
                    sharedPreferencesHelper.saveLanguage("ar")
                    //updateLocaleAndRecreate("ar")
                    (requireActivity() as MainActivity).checkAndChangLocality()
                }
            }
        }
    }

    private fun updateLocaleAndRecreate(languageCode: String) {
        updateLocale(requireContext(), languageCode)

        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.detach(this).attach(this).commit()
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
            "metric" -> view.findViewById<RadioButton>(R.id.rb_celsius).isChecked = true
            "kelvin" -> view.findViewById<RadioButton>(R.id.rb_kelvin).isChecked = true
            "imperial" -> view.findViewById<RadioButton>(R.id.rb_fahrenheit).isChecked = true
        }

        // Retrieve saved language and set the corresponding RadioButton
        val savedLanguage = sharedPreferencesHelper.getLanguage()
        when (savedLanguage) {
            "en" -> view.findViewById<RadioButton>(R.id.rb_english).isChecked = true
            "ar" -> view.findViewById<RadioButton>(R.id.rb_arabic).isChecked = true
        }

        // Retrieve saved wind speed unit and set the corresponding RadioButton
        val savedWindSpeedUnit = sharedPreferencesHelper.getWindSpeedUnit()
        when (savedWindSpeedUnit) {
            "m/s" -> view.findViewById<RadioButton>(R.id.rb_meter_sec).isChecked = true
            "mph" -> view.findViewById<RadioButton>(R.id.rb_mile_hour).isChecked = true
            else -> view.findViewById<RadioButton>(R.id.rb_km_hour).isChecked = true // Default to km/h
        }

        // Optionally, retrieve the saved location and use it (for GPS or Map option)
        val savedLocation = sharedPreferencesHelper.getLocation()
        savedLocation?.let {
            // Handle the saved location if necessary (e.g., display in the UI)
        }
    }
}
