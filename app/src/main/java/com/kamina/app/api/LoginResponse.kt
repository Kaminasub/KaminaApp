package com.kamina.app.api

data class LoginResponse(
    val userId: String,
    val token: String,
    val createPin: Boolean,
    val message: String? = null
)
