package com.umtualgames.squadmaster.domain.entities.responses.loginresponses

data class RegisterResponse(
    val statusCode: Int,
    val message: String,
    val data: RegisterResponseItem
)
