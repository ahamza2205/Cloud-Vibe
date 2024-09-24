package com.example.cloudvibe.favorit.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cloudvibe.R
import com.example.cloudvibe.activity.SharedViewModel
import com.example.cloudvibe.databinding.FragmentFavoritBinding
import com.example.cloudvibe.favorit.favdetil.FavoritWeatherFragment
import com.example.cloudvibe.favorit.viewmodel.FavoritViewModel
import com.example.cloudvibe.map.MapFragment
import dagger.hilt.android.AndroidEntryPoint
import android.app.AlertDialog
import com.example.cloudvibe.model.database.FavoriteCity

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
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        //  ViewModel
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
                sharedViewModel.detailsLocation.value = city
                val favoritWeatherFragment = FavoritWeatherFragment()
                val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragment_container, favoritWeatherFragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            }
        )

        binding.FavRecycleView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FavoritFragment.adapter
        }
    }

    private fun showDeleteConfirmationDialog(city: FavoriteCity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Favorite City")
            .setMessage("Are you sure you want to delete ${city.cityName} from favorites?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteFavoriteCity(city)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
