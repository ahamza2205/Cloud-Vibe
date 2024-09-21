package com.example.cloudvibe.alert

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.cloudvibe.R
import com.example.cloudvibe.activity.MainActivity
import com.example.cloudvibe.model.repository.WeatherRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var weatherRepository: WeatherRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            Log.d("AlarmReceiver", "Alarm received")

            // إعداد mediaPlayer
            val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val mediaPlayer = MediaPlayer.create(context, alarmUri)
            mediaPlayer.start()

            // أرسل Intent إلى MainActivity
            val alarmIntent = Intent(context, MainActivity::class.java)
            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // يضمن أن تفتح النشاط بشكل صحيح
            alarmIntent.putExtra("SHOW_DIALOG", true) // استخدم هذا لإخبار النشاط بفتح الـ Dialog
            context.startActivity(alarmIntent)

            // تابع في جمع بيانات الطقس
            try {
                val lat = intent.getDoubleExtra("LATITUDE", 0.0)
                val lon = intent.getDoubleExtra("LONGITUDE", 0.0)
                val apiKey = intent.getStringExtra("API_KEY") ?: "7af08d0e1d543aea9b340405ceed1c3d"
                val units = intent.getStringExtra("UNITS") ?: "metric"
                val language = intent.getStringExtra("LANGUAGE") ?: "en"

                CoroutineScope(Dispatchers.IO).launch {
                    val weatherFlow = weatherRepository.getWeatherFromApiAndSaveToLocal(lat, lon, units, language, apiKey)

                    weatherFlow.collect { weatherList ->
                        if (weatherList.isNotEmpty()) {
                            val weather = weatherList[0]
                            showNotification(context, weather.description)
                        }
                    }
                }
            } catch (e: SecurityException) {
                Log.e("AlarmReceiver", "Location permission denied: ${e.message}")
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Error occurred: ${e.message}")
            }
        }
    }

    private fun showNotification(context: Context, weatherDescription: String) {
        val notificationBuilder = NotificationCompat.Builder(context, "channel_id")
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle("Weather Alert")
            .setContentText("Current Weather: $weatherDescription")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(1, notificationBuilder.build())
        }
    }

    private fun showAlarmDialog(context: Context, mediaPlayer: MediaPlayer) {
        val dialog = AlarmDialog(context)
        dialog.setMediaPlayer(mediaPlayer)
        dialog.show()
    }
}
