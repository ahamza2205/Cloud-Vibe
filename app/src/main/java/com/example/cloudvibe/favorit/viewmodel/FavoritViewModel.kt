    package com.example.cloudvibe.favorit.viewmodel

    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.example.cloudvibe.model.database.FavoriteCity
    import com.example.cloudvibe.model.repository.WeatherRepository
    import dagger.hilt.android.lifecycle.HiltViewModel
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.launch
    import javax.inject.Inject


    @HiltViewModel
    class FavoritViewModel @Inject constructor(
        private val weatherRepository: WeatherRepository
    ) : ViewModel() {

        private val _favoriteCities = MutableStateFlow<List<FavoriteCity>>(emptyList())
        val favoriteCities: StateFlow<List<FavoriteCity>> = _favoriteCities

        init {
            fetchFavoriteCities()
        }

        private fun fetchFavoriteCities() {
            viewModelScope.launch {
                weatherRepository.getAllFavoriteCities().collect { cities ->
                    _favoriteCities.value = cities
                }
            }
        }

        fun deleteFavoriteCity(city: FavoriteCity) {
            viewModelScope.launch {
                weatherRepository.deleteFavoriteCity(city)
            }
        }
    }


