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
import com.example.cloudvibe.utils.UnitConverter
import java.util.Date
import java.util.Locale

class HourlyForecastAdapter(
    private var dataList: MutableList<Hourly>,
    private var symbol: String,
    private var speedUnit: String
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

    fun updateList(newList: List<Hourly>, newSymbol: String, newSpeedSymbol: String) {
        dataList.clear()
        dataList.addAll(newList)
        symbol = newSymbol
        speedUnit = newSpeedSymbol
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentHour: Hourly = dataList[position]
        val temp = currentHour.temp ?: 0.0
        val icon = currentHour.weather[0].icon
        val link = "https://openweathermap.org/img/wn/$icon@2x.png"
        val localHour = getLocalHourFromUnixTimestamp(currentHour.dt)
        val formattedTemp = when (symbol) {
            "°C" -> "${UnitConverter.kelvinToCelsius(temp)}°C"
            "°F" -> "${UnitConverter.kelvinToFahrenheit(temp)}°F"
            else -> "${temp}K"
        }

        val windSpeed = currentHour.windSpeed ?: 0.0
        val formattedWindSpeed = if (speedUnit == "km/h") {
            UnitConverter.meterPerSecondToKilometersPerHour(windSpeed)
        } else {
            UnitConverter.meterPerSecondToMilesPerHour(windSpeed)
        }

        val formattedTime = formatHour(localHour)
        Log.i("Hour", "onBindViewHolder: $localHour")

        holder.degreeTV.text = formattedTemp
        holder.timeTV.text = formattedTime
        Glide.with(context)
            .load(link)
            .apply(RequestOptions().override(100, 100))
            .into(holder.iconView)
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val degreeTV: TextView = itemView.findViewById(R.id.tvTemp)
        val timeTV: TextView = itemView.findViewById(R.id.tvTime)
        val iconView: ImageView = itemView.findViewById(R.id.ivWeatherIcon)
    }

    private fun getLocalHourFromUnixTimestamp(gmtUnixTimestamp: Int): Int {
        val date = Date(gmtUnixTimestamp * 1000L)
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
