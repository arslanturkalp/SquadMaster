package com.example.squadmaster.utils

fun ifContains(nation: String): String =
    if (nation.contains("Korea")) { "kor" }
    else if (nation.contains("Czech")) { "cz" }
    else if (nation.contains("Russia")) { "ru" }
    else if (nation.contains("Ireland")) { "irl" }
    else if (nation.contains("Congo")) { "cg" }
    else if (nation.contains("Macedonia")) { "mkd" }
    else if (nation.contains("Cape Verde")) { "cv" }
    else if (nation.contains("Panama")) { "pa" }
    else if (nation.contains("Fransa")) { "fr" }
    else if (nation.contains("Türkiye")) { "tr" }
    else if (nation.contains("Brezilya")) { "br" }
    else if (nation.contains("Arjantin")) { "ar" }
    else if (nation.contains("İngiltere")) { "england" }
    else nation
