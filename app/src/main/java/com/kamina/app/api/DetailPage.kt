package com.kamina.app.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

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
}

// Retrofit initialization
private val detailPageRetrofit = Retrofit.Builder()
    .baseUrl("https://api.kaminajp.com/api/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

private val detailPageApi = detailPageRetrofit.create(DetailPageApiService::class.java)

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

// API functions
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
