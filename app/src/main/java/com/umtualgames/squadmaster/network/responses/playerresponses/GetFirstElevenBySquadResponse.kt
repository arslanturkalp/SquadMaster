package com.umtualgames.squadmaster.network.responses.playerresponses

data class GetFirstElevenBySquadResponse(
    val statusCode: Int,
    val message: String,
    val data: GetFirstElevenBySquadResponseItem
)

