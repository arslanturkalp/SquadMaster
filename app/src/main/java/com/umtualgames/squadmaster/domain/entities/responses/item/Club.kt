package com.umtualgames.squadmaster.domain.entities.responses.item

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
    val isPassed: Boolean
) : Parcelable