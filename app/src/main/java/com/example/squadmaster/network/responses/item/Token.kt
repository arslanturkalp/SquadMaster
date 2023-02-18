package com.example.squadmaster.network.responses.item

data class Token(
    val accessToken: String,
    val expiration: String,
    val refreshToken: String
)
