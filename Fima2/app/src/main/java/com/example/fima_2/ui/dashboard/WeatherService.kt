package com.example.fima_2.ui.dashboard

import WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("weather")
    fun getWeather(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") apiKey: String
    ): Call<WeatherResponse>
}