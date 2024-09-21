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
import com.example.cloudvibe.utils.UnitConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DailyAdapter(
    private var dailyList: MutableList<ForecastData>,
    private var symbol: String,
    private val context: Context
) : RecyclerView.Adapter<DailyAdapter.MyViewHolder>() {

    companion object {
        const val CELSIUS = "C"
        const val FAHRENHEIT = "F"
    }

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
        maxd = when (symbol) {
            CELSIUS -> UnitConverter.kelvinToCelsius(maxd)
            FAHRENHEIT -> UnitConverter.kelvinToFahrenheit(maxd)
            else -> maxd
        }
        mind = when (symbol) {
            CELSIUS -> UnitConverter.kelvinToCelsius(mind)
            FAHRENHEIT -> UnitConverter.kelvinToFahrenheit(mind)
            else -> mind
        }

        val maxDegree = maxd.toInt()
        val minDegree = mind.toInt()
        val icon = currentDaily.weather[0].icon
        val link = "https://openweathermap.org/img/wn/$icon@2x.png"
        val dayName = getFormattedDay(currentDaily.dt)
        val state = currentDaily.weather[0].description

        holder.dayTV.text = dayName
        holder.stateTV.text = state
        holder.maxDegreeTV.text = "$maxDegree$symbol"
        holder.minDegreeTV.text = "$minDegree$symbol"

        Glide.with(context)
            .load(link)
            .apply(RequestOptions().override(100, 100).placeholder(R.drawable.ic_launcher_foreground))
            .into(holder.iconView)
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
