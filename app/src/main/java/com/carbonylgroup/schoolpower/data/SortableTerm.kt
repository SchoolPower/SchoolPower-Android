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
        return when (letter) {
            "T" -> 1
            "Q" -> 1
            "S" -> 2
            "Y" -> 3
            "X" -> 4
            else -> 0
        }
    }
}
