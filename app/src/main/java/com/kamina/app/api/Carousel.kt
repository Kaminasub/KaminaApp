package com.kamina.app.api

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Data class for the Carousel API response
data class CarouselEntityResponse(
    val id: Int,
    val name: String,
    val pic: String?,  // URL for entity picture
    val logo: String?,  // URL for entity logo
    val language: String?  // Language of the entity
)

// API service interface for the carousel
interface CarouselApiService {
    @GET("carousel")
    fun getCarouselEntities(
        @Query("language") language: String  // Pass the language parameter in the query
    ): Call<List<CarouselEntityResponse>>
}

// Function to fetch carousel entities from the API
// Function to fetch carousel entities
fun fetchCarouselEntities(language: String, onResult: (List<CarouselEntityResponse>?) -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.kaminajp.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(CarouselApiService::class.java)

    api.getCarouselEntities(language = language).enqueue(object : Callback<List<CarouselEntityResponse>> {
        override fun onResponse(call: Call<List<CarouselEntityResponse>>, response: Response<List<CarouselEntityResponse>>) {
            if (response.isSuccessful) {
                Log.d("CarouselAPI", "Fetched ${response.body()?.size} carousel entities")
                onResult(response.body())
            } else {
                Log.e("CarouselAPI", "Failed to fetch carousel entities. Response code: ${response.code()}")
                onResult(null)
            }
        }

        override fun onFailure(call: Call<List<CarouselEntityResponse>>, t: Throwable) {
            Log.e("CarouselAPI", "Failed to fetch carousel entities: ${t.message}")
            onResult(null)
        }
    })
}
