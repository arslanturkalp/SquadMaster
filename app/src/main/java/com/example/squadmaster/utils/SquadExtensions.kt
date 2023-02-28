package com.example.squadmaster.utils

import com.example.squadmaster.data.enums.PositionIdStatus
import com.example.squadmaster.network.responses.item.Player

fun ifTwoBack(defences: ArrayList<Player>): List<Player> {

    if (defences.count { it.positionID == PositionIdStatus.SAB.value } > 1 && defences.count() == 5) {
        val player = defences.first { it.positionID == PositionIdStatus.SAB.value }
        defences.remove(player)
        defences.add(4, player)
    }

    return defences
}

fun ifTwoWinger(attackers: ArrayList<Player>): List<Player> {

    if (attackers.count { it.positionID == PositionIdStatus.SAK.value } > 1 && attackers.count() == 3) {
        val player = attackers.first { it.positionID == PositionIdStatus.SAK.value }
        attackers.remove(player)
        attackers.add(2, player)
    } else if (attackers.count { it.positionID == PositionIdStatus.SOK.value } > 1 && attackers.count() == 3) {
        val player = attackers.first { it.positionID == PositionIdStatus.SOK.value }
        attackers.remove(player)
        attackers.add(0, player)
    }

    return attackers
}

fun ifExists10Number(squad: List<Player>): Boolean {
    val number10s = squad.count { it.positionID == PositionIdStatus.FA.value || it.positionID == PositionIdStatus.ON.value }
    val rightWingers = squad.count { it.positionID == PositionIdStatus.SAK.value }
    val centerForward = squad.count { it.positionID == PositionIdStatus.S.value }
    val leftWingers = squad.count { it.positionID == PositionIdStatus.SOK.value }
    if (number10s == 0 && (rightWingers + centerForward + leftWingers == 5)) {
        return false
    }
    return true
}