package com.example.cloudvibe.model.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_table")
data class AlarmData(
    @PrimaryKey val requestCode :Int,
    val time:Long
)