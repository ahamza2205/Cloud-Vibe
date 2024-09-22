package com.example.cloudvibe.alert

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.example.cloudvibe.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmDialog : DialogFragment() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.alarm_dialog, container, false)

        val stopButton: Button = view.findViewById(R.id.stop_button)
        val snoozeButton: Button = view.findViewById(R.id.snooze_button)

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

        return view
    }

}
