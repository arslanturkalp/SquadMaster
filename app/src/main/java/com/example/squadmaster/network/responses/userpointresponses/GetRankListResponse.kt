package com.example.squadmaster.network.responses.userpointresponses

data class GetRankListResponse(
    val statusCode: Int,
    val message: String,
    val data: GetRankListResponseItem
)
