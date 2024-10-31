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

// Data class for individual Movies entities with translations
data class MoviesEntity(
    val id: Int,
    val thumbnail: String?,  // URL of the movies thumbnail
    val translations: TranslationMoviesData?  // Changed to TranslationMoviesData
)

// Data class for translation data
data class TranslationMoviesData(
    val name: String?,
    val description: String?,
    val logo: String?,
    val language: String? // Adding the language field
)

// API interface for fetching movies grouped by category
interface MoviesApiService {
    @GET("thumbnails-by-category-isMovie")
    fun getMoviesCategories(
        @Query("isMovie") isMovie: Int = 1,  // Ensuring it fetches only Movies (where isMovie=1)
        @Query("language") language: String  // Added language query parameter
    ): Call<Map<String, MoviesCategoryResponse>>
}

// Function to fetch Movies grouped by category, including language support
fun fetchMoviesByCategory(userLanguage: String, onResult: (List<MoviesCategoryResponse>?) -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.kaminajp.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(MoviesApiService::class.java)

    api.getMoviesCategories(language = userLanguage).enqueue(object : Callback<Map<String, MoviesCategoryResponse>> {
        override fun onResponse(
            call: Call<Map<String, MoviesCategoryResponse>>,
            response: Response<Map<String, MoviesCategoryResponse>>
        ) {
            if (response.isSuccessful) {
                val result = response.body()?.values?.toList()  // Extract the values of the map

                // Log the result for debugging
                result?.forEach { category ->
                    Log.d("MoviesAPI", "Category: ${category.categoryName}")
                    category.entities.forEach { entity ->
                        val translation = entity.translations
                        if (translation?.language == userLanguage) {
                            Log.d("MoviesAPI", "Movie found in $userLanguage: ${translation.name}")
                        } else {
                            Log.d("MoviesAPI", "No translation available for entityId: ${entity.id} in language: $userLanguage")
                        }
                    }
                }

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
