package com.example.yksiloprojekti2_weatherapp.data.model

data class HourlyWeather(
    val time: List<String>,
    val temperature_2m: List<Double>,
)