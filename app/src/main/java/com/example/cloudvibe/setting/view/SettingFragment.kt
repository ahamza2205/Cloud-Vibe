package com.example.cloudvibe.setting.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import com.example.cloudvibe.R
import com.example.cloudvibe.home.view.HomeFragment
import com.example.cloudvibe.activity.MainActivity
import com.example.cloudvibe.map.MapFragment

class SettingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        val radioGroup = view.findViewById<RadioGroup>(R.id.radio_group_location)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_gps -> {
                    // Handle GPS selection
                    // Here you might trigger a method to get weather using GPS
                    navigateToHomeFragment()
                }
                R.id.rb_map -> {
                    // Handle Map selection
                    // Navigate to Map Fragment to let the user select a location
                    (activity as? MainActivity)?.replaceFragment(MapFragment())
                }
            }
        }

        return view
    }

    private fun navigateToHomeFragment() {
        // Replace the current fragment with HomeFragment
        (activity as? MainActivity)?.replaceFragment(HomeFragment())
    }
}