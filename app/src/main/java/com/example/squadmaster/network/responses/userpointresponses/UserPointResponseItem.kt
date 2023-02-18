package com.example.squadmaster.network.responses.userpointresponses

data class UserPointResponseItem(
    val id: Int,
    val userID: Int,
    val point: Int?,
    val bestPoint: Int?,
    val lastModifyDate: String?
)
