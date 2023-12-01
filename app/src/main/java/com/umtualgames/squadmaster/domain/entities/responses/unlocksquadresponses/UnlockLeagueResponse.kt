package com.umtualgames.squadmaster.domain.entities.responses.unlocksquadresponses

data class UnlockLeagueResponse(
    val isSuccess: Boolean,
    val statusCode: Int,
    val message: String,
    val data: UnlockLeagueResponseItem
)
