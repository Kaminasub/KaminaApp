package com.kamina.app.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

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

data class UserProgress(
    val currentSeason: Int,
    val currentEpisode: Int,
    val watched: Boolean,
    val entityId: Int
)

// Data classes for episodes and user progress from WatchPage
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

data class WatchPageUserProgress(
    val currentSeason: Int,
    val currentEpisode: Int,
    val watched: Boolean
)

// API interface to handle all API calls related to DetailPage and WatchPage
interface DetailPageApiService {
    @GET("entities/{id}")
    suspend fun getEntityDetail(@Path("id") id: Int): EntityDetail

    @GET("entities/{id}/cast")
    suspend fun getCast(@Path("id") id: Int): List<CastMember>

    @GET("entities/{id}/suggestions")
    suspend fun getSuggestions(@Path("id") id: Int): List<Suggestion>

    @GET("episodes/{id}/seasons")
    suspend fun getSeasons(@Path("id") id: Int): List<Season>

    @GET("episodes/{id}/seasons/{season}")
    suspend fun getEpisodes(@Path("id") id: Int, @Path("season") season: Int): List<Episode>

    // Fetch user progress for DetailPage
    @GET("api/user_progress/{userId}/{entityId}")
    suspend fun getUserProgress(
        @Path("userId") userId: Int,
        @Path("entityId") entityId: Int
    ): UserProgress?

    // Fetch movie details, including filePath
    @GET("videos/{entityId}")
    suspend fun getMovieDetails(@Path("entityId") entityId: Int): WatchPageEpisode

    // Fetch specific episode for WatchPage
    @GET("api/videos/{entityId}/seasons/{season}/episodes/{episode}")
    suspend fun getEpisode(
        @Path("entityId") entityId: Int,
        @Path("season") season: Int,
        @Path("episode") episode: Int
    ): WatchPageEpisode

    // Fetch user progress for WatchPage
    @GET("api/user_progress/{userId}/{entityId}/{videoId}")
    suspend fun getWatchPageUserProgress(
        @Path("userId") userId: Int,
        @Path("entityId") entityId: Int,
        @Path("videoId") videoId: Int
    ): WatchPageUserProgress?

    // Fetch next episode for WatchPage
    @GET("api/videos/{entityId}/seasons/{season}/episodes/{episode}/next")
    suspend fun getNextEpisode(
        @Path("entityId") entityId: Int,
        @Path("season") season: Int,
        @Path("episode") episode: Int
    ): WatchPageEpisode?
}

// Retrofit initialization
private val detailPageRetrofit = Retrofit.Builder()
    .baseUrl("https://api.kaminajp.com/api/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

private val detailPageApi = detailPageRetrofit.create(DetailPageApiService::class.java)

// API functions for DetailPage
suspend fun fetchEntityDetails(id: Int): EntityDetail? {
    return try {
        detailPageApi.getEntityDetail(id)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun fetchCast(id: Int): List<CastMember>? {
    return try {
        detailPageApi.getCast(id)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun fetchSuggestions(id: Int): List<Suggestion>? {
    return try {
        detailPageApi.getSuggestions(id)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun fetchSeasons(id: Int): List<Season>? {
    return try {
        detailPageApi.getSeasons(id)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun fetchEpisodes(id: Int, season: Int): List<Episode>? {
    return try {
        detailPageApi.getEpisodes(id, season)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun fetchUserProgress(userId: Int, entityId: Int): UserProgress? {
    return try {
        detailPageApi.getUserProgress(userId, entityId)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// API functions for WatchPage
suspend fun fetchWatchPageEpisode(entityId: Int, season: Int, episode: Int): WatchPageEpisode? {
    return try {
        detailPageApi.getEpisode(entityId, season, episode)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun fetchWatchPageUserProgress(userId: Int, entityId: Int, videoId: Int): WatchPageUserProgress? {
    return try {
        detailPageApi.getWatchPageUserProgress(userId, entityId, videoId)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun fetchNextEpisode(entityId: Int, season: Int, episode: Int): WatchPageEpisode? {
    return try {
        detailPageApi.getNextEpisode(entityId, season, episode)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// API function to fetch movie details
suspend fun getMovieDetails(entityId: Int): WatchPageEpisode? {
    return try {
        detailPageApi.getMovieDetails(entityId)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
