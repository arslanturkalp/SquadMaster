package com.umtualgames.squadmaster.utils

fun String.replaceChars(): String {
    return this.replace("Ç", "C")
        .replace("ç", "c")
        .replace("İ", "I")
        .replace("ı", "i")
        .replace("Ö", "O")
        .replace("ö", "o")
        .replace("Ş", "S")
        .replace("ş", "s")
        .replace("Ü", "U")
        .replace("ü", "u")
}