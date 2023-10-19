package com.umtualgames.squadmaster.domain.entities.responses.leagueresponses

import com.umtualgames.squadmaster.domain.entities.responses.item.League

data class GetLeaguesResponse(
    val statusCode: Int,
    val message: String,
    val data: List<League>
)
