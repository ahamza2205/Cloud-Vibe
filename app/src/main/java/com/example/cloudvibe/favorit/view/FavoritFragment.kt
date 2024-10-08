package com.example.cloudvibe.favorit.view

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cloudvibe.R
import com.example.cloudvibe.activity.SharedViewModel
import com.example.cloudvibe.databinding.FragmentFavoritBinding
import com.example.cloudvibe.favorit.favdetil.FavoritWeatherFragment
import com.example.cloudvibe.favorit.viewmodel.FavoritViewModel
import com.example.cloudvibe.model.database.FavoriteCity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoritFragment : Fragment() {

    private var _binding: FragmentFavoritBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoritViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var adapter: FavoriteCityAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        binding.fabOpenMap.setOnClickListener {
            val mapFragment = MapFragment()
            val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container, mapFragment)
            fragmentTransaction.addToBackStack(null)  // Important to add this
            fragmentTransaction.commit()
        }

        // ViewModel
        lifecycleScope.launchWhenStarted {
            viewModel.favoriteCities.collect { cities ->
                adapter.updateList(cities)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = FavoriteCityAdapter(
            { city -> showDeleteConfirmationDialog(city) },
            { city ->
                // Check for internet before navigating to the next fragment
                if (!isInternetAvailable()) {
                    showNoInternetDialog() // Show "No Internet" dialog if there's no connection
                } else {
                    // Proceed with navigating to the FavoritWeatherFragment
                    sharedViewModel.detailsLocation.value = city
                    val favoritWeatherFragment = FavoritWeatherFragment()
                    val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.fragment_container, favoritWeatherFragment)
                    fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.commit()
                }
            }
        )

        binding.FavRecycleView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FavoritFragment.adapter
        }
    }


    private fun showDeleteConfirmationDialog(city: FavoriteCity) {
        if (!isInternetAvailable()) {
            // Show no internet dialog
            showNoInternetDialog()
        } else {
            // Proceed with delete confirmation
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Favorite City")
                .setMessage("Are you sure you want to delete ${city.cityName} from favorites?")
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deleteFavoriteCity(city)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    // Check if internet is available
    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

    // Show custom no internet dialog
    private fun showNoInternetDialog() {
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
                // Retry logic if internet becomes available
            } else {
                showNoInternetDialog() // Show dialog again if still no internet
            }
        }

        cancelButton.setOnClickListener {
            customDialog.dismiss()
        }

        customDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
