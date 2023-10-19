package com.umtualgames.squadmaster.domain.entities.responses.userpointresponses

data class UserPointResponse(
    val statusCode: Int,
    val message: String,
    val data: UserPointResponseItem
)