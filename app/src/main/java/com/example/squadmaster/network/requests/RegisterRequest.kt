package com.example.squadmaster.network.requests

data class RegisterRequest(
    val name: String,
    val surname: String,
    val username: String,
    val email: String,
    val password: String,
    val roleID: Int
)