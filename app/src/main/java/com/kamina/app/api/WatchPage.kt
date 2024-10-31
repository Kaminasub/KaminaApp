package com.kamina.app.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Data class for episode details in WatchPage, including duration
data class WatchEpisode(
    val id: Int,
    val title: String,
    val description: String,
    val filePath: String,
    val miniatura: String,
    val isMovie: Int,
    val season: Int,
    val episode: Int,
    val duration: String // Added duration field
)

// API interface for WatchPage to fetch episode details
interface WatchPageApiService {

    // API to fetch a specific episode by entityId, season, episode, and language
    @GET("videos/{entityId}/seasons/{season}/episodes/{episode}")
    suspend fun getEpisode(
        @Path("entityId") entityId: Int,
        @Path("season") season: Int,
        @Path("episode") episode: Int,
        @Query("language") language: String // Pass language explicitly
    ): WatchEpisode
}

// Retrofit initialization for WatchPage API
private val watchPageRetrofit = Retrofit.Builder()
    .baseUrl("https://api.kaminajp.com/api/")  // Base URL for API
    .addConverterFactory(GsonConverterFactory.create()) // JSON converter
    .build()

// Create the API service instance
private val watchPageApi = watchPageRetrofit.create(WatchPageApiService::class.java)

// Function to fetch the episode data
suspend fun fetchWatchEpisode(entityId: Int, season: Int, episode: Int, language: String): WatchEpisode? {
    return try {
        // Call API with the provided entityId, season, episode, and language
        watchPageApi.getEpisode(entityId, season, episode, language)
    } catch (e: Exception) {
        // Print stack trace for debugging in case of error
        e.printStackTrace()
        null
    }
}
