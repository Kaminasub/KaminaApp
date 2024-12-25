package com.kamina.app.api

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Data Models
data class CastMember(
    val id: Int,
    val name: String,
    val character_name: String,
    val profile_path: String? // Nullable because some actors may not have a profile picture
)

data class Suggestion(val id: Int, val thumbnail: String)
data class Season(val season: Int)
data class Episode(val season: Int, val episode: Int, val title: String, val description: String, val miniatura: String)

data class EntityDetail(
    val id: Int,
    val name: String,
    val resumen: String,
    val thumbnail: String,
    val logo: String?,
    val pic: String?,
    val wall: String?,
    val preview: String?,
    val categoryId: Int,
    val isMovie: Int
)

data class WatchPageEpisode(
    val id: Int,
    val entityId: Int,
    val season: Int,
    val episode: Int,
    val title: String,
    val description: String,
    val filePath: String,
    val isMovie: Int
)

// API interface to handle all API calls related to DetailPage and WatchPage
interface DetailPageApiService {

    // Fetch entity details with language filter
    @GET("entities/{id}")
    suspend fun getEntityDetail(
        @Path("id") id: Int,
        @Query("language") language: String // Language filter
    ): EntityDetail

    // Fetch cast (same for all languages)
    @GET("entities/{id}/cast")
    suspend fun getCast(@Path("id") id: Int): List<CastMember>

    // Fetch suggestions with language filter
    @GET("entities/{id}/suggestions")
    suspend fun getSuggestions(
        @Path("id") id: Int,
        @Query("language") language: String // Language filter
    ): List<Suggestion>

    // Fetch seasons
    @GET("episodes/{id}/seasons")
    suspend fun getSeasons(
        @Path("id") id: Int,
        @Query("language") language: String // Language filter

    ): List<Season>


    // Fetch episodes for a given season with language
    @GET("episodes/{id}/seasons/{season}")
    suspend fun getEpisodes(
        @Path("id") id: Int,
        @Path("season") season: Int,
        @Query("language") language: String // Language filter
    ): List<Episode>

    // Fetch movie details, including filePath with language filter
    @GET("videos/{entityId}")
    suspend fun getMovieDetails(
        @Path("entityId") entityId: Int,
        @Query("language") language: String // Language filter
    ): WatchPageEpisode

    // Fetch specific episode with language filter
    @GET("videos/{entityId}/seasons/{season}/episodes/{episode}")
    suspend fun getEpisode(
        @Path("entityId") entityId: Int,
        @Path("season") season: Int,
        @Path("episode") episode: Int,
        @Query("language") language: String // Language filter
    ): WatchPageEpisode

    // Fetch next episode with language filter
    @GET("episodes/{entityId}/seasons/{season}/episodes/{episode}/next")
    suspend fun getNextEpisode(
        @Path("entityId") entityId: Int,
        @Path("season") season: Int,
        @Path("episode") episode: Int,
        @Query("language") language: String // Language filter
    ): WatchPageEpisode?
}

// Retrofit initialization
private val detailPageRetrofit = Retrofit.Builder()
    .baseUrl("https://api.kaminajp.com/api/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

private val detailPageApi = detailPageRetrofit.create(DetailPageApiService::class.java)

// API functions for DetailPage
suspend fun fetchEntityDetails(id: Int, language: String): EntityDetail? {
    return try {
        //Log.d("DetailPageApi", "Fetching entity details for id: $id, language: $language")
        val result = detailPageApi.getEntityDetail(id, language)
        //Log.d("DetailPageApi", "Entity details fetched: $result")
        result
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun fetchCast(id: Int): List<CastMember>? {
    return try {
        //Log.d("DetailPageApi", "Fetching cast for id: $id")
        val result = detailPageApi.getCast(id)
        //Log.d("DetailPageApi", "Cast fetched: $result")
        result
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun fetchSuggestions(id: Int, language: String): List<Suggestion>? {
    return try {
        //Log.d("DetailPageApi", "Fetching suggestions for id: $id, language: $language")
        val result = detailPageApi.getSuggestions(id, language)
        //Log.d("DetailPageApi", "Suggestions fetched: $result")
        result
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}



// Fetching seasons with language parameter
suspend fun fetchSeasons(entityId: Int, language: String): List<Season>? {
    return try {
        //Log.d("DetailPageApi", "Fetching seasons for id: $entityId, language: $language")
        val result = detailPageApi.getSeasons(entityId, language)
        //Log.d("DetailPageApi", "Seasons fetched: $result")
        result
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


suspend fun fetchEpisodes(id: Int, season: Int, language: String): List<Episode>? {
    return try {
        Log.d("DetailPageApi", "Fetching episodes for id: $id, season: $season, language: $language")
        val result = detailPageApi.getEpisodes(id, season, language)
        Log.d("DetailPageApi", "Episodes fetched: $result")
        result
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// API functions for WatchPage
suspend fun fetchWatchPageEpisode(entityId: Int, season: Int, episode: Int, language: String): WatchPageEpisode? {
    return try {
        Log.d("DetailPageApi", "Fetching watch page episode for entityId: $entityId, season: $season, episode: $episode, language: $language")
        val result = detailPageApi.getEpisode(entityId, season, episode, language)
        Log.d("DetailPageApi", "WatchPage episode fetched: $result")
        result
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun fetchNextEpisode(entityId: Int, season: Int, episode: Int, language: String): WatchPageEpisode? {
    return try {
        Log.d("DetailPageApi", "Fetching next episode for entityId: $entityId, season: $season, episode: $episode, language: $language")
        val result = detailPageApi.getNextEpisode(entityId, season, episode, language)
        Log.d("DetailPageApi", "Next episode fetched: $result")
        result
    } catch (e: Exception) {
        Log.e("DetailPageApi", "Error fetching next episode: ${e.message}", e)
        null
    }
}


// API function to fetch movie details
suspend fun getMovieDetails(entityId: Int, language: String): WatchPageEpisode? {
    return try {
        Log.d("DetailPageApi", "Fetching movie details for entityId: $entityId, language: $language")
        val result = detailPageApi.getMovieDetails(entityId, language)
        Log.d("DetailPageApi", "Movie details fetched: $result")
        result
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
