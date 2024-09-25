package com.kamina.app.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

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
    @GET("videos/{entityId}/seasons/{season}/episodes/{episode}")
    suspend fun getEpisode(
        @Path("entityId") entityId: Int,
        @Path("season") season: Int,
        @Path("episode") episode: Int
    ): WatchEpisode
}

// Retrofit initialization for WatchPage API
private val watchPageRetrofit = Retrofit.Builder()
    .baseUrl("https://api.kaminajp.com/api/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

private val watchPageApi = watchPageRetrofit.create(WatchPageApiService::class.java)

// Function to fetch the episode data
suspend fun fetchWatchEpisode(entityId: Int, season: Int, episode: Int): WatchEpisode? {
    return try {
        watchPageApi.getEpisode(entityId, season, episode)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
