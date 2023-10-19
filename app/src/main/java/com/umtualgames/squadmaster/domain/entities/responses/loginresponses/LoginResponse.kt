package com.umtualgames.squadmaster.domain.entities.responses.loginresponses

data class LoginResponse(
    val statusCode: Int,
    val message: String,
    val data: LoginResponseItem
)