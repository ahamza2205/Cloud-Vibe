package com.example.cloudvibe.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.cloudvibe.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.events.MapEventsReceiver

class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private var currentLocationMarker: Marker? = null
    private var selectedLocationMarker: Marker? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(R.id.map)

        // Initialize the map
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Handle map clicks to add a new marker
        mapView.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }

            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let {
                    addSelectedMarker(it)
                }
                return true
            }
        }))

        // Request location permissions and set location
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setCurrentLocation()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity(), OnSuccessListener { location ->
            if (location != null) {
                val currentLocation = GeoPoint(location.latitude, location.longitude)
                mapView.controller.setCenter(currentLocation)
                addCurrentLocationMarker(currentLocation)
            } else {
                Toast.makeText(requireContext(), "Unable to get current location", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addCurrentLocationMarker(point: GeoPoint) {
        // Remove the previous current location marker if it exists
        currentLocationMarker?.let {
            mapView.overlays.remove(it)
        }

        // Add a new marker for the current location
        currentLocationMarker = Marker(mapView)
        currentLocationMarker?.position = point
        currentLocationMarker?.icon = resources.getDrawable(R.drawable.ic_location_marker) // Your marker icon

        mapView.overlays.add(currentLocationMarker)
        mapView.invalidate()
    }

    private fun addSelectedMarker(point: GeoPoint) {
        // Remove the previous selected location marker if it exists
        selectedLocationMarker?.let {
            mapView.overlays.remove(it)
        }

        // Add a new marker for the selected location
        selectedLocationMarker = Marker(mapView)
        selectedLocationMarker?.position = point
        selectedLocationMarker?.icon = resources.getDrawable(R.drawable.ic_selected_marker) // Your marker icon

        mapView.overlays.add(selectedLocationMarker)
        mapView.invalidate()

        // Optionally, you can display a Toast or update the UI to show that the location has been selected
        Toast.makeText(requireContext(), "Selected location: $point", Toast.LENGTH_SHORT).show()
    }
}
