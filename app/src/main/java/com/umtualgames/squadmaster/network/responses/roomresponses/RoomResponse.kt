package com.umtualgames.squadmaster.network.responses.roomresponses

import com.umtualgames.squadmaster.network.responses.item.Room

data class RoomResponse(
    val roomList: List<Room>,
    val availableCount: Int
)
