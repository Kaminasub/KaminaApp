package com.kamina.app.api

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Data class for search results and suggestions
data class SearchResult(
    val id: Int,
    val name: String,
    val thumbnail: String?,
    val type: String
)

data class AutocompleteSuggestion(
    val suggestion: String
)

// API service for search
interface SearchApiService {

    // Fetch autocomplete suggestions, filtered by language
    @GET("search/autocomplete")
    fun getAutocompleteSuggestions(
        @Query("query") query: String,
        @Query("language") language: String // Pass the language parameter
    ): Call<List<AutocompleteSuggestion>>

    // Fetch search results, filtered by language
    @GET("search/results")
    fun getSearchResults(
        @Query("query") query: String,
        @Query("language") language: String // Pass the language parameter
    ): Call<List<SearchResult>>
}

// Retrofit instance for Search API
private val SearchPageretrofit = Retrofit.Builder()
    .baseUrl("https://api.kaminajp.com/api/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

private val SearchApi = SearchPageretrofit.create(SearchApiService::class.java)

// Function to fetch autocomplete suggestions
fun fetchAutocompleteSuggestions(query: String, language: String, onResult: (List<AutocompleteSuggestion>?) -> Unit) {
    SearchApi.getAutocompleteSuggestions(query, language).enqueue(object : Callback<List<AutocompleteSuggestion>> {
        override fun onResponse(call: Call<List<AutocompleteSuggestion>>, response: Response<List<AutocompleteSuggestion>>) {
            if (response.isSuccessful) {
                onResult(response.body())
            } else {
                Log.e("SearchAPI", "API Error: ${response.code()} - ${response.message()}")
                Log.e("SearchAPI", "Response Body: ${response.errorBody()?.string()}")
                onResult(null)
            }
        }

        override fun onFailure(call: Call<List<AutocompleteSuggestion>>, t: Throwable) {
            Log.e("SearchAPI", "API Failure: ${t.message}")
            onResult(null)
        }
    })
}

fun fetchSearchResults(query: String, language: String, onResult: (List<SearchResult>?) -> Unit) {
    SearchApi.getSearchResults(query, language).enqueue(object : Callback<List<SearchResult>> {
        override fun onResponse(call: Call<List<SearchResult>>, response: Response<List<SearchResult>>) {
            if (response.isSuccessful) {
                try {
                    val responseBody = response.body()
                    Log.d("SearchAPI", "Parsed API Response: $responseBody")
                    onResult(responseBody)
                } catch (e: Exception) {
                    Log.e("SearchAPI", "Parsing Error: ${e.message}")
                    onResult(null)
                }
            } else {
                Log.e("SearchAPI", "API Error: ${response.code()} - ${response.message()}")
                Log.e("SearchAPI", "Response Body: ${response.errorBody()?.string()}")
                onResult(null)
            }
        }

        override fun onFailure(call: Call<List<SearchResult>>, t: Throwable) {
            Log.e("SearchAPI", "API Failure: ${t.message}")
            onResult(null)
        }
    })
}
