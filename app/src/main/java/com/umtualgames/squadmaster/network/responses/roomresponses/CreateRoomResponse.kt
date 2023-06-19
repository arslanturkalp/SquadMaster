package com.umtualgames.squadmaster.network.responses.roomresponses

import com.umtualgames.squadmaster.network.responses.item.Room

data class CreateRoomResponse(
    val statusCode: Int,
    val message: String,
    val data: Room
)
