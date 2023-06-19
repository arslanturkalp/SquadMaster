package com.umtualgames.squadmaster.network.responses.roomresponses

data class GetRoomsResponse(
    val statusCode: Int,
    val message: String,
    val data: RoomResponse
)
