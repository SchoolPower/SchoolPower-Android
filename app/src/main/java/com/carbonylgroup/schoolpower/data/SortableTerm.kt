/**
 * Copyright (C) 2018 SchoolPower Studio
 */

package com.carbonylgroup.schoolpower.data

class SortableTerm(raw: String)  {

    private var raw: String = ""
    private var letter: String = ""
    private var index: Int = 0

    init {
        this.raw = raw
        this.letter = raw.substring(0, 1)
        this.index = raw.substring(1, 2).toInt()
    }

    fun getValue(): Int {
        return valueOfLetter(letter) * index
    }

    fun getRaw(): String {
        return raw
    }

    private fun valueOfLetter(letter: String): Int {
        // This is so that S1 > T4/Q4, Y1 > S2, X1 > Y1
        // Don't know what's X, assume it's greater than Y here :P
        return when (letter) {
            "T" -> 1
            "Q" -> 1
            "S" -> 5
            "Y" -> 11
            "X" -> 12
            else -> 0
        }
    }
}
