package com.kamina.app.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("api/usernames")
    fun getUsernames(): Call<List<UsernameResponse>>

    @POST("api/login-with-pin")
    fun loginWithPin(@Body request: PinLoginRequest): Call<LoginResponse>

    @POST("api/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

     @GET("api/user_icon/{userId}")
     fun getUserIcon(@Path("userId") userId: String): Call<UserIconResponse>

    @GET("api/entities")
    fun getEntities(): Call<List<EntityResponse>>



}

