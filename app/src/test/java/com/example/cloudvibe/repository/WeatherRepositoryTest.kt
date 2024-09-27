package com.example.cloudvibe.repository

import com.example.cloudvibe.model.database.FavoriteCity
import com.example.cloudvibe.model.database.WeatherDao
import com.example.cloudvibe.model.network.WeatherApiService
import com.example.cloudvibe.model.repository.WeatherRepository
import com.example.cloudvibe.sharedpreferences.SharedPreferencesHelper
import com.example.cloudvibe.utils.WeatherMapper
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import retrofit2.Response
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class WeatherRepositoryTest {
    @Mock
    private lateinit var weatherApiService: WeatherApiService
    @Mock
    private lateinit var weatherDao: WeatherDao
    @Mock
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var weatherRepository: WeatherRepository
    @Before
    fun setUp() {
        weatherRepository = WeatherRepository(weatherApiService, weatherDao, sharedPreferencesHelper)
    }


    // ---------------------------Weather tests here----------------------------------------
    @Test
    fun `test fetch weather and save to local successfully`() {
        runTest {
            // Arrange
            val fakeWeatherResponse = FakeWeatherData.getWeatherResponse()
            val fakeWeatherEntity = FakeWeatherData.getWeatherEntity()

            // Mock API response
            `when`(weatherApiService.getCurrentWeather(10.0, 20.0, "en"))
                .thenReturn(fakeWeatherResponse)

            // Mock DAO response
            `when`(weatherDao.getAllWeather()).thenReturn(flowOf(listOf(fakeWeatherEntity)))

            // Act
            val weatherFlow = weatherRepository.getWeatherFromApiAndSaveToLocal(10.0, 20.0, "en").toList()

            // Assert
            verify(weatherDao).deleteAllWeather()
            verify(weatherDao).insertWeather(fakeWeatherEntity)

            assertEquals(listOf(fakeWeatherEntity), weatherFlow.first())
        }
    }

    @Test
    fun `test get saved weather returns expected data`() {
        runTest {
            // Get the fake weather entity
            val fakeWeatherEntity = FakeWeatherData.getWeatherEntity()
            // Mock DAO response
            `when`(weatherDao.getAllWeather()).thenReturn(flowOf(listOf(fakeWeatherEntity)))

            // Get the saved weather
            val savedWeatherFlow = weatherRepository.getSavedWeather().toList()

            // Assert Check if returned data matches expected data
            assertEquals(listOf(fakeWeatherEntity), savedWeatherFlow.flatten())
        }
    }
 // test for empty data here
    @Test
    fun `test get saved weather returns empty data`() {
        runTest {
            // Arrange
            `when`(weatherDao.getAllWeather()).thenReturn(flowOf(emptyList()))

            // Act
            val savedWeatherFlow = weatherRepository.getSavedWeather().toList()

            // Assert
            assertTrue(savedWeatherFlow.flatten().isEmpty())
        }
    }

    // ----------------------------Forecast tests here----------------------------------------
    @Test
    fun `test fetch forecast and save to local successfully`() {
        runTest {
            // Arrange
            val fakeForecastResponse = FakeWeatherData.getFakeForecastResponse() // Use fake data for the forecast
            val fakeForecastData = WeatherMapper.mapForecastResponseToData(fakeForecastResponse)

            // Mock API response
            `when`(weatherApiService.getForecastWeather(1.0, 1.0, "en"))
                .thenReturn(Response.success(fakeForecastResponse))

            // Mock DAO response
            `when`(weatherDao.getAllForecasts()).thenReturn(flowOf(fakeForecastData))
            // Act
            val forecastFlow = weatherRepository.fetchForecastFromApiAndSave(1.0, 1.0, "en").toList()
            // Assert Ensure all existing data is cleared
            verify(weatherDao).clearForecasts()
            // Ensure new data is inserted
            verify(weatherDao).insertForecast(fakeForecastData)
            // Updated assertion to match the actual output
            assertEquals(fakeForecastData, forecastFlow.first()) // Check that the expected data matches the returned data
        }
    }
    @Test
    fun `test get saved forecast returns expected data`() {
        runTest {
            // Arrange
            val fakeForecastResponse = FakeWeatherData.getFakeForecastResponse() // Get the fake response
            val fakeForecastData = WeatherMapper.mapForecastResponseToData(fakeForecastResponse) // Convert it to ForecastData
            `when`(weatherDao.getAllForecasts()).thenReturn(flowOf(fakeForecastData)) // Mock DAO response
            // Act
            val savedForecastFlow = weatherRepository.getSavedForecast().toList()
            // Assert
            assertEquals(listOf(fakeForecastData), savedForecastFlow) // Check if returned data matches expected
        }
    }

     // -------------------------------------------Favorite city tests here---------------------------------------------
@Test
fun `test insert favorite city`() {
    runTest {
        // Arrange
        val favoriteCity = FavoriteCity(cityName = "Cairo", latitude = 30.0, longitude = 31.0)

        // Act
        weatherRepository.insertFavoriteCity(favoriteCity)

        // Assert
        verify(weatherDao).insert(favoriteCity)
    }
}

    @Test
    fun `test get all favorite cities returns expected data`() {
        runTest {
            // Arrange
            val favoriteCity = FavoriteCity(cityName = "Cairo", latitude = 30.0, longitude = 31.0)
            `when`(weatherDao.getAllLocal()).thenReturn(flowOf(listOf(favoriteCity)))

            // Act
            val favoriteCitiesFlow = weatherRepository.getAllFavoriteCities().toList()

            // Assert
            assertEquals(listOf(favoriteCity), favoriteCitiesFlow.first())
        }
    }

    @Test
    fun `test delete favorite city`() {
        runTest {
            // Arrange
            val favoriteCity = FavoriteCity(cityName = "Cairo", latitude = 30.0, longitude = 31.0)

            // Act
            weatherRepository.deleteFavoriteCity(favoriteCity)

            // Assert
            verify(weatherDao).delete(favoriteCity)
        }
    }

}

