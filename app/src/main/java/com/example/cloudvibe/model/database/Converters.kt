package com.example.cloudvibe.model.database


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import com.example.cloudvibe.model.network.data.Clouds
import com.example.cloudvibe.model.network.data.Hourly
import com.example.cloudvibe.model.network.data.Main
import com.example.cloudvibe.model.network.data.Sys
import com.example.cloudvibe.model.network.data.Weather
import com.example.cloudvibe.model.network.data.Wind
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.format.DateTimeFormatter

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromWind(wind: Wind?): String? {
        return wind?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toWind(windString: String?): Wind? {
        return windString?.let { gson.fromJson(it, Wind::class.java) }
    }
    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        val formatter: DateTimeFormatter =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }

    @TypeConverter
    fun fromMain(main: Main): String {
        return gson.toJson(main)
    }

    @TypeConverter
    fun toMain(mainString: String): Main {
        val type = object : TypeToken<Main>() {}.type
        return gson.fromJson(mainString, type)
    }

    @TypeConverter
    fun fromWeather(weather: List<Weather>): String {
        return gson.toJson(weather)
    }

    @TypeConverter
    fun toWeather(weatherString: String): List<Weather> {
        val type = object : TypeToken<List<Weather>>() {}.type
        return gson.fromJson(weatherString, type)
    }
    @TypeConverter
    fun fromClouds(clouds: Clouds?): String? {
        return clouds?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toClouds(cloudsString: String?): Clouds? {
        return cloudsString?.let { gson.fromJson(it, Clouds::class.java) }
    }

    @TypeConverter
    fun fromHourlyList(hourlyList: List<Hourly>?): String? {
        return gson.toJson(hourlyList)
    }

    @TypeConverter
    fun toHourlyList(hourlyString: String?): List<Hourly>? {
        if (hourlyString.isNullOrEmpty()) {
            return null
        }
        val type = object : TypeToken<List<Hourly>>() {}.type
        return gson.fromJson(hourlyString, type)
    }

    @TypeConverter
    fun fromSys(sys: Sys): String {
        return Gson().toJson(sys)
    }

    @TypeConverter
    fun toSys(sysString: String): Sys {
        return Gson().fromJson(sysString, Sys::class.java)
    }
}