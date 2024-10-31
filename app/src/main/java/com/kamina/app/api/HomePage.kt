package com.kamina.app.api

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Adjusted data class for HomeResponse, which combines category and entity information

data class HomeCategoryResponse(
    val categoryName: String,
    val thumbnails: List<String>,
    val entities: List<HomeEntity>  // List of entities with translations
)

data class HomeEntity(
    val id: Int,
    val thumbnail: String?,  // URL of the entity's thumbnail
    val translations: TranslationHomeData?  // Entity's translations
)

data class TranslationHomeData(
    val name: String?,
    val description: String?,
    val logo: String?,
    val language: String? // Include language field to filter by
)

interface HomeApiService {
    @GET("thumbnails-by-category")
    fun getHomeCategories(
        @Query("language") language: String
    ): Call<Map<String, HomeCategoryResponse>>
}

fun fetchHomeCategories(language: String, onResult: (List<HomeCategoryResponse>?) -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.kaminajp.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(HomeApiService::class.java)

    api.getHomeCategories(language = language).enqueue(object : Callback<Map<String, HomeCategoryResponse>> {
        override fun onResponse(
            call: Call<Map<String, HomeCategoryResponse>>,
            response: Response<Map<String, HomeCategoryResponse>>
        ) {
            if (response.isSuccessful) {
                val result = response.body()?.values?.toList()
                onResult(result)
            } else {
                onResult(null)
            }
        }

        override fun onFailure(call: Call<Map<String, HomeCategoryResponse>>, t: Throwable) {
            onResult(null)
        }
    })
}
