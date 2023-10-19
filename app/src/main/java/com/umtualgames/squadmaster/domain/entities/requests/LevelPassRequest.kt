package com.umtualgames.squadmaster.domain.entities.requests

data class LevelPassRequest(
    val userID: Int,
    val point: Int,
    val leagueID: Int,
    val squadID: Int
)
