package com.example.cloudvibe.alert.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudvibe.alert.alarm.AlarmReceiver
import com.example.cloudvibe.model.database.AlarmData
import com.example.cloudvibe.model.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    // Define a MutableStateFlow that holds a list of AlarmData
    private val _alarmsFlow = MutableStateFlow<List<AlarmData>>(emptyList())
    val alarmsFlow: StateFlow<List<AlarmData>>  = weatherRepository.getAllLocalAlarms().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        fetchAlarms()
    }

    private fun fetchAlarms() {
        viewModelScope.launch {
            weatherRepository.getAllLocalAlarms().collect { alarmsList ->
                Log.d("AlarmViewModel", "Alarms received: $alarmsList")
                _alarmsFlow.value = alarmsList // Emit to StateFlow
            }
        }
    }

    fun insertAlarm(alarmData: AlarmData) {
        viewModelScope.launch {
            weatherRepository.insertAlarm(alarmData)
        }
    }

    // Function to delete old alarms (alarms with time earlier than current time)
    fun deleteOldAlarms() {
        viewModelScope.launch {
            val currentTimeMillis = System.currentTimeMillis()
            weatherRepository.deleteOldAlarms(currentTimeMillis)
            Log.d("AlarmViewModel", "Deleted alarms older than $currentTimeMillis")
        }
    }
    fun deleteAlarm(context: Context, alarmData: AlarmData) {

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmRequestCode = alarmData.requestCode

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "Alarm"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)

        pendingIntent.cancel()

        viewModelScope.launch {
            weatherRepository.deleteAlarm(alarmData)
        }

    }

}