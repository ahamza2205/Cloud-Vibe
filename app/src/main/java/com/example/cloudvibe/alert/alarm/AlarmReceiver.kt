package com.example.cloudvibe.alert.alarm


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

            when (intent?.action) {
                "Alarm" -> {
                    // Start the overlay service for alarm
                    val serviceIntent = Intent(context, OverlayService::class.java)
                    context?.startService(serviceIntent)
                }
                "Notification" -> {
                    // Show a notification for the scheduled time
                    showNotification(context)
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotification(context: Context?) {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = Notification.Builder(context, "channel_id")
            .setContentTitle("Weather Notification")
            .setContentText("It's time to check the weather!")
            .setSmallIcon(R.drawable.appstore)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(0, notification)
    }
}
