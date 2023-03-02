package com.umtualgames.squadmaster.network.responses.item

data class RankItem(
    val userID: Int,
    val point: Int,
    val lastModifyDate: String,
    val userViewModel: User
)