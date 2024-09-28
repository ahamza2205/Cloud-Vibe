package com.example.cloudvibe.model.repository

import com.example.cloudvibe.model.database.AlarmData
import kotlinx.coroutines.flow.Flow

interface WeatherRepositoryInterFace {

    fun getAllLocalAlarms(): Flow<List<AlarmData>>
    suspend fun insertAlarm(alarmData: AlarmData)
    suspend fun deleteOldAlarms(currentTimeMillis: Long)
}