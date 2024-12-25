package com.kamina.app.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Data classes for requests and responses
data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val userId: String,
    val token: String,
    val createPin: Boolean,
    val skipPin: Boolean,  // Add this field to indicate users who can skip PIN
    val message: String? = null
)

data class PinLoginRequest(
    val username: String,
    val pin: String
)

data class PinlessLoginRequest(
    val username: String
)


interface ApiService {
    @GET("api/usernames")
    fun getUsernames(): Call<List<UsernameResponse>>

    // Login without a PIN for specific users
    @POST("/api/login-without-pin")
    fun loginWithoutPin(@Body request: PinlessLoginRequest): Call<LoginResponse>

    @POST("api/login-with-pin")
    fun loginWithPin(@Body request: PinLoginRequest): Call<LoginResponse>

    @POST("api/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @GET("api/user_icon/{userId}")
    fun getUserIcon(@Path("userId") userId: String): Call<UserIconResponse>

    @GET("api/entities")
    fun getEntities(): Call<List<CarouselEntityResponse>>



}