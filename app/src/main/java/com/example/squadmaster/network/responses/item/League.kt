package com.example.squadmaster.network.responses.item

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class League(
    val id: Int,
    val name: String,
    val imagePath: String,
    val point: Int,
    val isLocked: Boolean
): Parcelable