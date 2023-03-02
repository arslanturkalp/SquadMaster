package com.umtualgames.squadmaster.network.responses.squadresponses

import com.umtualgames.squadmaster.network.responses.item.Club

data class GetSquadListResponse(
    val statusCode: Int,
    val message: String,
    val data: List<Club>
)