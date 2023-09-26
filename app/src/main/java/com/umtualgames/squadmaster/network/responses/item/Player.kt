package com.umtualgames.squadmaster.network.responses.item

import android.os.Parcelable
import com.umtualgames.squadmaster.data.enums.PositionIdStatus
import com.umtualgames.squadmaster.data.enums.PositionTypeIdStatus
import kotlinx.parcelize.Parcelize

@Parcelize
data class Player(
    val squadID: Int,
    val positionID: Int,
    val positionTypeID: Int?,
    val displayName: String,
    val nationality: String,
    val imagePath: String,
    val number: Int?,
    val squadImagePath: String,
    var isVisible: Boolean
) : Parcelable {

    fun isGoalkeeper() =  positionTypeID == PositionTypeIdStatus.GOALKEEPER.value
    fun isDefender() =  positionTypeID == PositionTypeIdStatus.DEFENCE.value
    fun isMidfielder() =  positionTypeID == PositionTypeIdStatus.MIDFIELDER.value
    fun isForward() =  positionTypeID == PositionTypeIdStatus.FORWARD.value

    fun isRightWinger() = positionID == PositionIdStatus.SAK.value
    fun is10Number() = positionID == PositionIdStatus.ON.value
    fun isOF() = positionID == PositionIdStatus.FA.value
}