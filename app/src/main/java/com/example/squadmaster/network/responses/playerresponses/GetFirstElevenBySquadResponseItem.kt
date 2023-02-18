package com.example.squadmaster.network.responses.playerresponses

import com.example.squadmaster.network.responses.item.Club
import com.example.squadmaster.network.responses.item.Player
import com.example.squadmaster.network.responses.item.PotentialAnswer

data class GetFirstElevenBySquadResponseItem(
    val squad: Club,
    val playerList: List<Player>,
    val potentialAnswerList: List<PotentialAnswer>
)