package com.example.cloudvibe.repository

import com.example.cloudvibe.model.database.WeatherEntity
import com.example.cloudvibe.model.network.data.City
import com.example.cloudvibe.model.network.data.Clouds
import com.example.cloudvibe.model.network.data.Coord
import com.example.cloudvibe.model.network.data.ForecastItem
import com.example.cloudvibe.model.network.data.ForecastResponse
import com.example.cloudvibe.model.network.data.Main
import com.example.cloudvibe.model.network.data.Sys
import com.example.cloudvibe.model.network.data.Weather
import com.example.cloudvibe.model.network.data.WeatherResponse
import com.example.cloudvibe.model.network.data.Wind

object FakeWeatherData {
    fun getWeatherResponse(): WeatherResponse {
        return WeatherResponse(
            coord = Coord(10.0, 20.0),
            weather = listOf(Weather(800, "Clear", "clear sky", "01d")),
            base = "stations",
            main = Main(300.0f, 298.0, 295.0f, 305.0f, 1012, 85, 1009, 1005),
            wind = Wind(5.5, 250, 7.0),
            rain = null,
            clouds = Clouds(0),
            dt = 1633094675,
            sys = Sys(1, 1000, "EG", 1633054675, 1633094675, "n"),
            timezone = 7200,
            id = 12345,
            name = "Cairo",
            cod = 200,
            hourly = null
        )
    }

    fun getWeatherEntity(): WeatherEntity {
        return WeatherEntity(
            locationName = "Cairo",
            country = "EG",
            localtime = "1633094675",
            temperature = 300.0f,
            description = "clear sky",
            windSpeed = 5.5,
            humidity = 85,
            timestamp = 1633094675,
            pressure = 1012,
            sunrise = 1633054675,
            sunset = 1633094675
        )
    }

    fun getFakeForecastResponse(): ForecastResponse {
        return ForecastResponse(
            dt = 1234567890,
            city = City(
                id = 1,
                name = "Test City",
                coord = Coord(1.0, 1.0),
                country = "Test Country",
                population = 1000000,
                timezone = 3600,
                sunrise = 1234567890,
                sunset = 1234567890
            ),
            list = listOf(
                ForecastItem(
                    dt = 1234567890,
                    main = Main(temp_max = 30f, temp_min = 20f),
                    weather = listOf(Weather(id = 1, main = "Clear", description = "clear sky")),
                    clouds = Clouds(all = 0),
                    wind = Wind(speed = 5.0, deg = 200),
                    visibility = 10000,
                    pop = 0.0,
                    sys = Sys(type = 1, id = 1, country = "Test", sunrise = 1234567890, sunset = 1234567890),
                    dt_txt = "2024-09-27 12:00:00"
                )
            )
        )
    }
}
