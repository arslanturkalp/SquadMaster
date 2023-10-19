package com.umtualgames.squadmaster.domain.entities.responses.item

data class RankItem(
    val userID: Int,
    val point: Int,
    val lastModifyDate: String,
    val userViewModel: User
)