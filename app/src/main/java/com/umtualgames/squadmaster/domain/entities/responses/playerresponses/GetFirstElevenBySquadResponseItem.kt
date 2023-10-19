package com.umtualgames.squadmaster.domain.entities.responses.playerresponses

import com.umtualgames.squadmaster.domain.entities.responses.item.Club
import com.umtualgames.squadmaster.domain.entities.responses.item.Player
import com.umtualgames.squadmaster.domain.entities.responses.item.PotentialAnswer

data class GetFirstElevenBySquadResponseItem(
    val squad: Club,
    val playerList: List<Player>,
    val potentialAnswerList: List<PotentialAnswer>
)