package com.example.squadmaster.network.responses.loginresponses

import com.example.squadmaster.network.responses.item.Token

data class LoginResponseItem(
    val id: Int,
    val name: String,
    val surname: String,
    val username: String,
    val email: String,
    val token: Token
)
