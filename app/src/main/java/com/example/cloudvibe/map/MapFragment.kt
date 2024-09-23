package com.example.cloudvibe.map

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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.cloudvibe.R
import com.example.cloudvibe.activity.MainActivity
import com.example.cloudvibe.favorit.view.FavoritFragment
import com.example.cloudvibe.model.database.FavoriteCity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.IOException

@AndroidEntryPoint
class MapFragment : Fragment(), LocationListener {

    private lateinit var mapView: MapView
    private lateinit var searchEditText: EditText
    private var selectedLocationMarker: Marker? = null
    private var currentLocationMarker: Marker? = null

    private val mapViewModel: MapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(requireActivity(), PreferenceManager.getDefaultSharedPreferences(requireActivity()))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.supportActionBar?.hide()

        mapView = view.findViewById(R.id.map)
        searchEditText = view.findViewById(R.id.mainEditText)

        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        val startPoint = GeoPoint(31.2001, 29.9187)
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(startPoint)

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

        mapViewModel.cityCoordinates.observe(viewLifecycleOwner) { geoPoint ->
            mapView.controller.setCenter(geoPoint)
            addSelectedMarker(geoPoint)
            showConfirmationDialog(searchEditText.text.toString(), geoPoint)
        }

        mapViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }

        getCurrentLocation()
    }

    private fun addSelectedMarker(point: GeoPoint) {
        selectedLocationMarker?.let {
            mapView.overlays.remove(it)
        }

        selectedLocationMarker = Marker(mapView).apply {
            position = point
            icon = resources.getDrawable(R.drawable.ic_selected_marker) // Your selected location marker icon
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        mapView.overlays.add(selectedLocationMarker)
        mapView.invalidate()
    }

    private fun showConfirmationDialog(cityName: String, geoPoint: GeoPoint) {
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



    private fun saveCityToFavorites(cityName: String, lat: Double, lon: Double) {
        val favoriteCity = FavoriteCity(cityName = cityName, latitude = lat, longitude = lon)

        lifecycleScope.launch {
            mapViewModel.insertFavoriteCity(favoriteCity)
            Toast.makeText(requireContext(), "$cityName added to favorites", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentLocation() {
        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
            return
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this)
    }

    override fun onLocationChanged(location: Location) {
        val currentPoint = GeoPoint(location.latitude, location.longitude)

        currentLocationMarker = Marker(mapView).apply {
            position = currentPoint
            icon = resources.getDrawable(R.drawable.ic_location_marker) // Your current location marker icon
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        mapView.overlays.add(currentLocationMarker)
        mapView.controller.setCenter(currentPoint)
        mapView.invalidate()
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
