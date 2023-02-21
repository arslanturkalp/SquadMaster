package com.example.squadmaster.network.responses.item

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class League(
    val name: String,
    val imagePath: String,
    val level: Int,
    val isLocked: Boolean
): Parcelable