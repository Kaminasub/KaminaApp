package com.kamina.app.api

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Data class to represent a TV channel.
 */
data class Channel(
    val id: Int,
    val name: String,
    @SerializedName("tvg_id") val tvgId: String?,
    @SerializedName("tvg_name") val tvgName: String?,
    @SerializedName("tvg_language") val tvgLanguage: String?,
    @SerializedName("tvg_country") val tvgCountry: String?,
    @SerializedName("tvg_logo") val tvgLogo: String?,
    @SerializedName("tvg_url") val tvgUrl: String?,
    @SerializedName("group_title") val groupTitle: String?,
    @SerializedName("http_referrer") val httpReferrer: String?,
    @SerializedName("http_user_agent") val httpUserAgent: String?,
    @SerializedName("stream_url") val streamUrl: String,
    val raw: String?,
    @SerializedName("catchup_type") val catchupType: String?,
    @SerializedName("catchup_days") val catchupDays: Int?,
    @SerializedName("catchup_source") val catchupSource: String?,
    val timeshift: String?
)

/**
 * API interface for TvPage functionalities.
 */
interface TvPageApi {
    /**
     * Fetch the list of TV channels.
     *
     * @param query Optional query string to filter channels by name.
     * @return A list of channels.
     */
    @GET("api/channels")
    suspend fun getChannels(
        @Query("query") query: String? = null
    ): Response<List<Channel>>

    /**
     * Fetch the streaming URL for a given channel.
     *
     * @param streamUrl The URL of the channel to stream.
     * @return The rewritten streaming URL.
     */
    @GET("api/channels/stream")
    suspend fun getStreamUrl(
        @Query("streamUrl") streamUrl: String
    ): Response<ResponseBody> // Use ResponseBody for non-JSON responses

}

/**
 * Singleton object for TvPage API client.
 */
object TvPageApiClient {
    private const val BASE_URL = "https://api.kaminajp.com/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: TvPageApi by lazy {
        retrofit.create(TvPageApi::class.java)
    }
}

