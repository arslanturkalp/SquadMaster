package com.umtualgames.squadmaster.domain.entities.responses.roomresponses

import com.umtualgames.squadmaster.domain.entities.responses.item.Room

data class RoomResponse(
    val roomList: List<Room>,
    val availableCount: Int
)
