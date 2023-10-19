package com.umtualgames.squadmaster.domain.entities.responses.userpointresponses

data class GetRankListResponse(
    val statusCode: Int,
    val message: String,
    val data: GetRankListResponseItem
)
