/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.classes.ListItems

import java.io.Serializable
import java.util.ArrayList

class MainListItem(val subjectTitle: String, val teacherName: String, val blockLetter: String,
                   val roomNumber: String, val periodGradeItemArrayList: ArrayList<PeriodGradeItem>) : Serializable {

    val letterGrade: String
        get() = periodGradeItemArrayList[0].termLetterGrade

    val percentageGrade: String
        get() = periodGradeItemArrayList[0].termPercentageGrade

    fun getAssignmentItemArrayList(term: String): ArrayList<AssignmentItem>? {
        return periodGradeItemArrayList
                .firstOrNull { term == it.termIndicator }
                ?.assignmentItemArrayList
    }

    fun addPeriodGradeItem(pgi: PeriodGradeItem) {
        periodGradeItemArrayList.add(pgi)
    }
}
