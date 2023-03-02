package com.umtualgames.squadmaster.network.responses.playerresponses

import com.umtualgames.squadmaster.network.responses.item.Club
import com.umtualgames.squadmaster.network.responses.item.Player
import com.umtualgames.squadmaster.network.responses.item.PotentialAnswer

data class GetFirstElevenBySquadResponseItem(
    val squad: Club,
    val playerList: List<Player>,
    val potentialAnswerList: List<PotentialAnswer>
)