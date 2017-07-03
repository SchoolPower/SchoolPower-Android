/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.classes.ListItems

import com.carbonylgroup.schoolpower.classes.Utils.Utils
import java.io.Serializable
import java.util.*

class Subject(val subjectTitle: String, val teacherName: String, val blockLetter: String,
              val roomNumber: String, val periodArrayList: ArrayList<Period>) : Serializable {

    fun getPeriodGradeItem(term: String) = periodArrayList.firstOrNull { term == it.termIndicator }

    fun getShortName() = Utils.getShortName(subjectTitle)

    fun addPeriodGradeItem(pgi: Period) {
        periodArrayList.add(pgi)
    }
}
