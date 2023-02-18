package com.example.squadmaster.utils

import com.example.squadmaster.network.responses.item.Player

fun ifTwoBack(defences: ArrayList<Player>) : List<Player> {

    if (defences.count { it.positionID == 2 } > 1 && defences.count() == 5) {
        val player = defences.first { it.positionID == 2 }
        defences.remove(player)
        defences.add(4, player)
    }

    return defences
}

fun ifTwoWinger(attackers: ArrayList<Player>) : List<Player> {

    if (attackers.count { it.positionID == 11 } > 1 && attackers.count() == 3) {
        val player = attackers.first { it.positionID == 11}
        attackers.remove(player)
        attackers.add(2, player)
    }

    else if (attackers.count { it.positionID == 13 } > 1 && attackers.count() == 3) {
        val player = attackers.first { it.positionID == 13 }
        attackers.remove(player)
        attackers.add(0, player)
    }

    return attackers
}