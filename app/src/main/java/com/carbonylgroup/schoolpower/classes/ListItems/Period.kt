/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.classes.ListItems

import java.io.Serializable
import java.util.ArrayList


class Period(val termIndicator: String, val termLetterGrade: String, val termPercentageGrade: String,
             val assignmentItemArrayList: ArrayList<AssignmentItem>) : Serializable{
    fun getIntegerGrade() : Int {
        if(termPercentageGrade=="--") return 0
        return termPercentageGrade.toInt()
    }
}
