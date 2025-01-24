package com.example.yksiloprojekti2_weatherapp.data.network

import retrofit2.http.GET
import retrofit2.http.Query

// Class to save the weather data returned from the API
data class GeocodingResponse(
    val name: String,
    val lat: String,
    val lon: String
)

// Interface for the API service to fetch weather data
interface GeoServiceApi {
    @GET("search")
    suspend fun getCoordinates(
        @Query("city") city: String,
        @Query("format") format: String = "json"
    ): List<GeocodingResponse>
}
