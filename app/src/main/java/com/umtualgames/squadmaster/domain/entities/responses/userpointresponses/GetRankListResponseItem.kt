package com.umtualgames.squadmaster.domain.entities.responses.userpointresponses

import com.umtualgames.squadmaster.domain.entities.responses.item.RankItem

data class GetRankListResponseItem(
    val userTotalPoints: List<RankItem>,
    val userBestPoints: List<RankItem>
)
