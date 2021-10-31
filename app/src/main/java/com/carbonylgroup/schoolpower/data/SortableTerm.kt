/**
 * Copyright (C) 2019 SchoolPower Studio
 */

package com.carbonylgroup.schoolpower.data

class SortableTerm(raw: String)  {

    private var raw: String = ""
    private var letter: String = ""
    var index: Int = 0
    var letterValue: Int = 0

    init {
        this.raw = raw
        this.letter = raw.substring(0, 1)
        this.index = raw.substring(1, 2).toInt()
        this.letterValue = valueOfLetter(this.letter)
    }

    fun getRaw(): String {
        return raw
    }

    private fun valueOfLetter(letter: String): Int {
        return when (letter) {
            "F" -> 1
            "L" -> 2
            "E" -> 3
            "S" -> 4
            else -> 5
        }
    }
}
