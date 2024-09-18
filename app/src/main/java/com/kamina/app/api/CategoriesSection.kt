package com.kamina.app.api

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// Adjust the root response model to match the object structure
data class CategoryResponseWrapper(
    val categoryId: Map<String, CategoryResponse> // Map with categoryId as keys
)

data class CategoryResponse(
    val categoryName: String,
    val thumbnails: List<String>,
    val entities: List<Entity>
)

data class Entity(
    val id: Int,
    val thumbnail: String
)

interface NewCategoriesApiService {
    @GET("thumbnails-by-category")
    fun getCategories(): Call<Map<String, CategoryResponse>>  // Expecting a map structure
}

fun fetchCategories(onResult: (List<CategoryResponse>?) -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.kaminajp.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(NewCategoriesApiService::class.java)

    api.getCategories().enqueue(object : Callback<Map<String, CategoryResponse>> {
        override fun onResponse(
            call: Call<Map<String, CategoryResponse>>,
            response: Response<Map<String, CategoryResponse>>
        ) {
            if (response.isSuccessful) {
                val result = response.body()?.values?.toList()  // Extract the values of the map
                Log.d("CategoriesAPI", "API Success: $result")
                onResult(result)
            } else {
                Log.e("CategoriesAPI", "API Error: ${response.message()}")
                onResult(null)
            }
        }

        override fun onFailure(call: Call<Map<String, CategoryResponse>>, t: Throwable) {
            Log.e("CategoriesAPI", "API Failure: ${t.message}")
            onResult(null)
        }
    })
}
