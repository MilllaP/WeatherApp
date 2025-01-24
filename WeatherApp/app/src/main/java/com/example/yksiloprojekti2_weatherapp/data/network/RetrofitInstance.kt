package com.example.yksiloprojekti2_weatherapp.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // Base URL for Open-Meteo API and Nominatim API
    private const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"
    private const val OPEN_METEO_BASE_URL  = "https://api.open-meteo.com/v1/"


    // HTTP logging interceptor for debugging
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // OkHttpClient with logging interceptor
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // Nominatim API for geocoding (city -> coordinates)
    val geoApi: GeoServiceApi by lazy {
        Retrofit.Builder()
            .baseUrl(NOMINATIM_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeoServiceApi::class.java)
    }

    // Open-Meteo API for weather data
    val weatherApi: WeatherServiceApi by lazy {
        Retrofit.Builder()
            .baseUrl(OPEN_METEO_BASE_URL )
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherServiceApi::class.java)
    }
}