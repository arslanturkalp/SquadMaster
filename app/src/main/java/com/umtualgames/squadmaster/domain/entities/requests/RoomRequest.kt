package com.umtualgames.squadmaster.domain.entities.requests

import com.google.gson.annotations.SerializedName

data class RoomRequest(
    @SerializedName("id")
    val id: String
)