package com.kamina.app.api

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Data class for the API response
data class MoviesCategoryResponse(
    val categoryName: String,
    val thumbnails: List<String>,  // URLs of thumbnails for the Movies
    val entities: List<MoviesEntity>  // List of entities within each category
)

// Data class for individual Movies entities
data class MoviesEntity(
    val id: Int,
    val thumbnail: String  // URL of the movies thumbnail
)

// API interface for fetching movies grouped by category
interface MoviesApiService {
    @GET("thumbnails-by-category-isMovie")
    fun getMoviesCategories(
        @Query("isMovie") isMovie: Int = 1 // Ensuring it fetches only Movies (where isMovie=1)
    ): Call<Map<String, MoviesCategoryResponse>>
}

// Function to fetch Movies grouped by category
fun fetchMoviesByCategory(onResult: (List<MoviesCategoryResponse>?) -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.kaminajp.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(MoviesApiService::class.java)

    api.getMoviesCategories().enqueue(object : Callback<Map<String, MoviesCategoryResponse>> {
        override fun onResponse(
            call: Call<Map<String, MoviesCategoryResponse>>,
            response: Response<Map<String, MoviesCategoryResponse>>
        ) {
            if (response.isSuccessful) {
                val result = response.body()?.values?.toList()  // Extract the values of the map
                Log.d("MoviesAPI", "API Success: $result")
                onResult(result)
            } else {
                Log.e("MoviesAPI", "API Error: ${response.message()}")
                onResult(null)
            }
        }

        override fun onFailure(call: Call<Map<String, MoviesCategoryResponse>>, t: Throwable) {
            Log.e("MoviesAPI", "API Failure: ${t.message}")
            onResult(null)
        }
    })
}
