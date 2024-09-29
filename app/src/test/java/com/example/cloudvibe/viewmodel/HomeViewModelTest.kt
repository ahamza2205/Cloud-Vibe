package com.example.cloudvibe.viewmodel

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cloudvibe.home.viewmodel.HomeViewModel
import com.example.cloudvibe.model.database.ForecastData
import com.example.cloudvibe.model.database.WeatherEntity
import com.example.cloudvibe.model.repository.WeatherRepository
import com.example.cloudvibe.FakeWeatherData.getFakeForecastResponse
import com.example.cloudvibe.home.view.ApiState
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import java.util.logging.Logger

@RunWith(AndroidJUnit4::class)
class HomeViewModelTest {

    @get:Rule
    private val repository: WeatherRepository = Mockito.mock(WeatherRepository::class.java)
    private val logger: Logger = Mockito.mock(Logger::class.java)
    private lateinit var homeViewModel: HomeViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        homeViewModel = HomeViewModel(repository, logger)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

// ------------------------------------- weather data test cases -------------------------------------
@Test
fun testFetchAndDisplayWeather() = runTest {
    //  Successful response
    val dummyWeatherList = listOf(
        WeatherEntity(1, "Alexandria", "EG", "1727521961", 27.74f, "clear sky", 4.75, 55, 1727521961, 1011, 1727495515, 1727538554)
    )
    whenever(repository.getWeatherFromApiAndSaveToLocal(31.255884, 29.987537, "en"))
        .thenReturn(flowOf(dummyWeatherList))

    homeViewModel.fetchAndDisplayWeather(31.255884, 29.987537)
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(ApiState.Success(dummyWeatherList), homeViewModel.weatherState.value)

    //  Empty response
    whenever(repository.getWeatherFromApiAndSaveToLocal(31.255884, 29.987537, "en"))
        .thenReturn(flowOf(emptyList()))

    // empty list
    homeViewModel.fetchAndDisplayWeather(31.255884, 29.987537)
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(ApiState.Success(emptyList<WeatherEntity>()), homeViewModel.weatherState.value) // Expect empty list

    //  An error occurs
    whenever(repository.getWeatherFromApiAndSaveToLocal(31.255884, 29.987537, "en"))
        .thenThrow(RuntimeException("Network error"))

    homeViewModel.fetchAndDisplayWeather(31.255884, 29.987537)
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(homeViewModel.weatherState.value is ApiState.Error) // Check if it's an error state
}

   // ------------------------------- Forecast Data -------------------------------
    @Test
    fun testFetchAndDisplayForecast() = runTest {
        // Using fake data
        val fakeForecastResponse = getFakeForecastResponse()

        // Convert fake data to a list of ForecastData
        val dummyForecastList = fakeForecastResponse.list.map { forecastItem ->
            ForecastData(
                dt = forecastItem.dt,
                maxTemp = forecastItem.main.temp_max,
                minTemp = forecastItem.main.temp_min,
                date = forecastItem.dt_txt.split(" ")[0], // Extract date from dt_txt
                time = forecastItem.dt_txt.split(" ")[1], // Extract time from dt_txt
                weather = forecastItem.weather
            )
        }

        // Mocking the repository response
        whenever(repository.fetchForecastFromApiAndSave(1.0, 1.0, "en"))
            .thenReturn(flowOf(dummyForecastList))

        val job = launch {
            homeViewModel.savedForecast.collect { savedForecast ->
                // Check if savedForecast is not empty, then assert
                if (savedForecast.isNotEmpty()) {
                    assertEquals(dummyForecastList, savedForecast)
                }
            }
        }

        homeViewModel.fetchAndDisplayForecast(1.0, 1.0)

        // Advance the coroutine dispatcher
        testDispatcher.scheduler.advanceUntilIdle()

        job.cancel()
    }

    // Test if the list is empty when the repository returns an empty list
    @Test
    fun testFetchAndDisplayForecast_EmptyList() = runTest {
        val emptyForecastList = listOf<ForecastData>()

        whenever(repository.fetchForecastFromApiAndSave(1.0, 1.0, "en"))
            .thenReturn(flowOf(emptyForecastList))

        val job = launch {
            homeViewModel.savedForecast.collect { savedForecast ->
                assertTrue(savedForecast.isEmpty())
            }
        }

        homeViewModel.fetchAndDisplayForecast(1.0, 1.0)
        testDispatcher.scheduler.advanceUntilIdle()
        job.cancel()
    }
  // ------------------------------- Settings test cases -------------------------------
    @Test
    fun testGetUnits() {
        // Scenario 1: Repository returns a specific unit
        whenever(repository.getUnits()).thenReturn("imperial")
        val result = homeViewModel.getUnits()
        assertEquals("imperial", result)

        // Scenario 2: Repository returns null, should default to "metric"
        whenever(repository.getUnits()).thenReturn(null)
        val resultDefault = homeViewModel.getUnits()
        assertEquals("metric", resultDefault)
    }


    @Test
    fun testGetWindSpeedUnit() {
        // Scenario 1: Repository returns a specific wind speed unit
        whenever(repository.getWindSpeedUnit()).thenReturn("m/s")
        val result = homeViewModel.getWindSpeedUnit()
        assertEquals("m/s", result)

        // Scenario 2: Repository returns null, should default to "km/h"
        whenever(repository.getWindSpeedUnit()).thenReturn(null)
        val resultDefault = homeViewModel.getWindSpeedUnit()
        assertEquals("km/h", resultDefault)
    }


// ---------------------------- Location test cases ----------------------------
    @Test
    fun testGetLocation() {
        // Scenario 1: Repository returns a valid location
        val location = Pair(31.255884, 29.987537)
        whenever(repository.getLocation()).thenReturn(location)
        val result = homeViewModel.getLocation()
        assertEquals(location, result)

        // Scenario 2: Repository returns null
        whenever(repository.getLocation()).thenReturn(null)
        val resultNull = homeViewModel.getLocation()
        assertNull(resultNull)
    }
}
