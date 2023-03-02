package com.umtualgames.squadmaster.network.responses.leagueresponses

import com.umtualgames.squadmaster.network.responses.item.League

data class GetLeaguesResponse(
    val statusCode: Int,
    val message: String,
    val data: List<League>
)
