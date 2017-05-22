/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.classes.ListItems

import java.io.Serializable
import java.util.*

class MainListItem(val subjectTitle: String, val teacherName: String, val blockLetter: String,
                   val roomNumber: String, val periodGradeItemArrayList: ArrayList<PeriodGradeItem>) : Serializable {

    fun getLetterGrade(requiredTerm: PeriodGradeItem?) = periodGradeItemArrayList[periodGradeItemArrayList.indexOf(requiredTerm)].termLetterGrade
    fun getPercentageGrade(requiredTerm: PeriodGradeItem?) = periodGradeItemArrayList[periodGradeItemArrayList.indexOf(requiredTerm)].termPercentageGrade
    fun getAssignmentItemArrayList(term: String) = periodGradeItemArrayList.firstOrNull { term == it.termIndicator }?.assignmentItemArrayList

    fun addPeriodGradeItem(pgi: PeriodGradeItem) {
        periodGradeItemArrayList.add(pgi)
    }
}
