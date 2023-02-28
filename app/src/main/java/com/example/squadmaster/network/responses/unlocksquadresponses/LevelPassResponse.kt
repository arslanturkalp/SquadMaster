package com.example.squadmaster.network.responses.unlocksquadresponses

data class LevelPassResponse(
    val statusCode: Int,
    val message: String,
    val data: LevelPassResponseItem
)
