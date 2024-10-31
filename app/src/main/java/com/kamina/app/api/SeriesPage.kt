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
data class SeriesCategoryResponse(
    val categoryName: String,
    val thumbnails: List<String>,  // URLs of thumbnails for the series
    val entities: List<SeriesEntity>  // List of entities within each category
)

// Data class for individual series entities with translations
data class SeriesEntity(
    val id: Int,
    val thumbnail: String?,  // URL of the series thumbnail
    val translations: TranslationSeriesData?  // Changed to TranslationSeriesData
)

// Data class for translation data (renamed to TranslationSeriesData)
data class TranslationSeriesData(
    val name: String?,
    val description: String?,
    val logo: String?,
    val language: String? // Adding the language field
)

// API interface for fetching series grouped by category
interface SeriesApiService {
    @GET("thumbnails-by-category-isMovie")
    fun getSeriesCategories(
        @Query("isMovie") isMovie: Int = 0,  // Ensuring it fetches only series (where isMovie=0)
        @Query("language") language: String  // Added language query parameter
    ): Call<Map<String, SeriesCategoryResponse>>
}

// Function to fetch series grouped by category, including language support
fun fetchSeriesByCategory(userLanguage: String, onResult: (List<SeriesCategoryResponse>?) -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.kaminajp.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(SeriesApiService::class.java)

    api.getSeriesCategories(language = userLanguage).enqueue(object : Callback<Map<String, SeriesCategoryResponse>> {
        override fun onResponse(
            call: Call<Map<String, SeriesCategoryResponse>>,
            response: Response<Map<String, SeriesCategoryResponse>>
        ) {
            if (response.isSuccessful) {
                val result = response.body()?.values?.toList()  // Extract the values of the map

                // Log the result for debugging
                result?.forEach { category ->
                    Log.d("SeriesAPI", "Category: ${category.categoryName}")
                    category.entities.forEach { entity ->
                        val translation = entity.translations
                        if (translation?.language == userLanguage) {
                            Log.d("SeriesAPI", "Series found in $userLanguage: ${translation.name}")
                        } else {
                            Log.d("SeriesAPI", "No translation available for entityId: ${entity.id} in language: $userLanguage")
                        }
                    }
                }

                onResult(result)
            } else {
                Log.e("SeriesAPI", "API Error: ${response.message()}")
                onResult(null)
            }
        }

        override fun onFailure(call: Call<Map<String, SeriesCategoryResponse>>, t: Throwable) {
            Log.e("SeriesAPI", "API Failure: ${t.message}")
            onResult(null)
        }
    })
}
