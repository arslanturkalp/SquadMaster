package com.example.squadmaster.network.responses.userpointresponses

import com.example.squadmaster.network.responses.item.RankItem

data class GetRankListResponseItem(
    val userTotalPoints: List<RankItem>,
    val userBestPoints: List<RankItem>
)
