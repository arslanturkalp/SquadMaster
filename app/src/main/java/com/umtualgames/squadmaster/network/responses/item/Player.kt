package com.umtualgames.squadmaster.network.responses.item

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Player(
    val squadID: Int,
    val positionID: Int,
    val positionTypeID: Int?,
    val countryID: Int,
    val firstName: String,
    val lastName: String,
    val commonName: String,
    val displayName: String,
    val nationality: String,
    val height: String,
    val weight: String?,
    val imagePath: String,
    val squadImagePath: String,
    var isVisible: Boolean
) : Parcelable