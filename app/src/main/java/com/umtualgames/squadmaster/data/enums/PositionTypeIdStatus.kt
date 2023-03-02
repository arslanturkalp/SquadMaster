package com.umtualgames.squadmaster.data.enums

enum class PositionTypeIdStatus(val value: Int) {
    GOALKEEPER(1),
    DEFENCE(2),
    MIDFIELDER(3),
    FORWARD(4);

    companion object {
        fun getPositionTypeFromId(value: Int) = values().first { it.value == value }
    }
}