package com.umtualgames.squadmaster.domain.entities.responses.userpointresponses

data class UserPointResponseItem(
    val id: Int,
    val userID: Int,
    val point: Int?,
    val bestPoint: Int?,
    val lastModifyDate: String?,
    var lastPoint: Int?
)
