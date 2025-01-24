package com.example.yksiloprojekti2_weatherapp.data.network

import com.example.yksiloprojekti2_weatherapp.data.model.CurrentWeather
import com.example.yksiloprojekti2_weatherapp.data.model.HourlyWeather
import retrofit2.http.GET
import retrofit2.http.Query

// Class to save the weather data returned from the API
data class WeatherResponse (
    val current: CurrentWeather,
    val hourly: HourlyWeather
)

// Interface for the API service to fetch weather data
interface WeatherServiceApi {
    @GET("forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,wind_speed_10m",
        @Query("hourly") hourly: String = "temperature_2m",
        @Query("timezone") timezone: String
    ): WeatherResponse
}
