package com.example.cloudvibe.alert

import android.app.Dialog
import android.content.Context
import android.media.MediaPlayer
import android.view.Window
import android.widget.Button
import com.example.cloudvibe.R

class AlarmDialog(context: Context) : Dialog(context) {

    private var mediaPlayer: MediaPlayer? = null

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.alarm_dialog)

        val stopButton: Button = findViewById(R.id.stop_button)
        val snoozeButton: Button = findViewById(R.id.snooze_button)

        stopButton.setOnClickListener {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            dismiss()
        }

        snoozeButton.setOnClickListener {
            mediaPlayer?.pause()
            mediaPlayer?.release()
            dismiss()
        }
    }

    fun setMediaPlayer(mp: MediaPlayer) {
        mediaPlayer = mp
    }
}
