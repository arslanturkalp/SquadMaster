package com.umtualgames.squadmaster.domain.entities.responses.roomresponses

data class GetRoomsResponse(
    val statusCode: Int,
    val message: String,
    val data: RoomResponse
)
