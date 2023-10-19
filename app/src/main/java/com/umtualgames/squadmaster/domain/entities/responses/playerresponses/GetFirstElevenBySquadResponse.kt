package com.umtualgames.squadmaster.domain.entities.responses.playerresponses

data class GetFirstElevenBySquadResponse(
    val statusCode: Int,
    val message: String,
    val data: GetFirstElevenBySquadResponseItem
)

