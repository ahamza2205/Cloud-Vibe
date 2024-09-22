package com.example.cloudvibe.map

import android.app.AlertDialog
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.cloudvibe.R
import com.example.cloudvibe.home.view.HomeFragment
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.events.MapEventsReceiver
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.IOException

class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private var selectedLocationMarker: Marker? = null
    private var selectedGeoPoint: GeoPoint? = null

    // Using SharedViewModel
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the library
        Configuration.getInstance().load(requireActivity(), PreferenceManager.getDefaultSharedPreferences(requireActivity()))
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

        // Handle map clicks to add a new marker
        mapView.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }

            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let {
                    selectedGeoPoint = it
                    getCityNameAndShowDialog(it)
                }
                return true
            }
        }))
    }

    private fun addSelectedMarker(point: GeoPoint) {
        // Remove the previous selected location marker if it exists
        selectedLocationMarker?.let {
            mapView.overlays.remove(it)
        }

        // Add a new marker for the selected location
        selectedLocationMarker = Marker(mapView).apply {
            position = point
            icon = resources.getDrawable(R.drawable.ic_selected_marker) // Your marker icon
        }

        mapView.overlays.add(selectedLocationMarker)
        mapView.invalidate()
    }

    private fun getCityNameAndShowDialog(point: GeoPoint) {
        val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=${point.latitude}&lon=${point.longitude}&zoom=10&addressdetails=1"

        val request = OkHttpClient().newCall(
            Request.Builder()
                .url(url)
                .build()
        )

        request.enqueue(object : okhttp3.Callback {
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val json = response.body?.string()
                if (json != null) {
                    val jsonObject = JSONArray("[$json]").getJSONObject(0)
                    val cityName = jsonObject.getJSONObject("address").optString("city", "Unknown location")

                    activity?.runOnUiThread {
                        showConfirmationDialog(cityName, point)
                    }
                }
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to get city name", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun showConfirmationDialog(cityName: String, point: GeoPoint) {
        // Show a dialog to confirm the selected location
        AlertDialog.Builder(requireContext())
            .setTitle("Location Selected")
            .setMessage("Do you want to see the weather for $cityName?")
            .setPositiveButton("OK") { _, _ ->
                // Add marker for selected location
                addSelectedMarker(point)
                // Set selected coordinates in SharedViewModel
                sharedViewModel.setSelectedLocation(point.latitude, point.longitude)
                // Navigate to HomeFragment
                navigateToHomeFragment()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss() // Do nothing, allow user to select another location
            }
            .create()
            .show()
    }

    private fun navigateToHomeFragment() {
        // This method handles the navigation to HomeFragment
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()  // Enable map view
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()  // Disable map view
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDetach()  // Clean up map view
    }
}
