package com.umtualgames.squadmaster.network.responses.userpointresponses

data class UserPointResponse(
    val statusCode: Int,
    val message: String,
    val data: UserPointResponseItem
)