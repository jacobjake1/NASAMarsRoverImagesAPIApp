package com.example.nasaadventureapp

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MarsRoverApiService {
    @GET("mars-photos/api/v1/rovers/curiosity/photos")
    fun getPhotos(
        @Query("earth_date") earthDate: String,
        @Query("api_key") apiKey: String
    ): Call<MarsRoverResponse>
}