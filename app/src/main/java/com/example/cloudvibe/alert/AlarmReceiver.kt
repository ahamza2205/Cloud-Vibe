package com.example.cloudvibe.alert

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("AlarmReceiver", "Alarm Received")
        val serviceIntent = Intent(context, OverlayService::class.java)
        context?.startService(serviceIntent) // Start the overlay service to display the alert

    }
}
