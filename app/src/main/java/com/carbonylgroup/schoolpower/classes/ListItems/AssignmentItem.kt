/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.classes.ListItems

import android.util.Log

import java.io.Serializable

class AssignmentItem(var assignmentTitle: String,
                     val assignmentDate: String,
                     val assignmentPercentage: String, val assignmentDividedScore: String,
                     val assignmentGrade: String, val assignmentCategory: String, val assignmentTerm: String) : Serializable {
    var isNew = false
}
