package com.example.squadmaster.network.responses.loginresponses

data class LoginResponse(
    val statusCode: Int,
    val message: String,
    val data: LoginResponseItem
)