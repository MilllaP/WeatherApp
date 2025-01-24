package com.example.yksiloprojekti2_weatherapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yksiloprojekti2_weatherapp.data.model.CurrentWeather
import com.example.yksiloprojekti2_weatherapp.data.model.HourlyWeather
import com.example.yksiloprojekti2_weatherapp.data.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException

class WeatherViewModel : ViewModel() {

    // LiveData to hold the weather data and error messages
    private val _weatherData = MutableLiveData<CurrentWeather?>()
    val weatherData: LiveData<CurrentWeather?> = _weatherData

    private val _hourlyWeatherData = MutableLiveData<HourlyWeather?>()
    val hourlyWeatherData: LiveData<HourlyWeather?> = _hourlyWeatherData

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // LiveData to track loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Default coordinates for Helsinki when app opens
    private val helsinkiCoordinates = Pair(60.1699, 24.9384)
    private val defaultCity = "Helsinki"

    // Initialization block to fetch weather data for Helsinki at app launch
    init {
        fetchWeather(
            helsinkiCoordinates.first,
            helsinkiCoordinates.second,
            defaultCity
        )
    }

    // Function to fetch weather data from the weather API
    private fun fetchWeather(latitude: Double, longitude: Double, cityName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)

                // Makes an API call to get current weather data
                val response = RetrofitInstance.weatherApi.getCurrentWeather(
                    latitude = latitude,
                    longitude = longitude,
                    timezone = "auto"
                )

                // Posts current weather data to LiveDa
                _weatherData.postValue(
                    CurrentWeather(
                        cityName = cityName,
                        time = response.current.time,
                        temperature_2m = response.current.temperature_2m,
                        wind_speed_10m = response.current.wind_speed_10m
                    )
                )

                // Posts hourly weather data to LiveData
                _hourlyWeatherData.postValue(
                    HourlyWeather(
                        time = response.hourly.time,
                        temperature_2m = response.hourly.temperature_2m
                    )
                )

            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error fetching weather: ${e.message}")
                _weatherData.postValue(null)
                _hourlyWeatherData.postValue(null)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Function to search weather data for a specified city
    fun searchCityWeather(city: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                _errorMessage.postValue(null)

                // Fetches name and coordinates of the city from the geo API
                val response = RetrofitInstance.geoApi.getCoordinates(city, "json")
                if (response.isNotEmpty()) {
                    val cityName = response[0].name
                    val latitude = response[0].lat.toDouble()
                    val longitude = response[0].lon.toDouble()

                    // Fetches weather data for the city using the coordinates
                    val weatherResponse = RetrofitInstance.weatherApi.getCurrentWeather(
                        latitude = latitude,
                        longitude = longitude,
                        timezone = "auto"
                    )

                    // Post current weather data to LiveData
                    _weatherData.postValue(
                        CurrentWeather(
                            cityName = cityName,
                            time = weatherResponse.current.time,
                            temperature_2m = weatherResponse.current.temperature_2m,
                            wind_speed_10m = weatherResponse.current.wind_speed_10m
                        )
                    )

                    // Post hourly weather data to LiveData
                    _hourlyWeatherData.postValue(
                        HourlyWeather(
                            time = weatherResponse.hourly.time,
                            temperature_2m = weatherResponse.hourly.temperature_2m
                        )
                    )
                } else {
                    Log.e("WeatherViewModel", "City not found.")
                    _errorMessage.postValue("Hakemaasi kaupunkia ei löytynyt.")
                }
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error searching city: ${e.message}")
                _weatherData.postValue(null)
                _hourlyWeatherData.postValue(null)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Function to get today's and tomorrow's weather from hourly data
    fun getTodaysAndTomorrowsWeather(hourlyWeather: HourlyWeather): List<HourlyWeather> {
        val currentDate = LocalDate.now() // Today's date
        val tomorrowDate = currentDate.plusDays(1) // Tomorrow's date

        // Filter hourly weather data to include only today's and tomorrow's weather
        return hourlyWeather.time
            .zip(hourlyWeather.temperature_2m)
            .filter { (adjustedTime, _) ->
                try {
                    val date = LocalDate.parse(adjustedTime.substring(0, 10))
                    date.isEqual(currentDate) || date.isEqual(tomorrowDate)
                } catch (e: DateTimeParseException) {
                    Log.e("WeatherViewModel", "Error parsing date: ${e.message}")
                    false
                }
            } // Maps the filtered data into a new list of HourlyWeather
            .map { (time, temperature) ->
                HourlyWeather(
                    time = listOf(time),
                    temperature_2m = listOf(temperature)
                )
            }
    }
}
