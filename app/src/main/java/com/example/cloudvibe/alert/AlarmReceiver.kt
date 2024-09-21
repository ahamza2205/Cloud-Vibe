package com.example.cloudvibe.alert


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Here you can trigger a notification or execute some logic when the alarm goes off
        Toast.makeText(context, "Alarm triggered!", Toast.LENGTH_SHORT).show()
    }
}
