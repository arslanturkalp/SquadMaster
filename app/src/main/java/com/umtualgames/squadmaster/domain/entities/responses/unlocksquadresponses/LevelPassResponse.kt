package com.umtualgames.squadmaster.domain.entities.responses.unlocksquadresponses

data class LevelPassResponse(
    val statusCode: Int,
    val message: String,
    val data: LevelPassResponseItem
)
