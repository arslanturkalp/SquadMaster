package com.umtualgames.squadmaster.domain.entities.responses.loginresponses

data class RefreshTokenResponse(
    val isSuccess: Boolean,
    val statusCode: Int,
    val message: String,
    val data: RefreshTokenResponseItem
)