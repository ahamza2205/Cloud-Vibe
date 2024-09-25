package com.example.cloudvibe.alert.view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.cloudvibe.databinding.AlarmItemBinding
import com.example.cloudvibe.model.database.AlarmData
import java.text.SimpleDateFormat
import java.util.Locale

class AlarmDiff:DiffUtil.ItemCallback<AlarmData>(){
    override fun areItemsTheSame(oldItem: AlarmData, newItem: AlarmData): Boolean {
        return oldItem.requestCode==newItem.requestCode
    }

    override fun areContentsTheSame(oldItem: AlarmData, newItem: AlarmData): Boolean {
        return oldItem==newItem
    }
}

class AlarmAdapter :ListAdapter<AlarmData,AlarmAdapter.AlarmViewHolder>(AlarmDiff())
{

    lateinit var binding: AlarmItemBinding
    class AlarmViewHolder( var binding: AlarmItemBinding):ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val inflater : LayoutInflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding= AlarmItemBinding.inflate(inflater,parent,false)
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val currentAlarm = getItem(position)
        Log.d("AlarmAdapter", "Binding alarm at position $position: $currentAlarm")

        // Convert time and set to TextView
        val timeText = convertMilliSecondsToTime(currentAlarm.time, "hh:mm")
        holder.binding.cityName.text = timeText
    }


    fun convertMilliSecondsToTime(milliSeconds: Long, pattern: String): String
    {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())


        return dateFormat.format(milliSeconds)
    }
}