package com.example.cloudvibe.utils

import com.example.cloudvibe.model.database.ForecastData
import com.example.cloudvibe.model.database.WeatherEntity
import com.example.cloudvibe.model.network.data.ForecastResponse
import com.example.cloudvibe.model.network.data.WeatherResponse

object WeatherMapper {

    fun mapWeatherResponseToEntity(response: WeatherResponse): WeatherEntity {
        return WeatherEntity(
            locationName = response.name,
            country = response.sys.country,
            localtime = response.dt.toString(),
            temperature = response.main.temp,
            description = response.weather.firstOrNull()?.description ?: "",
            windSpeed = response.wind.speed,
            humidity = response.main.humidity,
            timestamp = response.dt,
            pressure = response.main.pressure,
            sunrise = response.sys.sunrise,
            sunset = response.sys.sunset
        )
    }

    fun mapForecastResponseToData(forecastResponse: ForecastResponse): List<ForecastData> {
        return forecastResponse.list.map { forecastItem ->
            ForecastData(
                dt = forecastItem.dt,
                maxTemp = forecastItem.main.temp_max,
                minTemp = forecastItem.main.temp_min,
                date = forecastItem.dt_txt.split(" ")[0],
                time = forecastItem.dt_txt.split(" ")[1],
                weather = forecastItem.weather
            )
        }
    }
}
