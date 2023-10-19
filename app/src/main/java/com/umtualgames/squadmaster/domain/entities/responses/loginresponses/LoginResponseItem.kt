package com.umtualgames.squadmaster.domain.entities.responses.loginresponses

import com.umtualgames.squadmaster.domain.entities.responses.item.Token

data class LoginResponseItem(
    val id: Int,
    val name: String,
    val surname: String,
    val username: String,
    val email: String,
    val token: Token
)
