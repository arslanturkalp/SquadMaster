package com.example.squadmaster.network.requests

data class LevelPassRequest(
    val userID: Int,
    val point: Int,
    val leagueID: Int,
    val squadID: Int
)
