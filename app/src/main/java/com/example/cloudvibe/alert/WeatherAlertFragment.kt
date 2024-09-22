package com.example.cloudvibe.alert

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.cloudvibe.R
import com.example.cloudvibe.activity.MainActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import java.util.concurrent.TimeUnit

class WeatherAlertFragment : Fragment() {

    private val notificationChannelId = "channel_id"

    companion object {
        private const val REQUEST_CODE_OVERLAY_PERMISSION = 1001
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_weather_alert, container, false)

        createNotificationChannel()

        val fabAddAlert: FloatingActionButton = view.findViewById(R.id.fab_add_alert)
        fabAddAlert.setOnClickListener {
            showAlertDialog()
        }

        return view
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "Weather Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for Weather Alert Notifications"
            }
            val notificationManager = requireContext().getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showAlertDialog() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)

                        showTypeDialog(calendar)
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showTypeDialog(calendar: Calendar) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Type")

        val types = arrayOf("Alarm", "Notification")
        builder.setItems(types) { _, which ->
            when (which) {
                0 -> checkAlarmPermission(calendar)
                1 -> checkNotificationPermission(calendar)
            }
        }
        builder.show()
    }

    private fun checkAlarmPermission(calendar: Calendar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestExactAlarmPermission(calendar)
        } else {
            setAlarm(calendar)
        }
    }

    @SuppressLint("NewApi")
    private fun checkNotificationPermission(calendar: Calendar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission(calendar)
        } else {
            showNotification(calendar)
        }
    }


    private fun setAlarm(calendar: Calendar) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java)

        // Pass the scheduled time
        intent.putExtra("SCHEDULED_TIME", calendar.timeInMillis)
        intent.putExtra("LATITUDE", 31.2156)
        intent.putExtra("LONGITUDE", 29.9553)
        intent.putExtra("API_KEY", "7af08d0e1d543aea9b340405ceed1c3d")
        intent.putExtra("UNITS", "metric")
        intent.putExtra("LANGUAGE", "en")

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set the alarm
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        // Check Overlay Permission
        checkOverlayPermission()

        Toast.makeText(requireContext(), "Alarm Set Successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun checkOverlayPermission() {
        if (Settings.canDrawOverlays(requireContext())) {
            // Start the Overlay Service
            val intent = Intent(requireContext(), OverlayService::class.java)
            requireActivity().startService(intent) // استخدام requireActivity()
        } else {
            // Request the Overlay permission
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${requireContext().packageName}")) // استخدام requireContext()
            startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION) // لا تحتاج إلى requireActivity() هنا
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotification(calendar: Calendar) {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(requireContext(), MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = Notification.Builder(requireContext(), notificationChannelId)
            .setContentTitle("Alert")
            .setContentText("Don't forget to check weather today!: ${calendar.time}")
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(0, notification)
        Toast.makeText(requireContext(), "Notification Set!", Toast.LENGTH_SHORT).show()
        scheduleWeatherAlert(calendar) // Schedule the weather alert

    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestExactAlarmPermission(calendar: Calendar) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
        } else {
            setAlarm(calendar)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission(calendar: Calendar) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        } else {
            showNotification(calendar)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(requireContext(), "Notification Permission Granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Notification Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scheduleWeatherAlert(calendar: Calendar) {
        val data = workDataOf(
            "LATITUDE" to 31.2156,
            "LONGITUDE" to 29.9553,
            "API_KEY" to "7af08d0e1d543aea9b340405ceed1c3d",
            "UNITS" to "metric",
            "LANGUAGE" to "en"
        )

        val workRequest = OneTimeWorkRequestBuilder<WeatherAlertWorker>()
            .setInitialDelay(calculateDelay(calendar), TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(requireContext()).enqueue(workRequest)
    }

    private fun calculateDelay(calendar: Calendar): Long {
        val currentTime = System.currentTimeMillis()
        return calendar.timeInMillis - currentTime
    }
}
