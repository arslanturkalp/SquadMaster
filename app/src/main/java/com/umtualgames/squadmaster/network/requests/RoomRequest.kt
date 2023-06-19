package com.umtualgames.squadmaster.network.requests

import com.google.gson.annotations.SerializedName

data class RoomRequest(
    @SerializedName("id")
    val id: String
)