package com.umtualgames.squadmaster.network.responses.loginresponses

import com.umtualgames.squadmaster.network.responses.item.Token

data class RefreshTokenResponseItem(
    val id: Int,
    val name: String,
    val surname: String,
    val username: String,
    val email: String,
    val token: Token
)
