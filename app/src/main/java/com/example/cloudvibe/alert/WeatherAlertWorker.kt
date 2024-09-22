package com.example.cloudvibe.alert

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import androidx.work.CoroutineWorker
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.cloudvibe.R
import com.example.cloudvibe.model.repository.WeatherRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class WeatherAlertWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val weatherRepository: WeatherRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val mediaPlayer = MediaPlayer.create(applicationContext, alarmUri)
        mediaPlayer.start()


        val lat = inputData.getDouble("LATITUDE", 0.0)
        val lon = inputData.getDouble("LONGITUDE", 0.0)
        val apiKey = inputData.getString("API_KEY") ?: "7af08d0e1d543aea9b340405ceed1c3d"
        val units = inputData.getString("UNITS") ?: "metric"
        val language = inputData.getString("LANGUAGE") ?: "en"

        try {
            val weatherFlow = weatherRepository.getWeatherFromApiAndSaveToLocal(lat, lon, units, language, apiKey)

            weatherFlow.collect { weatherList ->
                if (weatherList.isNotEmpty()) {
                    val weather = weatherList[0]
                    showNotification(weather.description)
                }
            }
        } catch (e: Exception) {
            return Result.failure()
        }

        return Result.success()
    }

    private fun showNotification(weatherDescription: String) {
        val notificationBuilder = NotificationCompat.Builder(applicationContext, "channel_id")
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle("Weather Alert")
            .setContentText("Current Weather: $weatherDescription")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(1, notificationBuilder.build())
        }
    }
}
