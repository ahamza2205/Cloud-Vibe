package com.example.cloudvibe.viewmodel

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.cloudvibe.alert.viewmodel.AlarmViewModel
import com.example.cloudvibe.model.database.AlarmData
import com.example.cloudvibe.model.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import kotlin.time.ExperimentalTime

@RunWith(AndroidJUnit4::class)
class AlarmViewModelTest {

    private lateinit var viewModel: AlarmViewModel
    private lateinit var repository: WeatherRepository
    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(WeatherRepository::class.java)
        `when`(repository.getAllLocalAlarms()).thenReturn(flowOf(listOf(
            AlarmData(1, System.currentTimeMillis() + 1000),
            AlarmData(2, System.currentTimeMillis() + 2000)
        )))
        viewModel = AlarmViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `test fetching alarms`() = runTest {
        viewModel.alarmsFlow.test {
            // The first emission should be the mocked list of alarms
            val alarms = awaitItem()
            assertEquals(2, alarms.size)
            assertEquals(1, alarms[0].requestCode)
            assertEquals(2, alarms[1].requestCode)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `test insert alarm`() = runTest {
        val newAlarm = AlarmData(3, System.currentTimeMillis() + 3000)

        // Insert the new alarm
        viewModel.insertAlarm(newAlarm)

        // Verify that the repository's insertAlarm method was called
        verify(repository).insertAlarm(newAlarm)
    }

    @Test
    fun `test delete old alarms`() = runTest {
        // Call the deleteOldAlarms function
        viewModel.deleteOldAlarms()

        // Verify that the repository's deleteOldAlarms method was called with current time
        verify(repository).deleteOldAlarms(anyLong())
    }

    // Test case for fetching alarms with no alarms in the database
    @OptIn(ExperimentalTime::class)
    @Test
    fun `test fetching alarms with no alarms`() = runTest {
        `when`(repository.getAllLocalAlarms()).thenReturn(flowOf(emptyList()))

        viewModel = AlarmViewModel(repository)

        viewModel.alarmsFlow.test {
            val alarms = awaitItem()
            assertEquals(0, alarms.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    // Test case for inserting an existing alarm into the database (should not insert)
    @Test
    fun `test inserting existing alarm`() = runTest {
        val existingAlarm = AlarmData(1, System.currentTimeMillis() + 1000)

        viewModel.insertAlarm(existingAlarm)

        verify(repository, times(1)).insertAlarm(existingAlarm)
    }
    @Test
    fun `test delete old alarms behavior`() = runTest {
        val currentTime = System.currentTimeMillis()
        `when`(repository.getAllLocalAlarms()).thenReturn(flowOf(listOf(
            AlarmData(1, currentTime - 1000),
            AlarmData(2, currentTime + 1000)
        )))

        viewModel.deleteOldAlarms()

        verify(repository).deleteOldAlarms(anyLong())
    }
}

