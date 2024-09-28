package com.example.cloudvibe.repository

import com.example.cloudvibe.FakeWeatherData
import com.example.cloudvibe.model.database.AlarmData
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
import org.mockito.Mock
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
    // ------------------------------ shared preferences tests here -----------------------------------

// test for null location
    @Test
    fun `test get location returns null when no location is saved`() {
        runTest {
            // Arrange
            `when`(sharedPreferencesHelper.getLocation()).thenReturn(null)

            // Act
            val location = weatherRepository.getLocation()

            // Assert
            verify(sharedPreferencesHelper).getLocation()
            assertEquals(null, location)
        }
    }
   // test
    @Test
    fun `test save and retrieve new language`() {
        runTest {
            // Arrange
            val newLanguage = "fr"
            `when`(sharedPreferencesHelper.getLanguage()).thenReturn(newLanguage)

            // Act
            val retrievedLanguage = weatherRepository.getLanguage()

            // Assert
            verify(sharedPreferencesHelper).getLanguage()
            assertEquals(newLanguage, retrievedLanguage)
        }
    }

    @Test
    fun `test retrieve language from shared preferences`() {
        runTest {
            // Arrange
            val language = "en"
            // Mock the getLanguage behavior
            `when`(sharedPreferencesHelper.getLanguage()).thenReturn(language)

            // Act
            val retrievedLanguage = weatherRepository.getLanguage()

            // Assert
            verify(sharedPreferencesHelper).getLanguage()
            assertEquals(language, retrievedLanguage)
        }
    }

    @Test
    fun `test retrieve units from shared preferences`() {
        runTest {
            // Arrange
            val units = "metric"
            // Mock the getUnits behavior
            `when`(sharedPreferencesHelper.getUnits()).thenReturn(units)

            // Act
            val retrievedUnits = weatherRepository.getUnits()

            // Assert
            verify(sharedPreferencesHelper).getUnits()
            assertEquals(units, retrievedUnits)
        }
    }

    @Test
    fun `test retrieve wind speed unit from shared preferences`() {
        runTest {
            // Arrange
            val windSpeedUnit = "km/h"
            // Mock the getWindSpeedUnit behavior
            `when`(sharedPreferencesHelper.getWindSpeedUnit()).thenReturn(windSpeedUnit)

            // Act
            val retrievedWindSpeedUnit = weatherRepository.getWindSpeedUnit()

            // Assert
            verify(sharedPreferencesHelper).getWindSpeedUnit()
            assertEquals(windSpeedUnit, retrievedWindSpeedUnit)
        }
    }

    @Test
    fun `test get language returns default when no language is saved`() {
        runTest {
            // Arrange
            `when`(sharedPreferencesHelper.getLanguage()).thenReturn(null)

            // Act
            val language = weatherRepository.getLanguage()

            // Assert
            verify(sharedPreferencesHelper).getLanguage()
            assertEquals(null, language)
        }
    }
 // -------------------------------------- Alarm tests here---------------------------------------------

    @Test
    fun `test insert alarm successfully`() = runTest {
        // Arrange
        val alarmData = AlarmData(requestCode = 101, time = 1632994981000L)

        // Act
        weatherRepository.insertAlarm(alarmData)

        // Assert
        verify(weatherDao).insertAlarm(alarmData)
    }
    @Test
    fun `test get all local alarms successfully`() = runTest {
        // Arrange
        val alarmDataList = listOf(
            AlarmData(requestCode = 101, time = 1632994981000L),
            AlarmData(requestCode = 102, time = 1632999999000L)
        )

        `when`(weatherDao.getAllAlarms()).thenReturn(flowOf(alarmDataList))

        // Act
        val alarmsFlow = weatherRepository.getAllLocalAlarms().toList()

        // Assert
        assertEquals(alarmDataList, alarmsFlow.first())
    }

    @Test
    fun `test delete old alarms successfully`() = runTest {
        // Arrange
        val currentTimeMillis = System.currentTimeMillis()

        // Act
        weatherRepository.deleteOldAlarms(currentTimeMillis)

        // Assert
        verify(weatherDao).deleteOldAlarms(currentTimeMillis)
    }

    @Test
    fun `test get all local alarms returns empty list`() = runTest {
        // Arrange
        `when`(weatherDao.getAllAlarms()).thenReturn(flowOf(emptyList()))

        // Act
        val alarmsFlow = weatherRepository.getAllLocalAlarms().toList()

        // Assert
        assertTrue(alarmsFlow.first().isEmpty())
    }
    @Test
    fun `test delete old alarms when no old alarms exist`() = runTest {
        // Arrange
        val currentTimeMillis = System.currentTimeMillis()

        // Act
        weatherRepository.deleteOldAlarms(currentTimeMillis)

        // Assert
        verify(weatherDao).deleteOldAlarms(currentTimeMillis)
    }
}

