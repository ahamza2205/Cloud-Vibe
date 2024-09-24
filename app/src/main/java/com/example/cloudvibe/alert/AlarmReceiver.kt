package com.example.cloudvibe.alert


import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.cloudvibe.R
import com.example.cloudvibe.activity.MainActivity


class AlarmReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("AlarmReceiver", "Alarm Received")

        // Retrieve the scheduled time and type from the intent
        val scheduledTime = intent?.getLongExtra("SCHEDULED_TIME", -1L) ?: -1L

        val alertType = intent?.getStringExtra("ALERT_TYPE") ?: "ALARM"

        // Check if the current time has reached or passed the scheduled time
        if (scheduledTime != -1L && System.currentTimeMillis() >= scheduledTime) {
            when (alertType) {
                "ALARM" -> {
                    // Start the overlay service for alarm
                    val serviceIntent = Intent(context, OverlayService::class.java)
                    context?.startService(serviceIntent)
                    Log.d("AlarmReceiver", "Alarm Triggered and Overlay Service Started")
                }
                "NOTIFICATION" -> {
                    // Show a notification for the scheduled time
                    showNotification(context)
                    Log.d("AlarmReceiver", "Notification Triggered")
                }
            }
        } else {
            Log.d("AlarmReceiver", "Alarm or Notification is not yet due: $scheduledTime")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotification(context: Context?) {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = Notification.Builder(context, "channel_id")
            .setContentTitle("Alert")
            .setContentText("It's time to check the weather!")
            .setSmallIcon(R.drawable.alarms)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(0, notification)
    }
}
