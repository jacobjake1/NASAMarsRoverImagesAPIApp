package com.example.nasaadventureapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import android.util.Log
import androidx.compose.ui.layout.ContentScale


class MainActivity : ComponentActivity() {
    private lateinit var apiService: MarsRoverApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.nasa.gov/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(MarsRoverApiService::class.java)

        setContent {
            MarsRoverApp(apiService)
        }
    }
}

@Composable
fun MarsRoverApp(apiService: MarsRoverApiService) {
    var marsResponse by remember { mutableStateOf<MarsRoverResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(key1 = Unit) {
        isLoading = true
        fetchMarsPhotos(apiService) { response, errorMessage ->
            isLoading = false
            if (response != null) {
                marsResponse = response
            } else {
                error = errorMessage
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (error != null) {
            Text("Error: $error")
        } else if (marsResponse != null) {
            LazyColumn {
                items(marsResponse!!.photos) { photo ->
                    MarsPhotoItem(photo)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            isLoading = true
            error = null
            fetchMarsPhotos(apiService) { response, errorMessage ->
                isLoading = false
                if (response != null) {
                    marsResponse = response
                } else {
                    error = errorMessage
                }
            }
        }) {
            Text("Get New Photos")
        }
    }
}

@Composable
fun MarsPhotoItem(photo: MarsPhoto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        AsyncImage(
            model = photo.img_src,
            contentDescription = "Mars Photo",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        Text(text = "Date: ${photo.earth_date}")
        Text(text = "Camera: ${photo.camera.full_name}")
    }
}

fun fetchMarsPhotos(apiService: MarsRoverApiService, callback: (MarsRoverResponse?, String?) -> Unit) {
    val apiKey = "DEMO_KEY"
    val earthDate = "2023-10-15"

    apiService.getPhotos(earthDate, apiKey).enqueue(object : retrofit2.Callback<MarsRoverResponse> {
        override fun onResponse(call: retrofit2.Call<MarsRoverResponse>, response: retrofit2.Response<MarsRoverResponse>) {
            if (response.isSuccessful && response.body() != null) {
                Log.d("MarsApi", "Received ${response.body()?.photos?.size} photos")
                callback(response.body(), null)
            } else {
                Log.e("MarsApi", "Error: ${response.errorBody()?.string()}")
                callback(null, "Error fetching data: ${response.code()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<MarsRoverResponse>, t: Throwable) {
            Log.e("MarsApi", "Network error", t)
            callback(null, "Network error: ${t.message}")
        }
    })
}
