package com.example.cloudvibe.home.view

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.cloudvibe.R
import com.example.cloudvibe.model.database.ForecastData
import com.example.cloudvibe.sharedpreferences.SharedPreferencesHelper
import com.example.cloudvibe.utils.UnitConverter
import com.example.cloudvibe.utils.UnitConverter.getWeatherIconResource
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DailyAdapter(
    private var dailyList: MutableList<ForecastData>,
    private var symbol: String, // Temperature unit symbol
    private val context: Context

) : RecyclerView.Adapter<DailyAdapter.MyViewHolder>() {

 /*   companion object {
        const val CELSIUS = "C"
        const val FAHRENHEIT = "F"
        const val KELVIN = "K"
    }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.day_list_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = dailyList.size

    fun updateList(newDailyList: List<ForecastData>, newSymbol: String) {
        dailyList.clear()
        dailyList.addAll(newDailyList)
        symbol = newSymbol
        notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentDaily = dailyList[position]

        var maxd = currentDaily.maxTemp
        var mind = currentDaily.minTemp

        maxd = convertTemperature(maxd.toFloat(), symbol)
        mind = convertTemperature(mind.toFloat(), symbol)

        val maxDegree = maxd.toInt()
        val minDegree = mind.toInt()
        val icon = currentDaily.weather[0].icon
        val iconResource = getWeatherIconResource(icon)

        holder.iconView.setImageResource(iconResource)
        val dayName = getFormattedDay(currentDaily.dt)
        val state = currentDaily.weather[0].description

        holder.dayTV.text = dayName
        holder.stateTV.text = state
        holder.maxDegreeTV.text = "${UnitConverter.parseIntegerIntoArabic(maxDegree.toString())}"
        holder.minDegreeTV.text = "${UnitConverter.parseIntegerIntoArabic(minDegree.toString())} ${symbol}"
    }


/*    fun convertTemperature(temp: Double, scale: String = "K"): String {
        return when (scale) {
            "C" -> "${(temp - 273.15).toInt()}°C"
            "F" -> "${((temp - 273.15) * 9/5 + 32).toInt()}°F"
            else -> "${(temp.toInt()).toString()} K"
        }
    }*/
    private fun convertTemperature(tempInCelsius: Float, unit: String): Float {
        return when (unit) {
            "°K" -> tempInCelsius + 273.15f
            "°F" -> (tempInCelsius * 9 / 5) + 32
            else -> tempInCelsius // Celsius by default
        }
    }
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val maxDegreeTV: TextView = itemView.findViewById(R.id.max_degree)
        val minDegreeTV: TextView = itemView.findViewById(R.id.min_degree)
        val dayTV: TextView = itemView.findViewById(R.id.day_text)
        val stateTV: TextView = itemView.findViewById(R.id.day_state)
        val iconView: ImageView = itemView.findViewById(R.id.day_icon)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getFormattedDay(unixTimestamp: Long): String {
        val targetDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTimestamp), ZoneId.systemDefault())
        val now = LocalDateTime.now()
        return when {
            targetDate.toLocalDate().isEqual(now.toLocalDate()) -> context.getString(R.string.today)
            targetDate.toLocalDate().isEqual(now.plusDays(1).toLocalDate()) -> context.getString(R.string.tomorrow)
            else -> targetDate.format(DateTimeFormatter.ofPattern("EEEE"))
        }
    }
}

