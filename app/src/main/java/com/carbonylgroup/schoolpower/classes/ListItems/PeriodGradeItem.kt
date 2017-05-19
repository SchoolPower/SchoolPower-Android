/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.classes.ListItems

import java.io.Serializable
import java.util.ArrayList


class PeriodGradeItem(val termIndicator: String, val termLetterGrade: String, val termPercentageGrade: String,
                      val assignmentItemArrayList: ArrayList<AssignmentItem>) : Serializable
