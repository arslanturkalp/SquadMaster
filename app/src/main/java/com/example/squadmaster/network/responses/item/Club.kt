package com.example.squadmaster.network.responses.item

data class Club(
    val id: Int,
    val leagueID: Int,
    val name: String,
    val shortName: String,
    val imagePath: String?
)