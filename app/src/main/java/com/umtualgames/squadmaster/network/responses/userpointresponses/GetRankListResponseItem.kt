package com.umtualgames.squadmaster.network.responses.userpointresponses

import com.umtualgames.squadmaster.network.responses.item.RankItem

data class GetRankListResponseItem(
    val userTotalPoints: List<RankItem>,
    val userBestPoints: List<RankItem>
)
