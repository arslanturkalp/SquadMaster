package com.umtualgames.squadmaster.domain.entities.responses.roomresponses

import com.umtualgames.squadmaster.domain.entities.responses.item.Room

data class LeaveRoomResponse(
    val statusCode: Int,
    val message: String,
    val data: Room
)
