package com.example.yksiloprojekti2_weatherapp.data.model

// Class for current weather data retrieved from the API
data class CurrentWeather(
    val cityName: String,
    val time: String,
    val temperature_2m: Double,
    val wind_speed_10m: Double
)

