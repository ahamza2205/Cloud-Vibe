package com.example.cloudvibe.favorit.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudvibe.R
import com.example.cloudvibe.activity.MainActivity
import com.example.cloudvibe.favorit.favdetil.LocationAdapter
import com.example.cloudvibe.favorit.viewmodel.MapViewModel
import com.example.cloudvibe.home.view.HomeFragment
import com.example.cloudvibe.model.database.FavoriteCity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@AndroidEntryPoint
class MapFragment : Fragment(), LocationListener {

    private lateinit var mapView: MapView
    private lateinit var searchEditText: EditText
    private lateinit var cityAdapter: LocationAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var comeFrom : String
    private var selectedLocationMarker: Marker? = null
    private var currentLocationMarker: Marker? = null

    private val mapViewModel: MapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(requireActivity(), PreferenceManager.getDefaultSharedPreferences(requireActivity()))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        comeFrom = arguments?.getString("comeFrom").toString()
        return inflater.inflate(R.layout.fragment_map, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.supportActionBar?.hide()

        // Handle the back button
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { v, keyCode, event ->
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK && event.action == android.view.KeyEvent.ACTION_UP) {
                requireActivity().supportFragmentManager.popBackStack()
                true
            } else {
                false
            }
        }

        mapView = view.findViewById(R.id.map)
        searchEditText = view.findViewById(R.id.mainEditText)
        recyclerView = view.findViewById(R.id.recyclerView)
        cityAdapter = LocationAdapter { city ->
            // When a city is selected from the list, set it in the EditText
            searchEditText.setText(city)
            recyclerView.visibility = View.GONE
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = cityAdapter

        // Text changed listener for filtering cities
        searchEditText.addTextChangedListener { text ->
            mapViewModel.filterCityList(text.toString())
        }

        // Observing filtered city list
        lifecycleScope.launchWhenStarted {
            mapViewModel.filteredCityList.collectLatest { filteredList ->
                if (filteredList.isEmpty()) {
                    recyclerView.visibility = View.GONE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    cityAdapter.submitList(filteredList)
                }
            }
        }

        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        // Set initial point on the map
        val startPoint = GeoPoint(31.2001, 29.9187)
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(startPoint)

        // Set the search action for the searchEditText
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val cityName = searchEditText.text.toString().trim()
                if (cityName.isNotEmpty()) {
                    mapViewModel.searchForCity(cityName)
                } else {
                    Toast.makeText(requireContext(), "Please enter a city name", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

        // Observing city coordinates to update the map
        mapViewModel.cityCoordinates.observe(viewLifecycleOwner) { geoPoint ->
            mapView.controller.setCenter(geoPoint)
            addSelectedMarker(geoPoint)
            showConfirmationDialog(searchEditText.text.toString(), geoPoint)
        }

        // Handle errors from ViewModel
        mapViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }

        // Get current location
        getCurrentLocation()
    }

    // Add a marker to the selected location on the map
    private fun addSelectedMarker(point: GeoPoint) {
        selectedLocationMarker?.let {
            mapView.overlays.remove(it)
        }

        selectedLocationMarker = Marker(mapView).apply {
            position = point
            icon = resources.getDrawable(R.drawable.ic_selected_marker) // Selected location marker icon
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        mapView.overlays.add(selectedLocationMarker)
        mapView.invalidate()
    }

    // Show confirmation dialog to add city to favorites
    private fun showConfirmationDialog(cityName: String, geoPoint: GeoPoint) {
        if (comeFrom == "setting") {
            AlertDialog.Builder(requireContext())
                .setTitle("See Weather")
                .setMessage("Do you want see the weather for $cityName ?")
                .setPositiveButton("OK") { _, _ ->
                    val bundle = Bundle().apply {
                        putDouble("lat", geoPoint.latitude)
                        putDouble("lon", geoPoint.longitude)
                    }

                    val homeFragment = HomeFragment().apply {
                        arguments = bundle
                    }

                    val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.fragment_container, homeFragment)
                    fragmentTransaction.commit()
                }
                .setNegativeButton("Cancel", null)
                .show()

        }else{
            AlertDialog.Builder(requireContext())
                .setTitle("Add to Favorites")
                .setMessage("Do you want to add $cityName to your favorites?")
                .setPositiveButton("OK") { _, _ ->
                    saveCityToFavorites(cityName, geoPoint.latitude, geoPoint.longitude)

                    val favoritFragment = FavoritFragment()
                    val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.fragment_container, favoritFragment)
                    fragmentTransaction.commit()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    // Save selected city to favorites
    private fun saveCityToFavorites(cityName: String, lat: Double, lon: Double) {
        val favoriteCity = FavoriteCity(cityName = cityName, latitude = lat, longitude = lon)

        lifecycleScope.launch {
            mapViewModel.insertFavoriteCity(favoriteCity)
            Toast.makeText(requireContext(), "$cityName added to favorites", Toast.LENGTH_SHORT).show()
        }
    }

    // Get the current location of the user
    private fun getCurrentLocation() {
        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
            return
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this)
    }

    // Called when the location is updated
    override fun onLocationChanged(location: Location) {
        mapView?.let { map ->
            val currentPoint = GeoPoint(location.latitude, location.longitude)

            currentLocationMarker?.let {
                map.overlays.remove(it)
            }

            currentLocationMarker = Marker(map).apply {
                position = currentPoint
                icon = resources.getDrawable(R.drawable.ic_location_marker) // Your current location marker icon
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }

            map.overlays.add(currentLocationMarker)
            map.controller.setCenter(currentPoint)
            map.invalidate()
        } ?: run {
            Toast.makeText(requireContext(), "Map is not initialized", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDetach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.supportActionBar?.show()
    }
}
