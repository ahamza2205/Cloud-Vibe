package com.example.cloudvibe.alert

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.example.cloudvibe.R


class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var mediaPlayer: MediaPlayer

    override fun onBind(intent: Intent?): IBinder? {
        return null // Not binding to any activity
    }

    override fun onCreate() {
        super.onCreate()

        // Create overlay view
        overlayView = LayoutInflater.from(this).inflate(R.layout.alarm_dialog, null)

        // Initialize MediaPlayer
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        mediaPlayer = MediaPlayer.create(this, alarmUri)
        mediaPlayer.start()

        // Set layout parameters for the overlay view
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        // Add the view to the window
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.addView(overlayView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove the view from the window when the service is destroyed
        if (::overlayView.isInitialized) windowManager.removeView(overlayView)
        mediaPlayer.release() // Release the media player
    }
}

