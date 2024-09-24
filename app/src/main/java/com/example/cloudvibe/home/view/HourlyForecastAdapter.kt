package com.example.cloudvibe.home.view

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.cloudvibe.R
import com.example.cloudvibe.model.network.data.Hourly
import com.example.cloudvibe.sharedpreferences.SharedPreferencesHelper
import com.example.cloudvibe.utils.UnitConverter
import com.example.cloudvibe.utils.UnitConverter.getWeatherIconResource
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HourlyForecastAdapter(
    private var dataList: List<Hourly>,
    private var unitSymbol: String,
    private var speedSymbol: String
) : RecyclerView.Adapter<HourlyForecastAdapter.MyViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hourlyforecast, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun updateList(newList: List<Hourly>, newUnitSymbol: String, newSpeedSymbol: String) {
        dataList = newList
        unitSymbol = newUnitSymbol
        speedSymbol = newSpeedSymbol
        // Notify RecyclerView to update its items
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentHour: Hourly = dataList[position]
        val icon = currentHour.weather[0].icon
        val iconResource = getWeatherIconResource(icon)
        holder.iconView.setImageResource(iconResource)

        val convertedTemp = currentHour.temp?.let { convertTemperature(it, unitSymbol) }
        val formattedTemp = String.format(Locale.getDefault(), "%.2f %s", convertedTemp, unitSymbol)
        val localHour = getLocalHourFromUnixTimestamp(currentHour.dt)
        val formattedTime = formatHour(localHour)
        Log.i("Hour", "onBindViewHolder: $localHour")

        holder.degreeTV.text = formattedTemp
        holder.timeTV.text = formattedTime
    }

    private fun convertTemperature(tempInCelsius: Float, unit: String): Float {
        return when (unit) {
            "K" -> tempInCelsius + 273.15f
            "F" -> (tempInCelsius * 9 / 5) + 32
            else -> tempInCelsius  // Celsius by default
        }
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val degreeTV: TextView = itemView.findViewById(R.id.hour_degree)
        val timeTV: TextView = itemView.findViewById(R.id.hour_time)
        val iconView: ImageView = itemView.findViewById(R.id.hour_imageView)
    }

    private fun getLocalHourFromUnixTimestamp(gmtUnixTimestamp: Long): Int {
        val date = Date(gmtUnixTimestamp * 1000L)  // Convert seconds to milliseconds
        val timeZone = TimeZone.getDefault()
        val sdf = SimpleDateFormat("HH", Locale.getDefault())
        sdf.timeZone = timeZone
        return sdf.format(date).toInt()
    }

    private fun formatHour(hour: Int): String {
        val dayNight = if (hour >= 12) "PM" else "AM"
        val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
        return "$displayHour $dayNight"
    }
}

