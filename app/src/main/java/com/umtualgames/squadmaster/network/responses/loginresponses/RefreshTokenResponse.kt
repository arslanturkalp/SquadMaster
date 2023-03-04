package com.umtualgames.squadmaster.network.responses.loginresponses

data class RefreshTokenResponse(
    val statusCode: Int,
    val message: String,
    val data: RefreshTokenResponseItem
)