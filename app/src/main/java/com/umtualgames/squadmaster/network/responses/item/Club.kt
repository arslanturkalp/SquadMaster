package com.umtualgames.squadmaster.network.responses.item

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Club(
    val id: Int,
    val leagueID: Int,
    val name: String,
    val shortName: String?,
    val imagePath: String?,
    val leagueOrder: Int,
    val isLocked: Boolean,
    var isPassed: Boolean?
): Parcelable