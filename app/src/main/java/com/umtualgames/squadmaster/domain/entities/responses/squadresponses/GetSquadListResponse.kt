package com.umtualgames.squadmaster.domain.entities.responses.squadresponses

import com.umtualgames.squadmaster.domain.entities.responses.item.Club

data class GetSquadListResponse(
    val statusCode: Int,
    val message: String,
    val data: List<Club>,
    var levelPass: Boolean
)