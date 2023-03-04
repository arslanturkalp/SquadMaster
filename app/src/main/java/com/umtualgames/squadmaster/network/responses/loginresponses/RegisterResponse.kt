package com.umtualgames.squadmaster.network.responses.loginresponses

data class RegisterResponse(
    val statusCode: Int,
    val message: String,
    val data: RegisterResponseItem
)
