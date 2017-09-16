/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.classes.Data

import com.carbonylgroup.schoolpower.classes.Utils.Utils
import org.json.JSONObject
import java.io.Serializable
import java.util.*

/*
    Sample:
    {
        "assignments": [
            (AssignmentItem)...
        ],
        "expression": "1(A-E)",
        "finalGrades": [],
        "name": "Course Name",
        "roomName": "100",
        "teacher": {
            "firstName": "John",
            "lastName": "Doe",
            "email": null,
            "schoolPhone": null
        }
    }
 */

class Subject(json: JSONObject) : Serializable {
    val subjectTitle: String
    val teacherName: String
    val blockLetter: String
    val roomNumber: String
    val periodArrayList: ArrayList<Period>

    init{
        
    }

    fun getPeriodGradeItem(term: String) = periodArrayList.firstOrNull { term == it.termIndicator }

    fun getShortName() = Utils.getShortName(subjectTitle)

    fun addPeriodGradeItem(pgi: Period) {
        periodArrayList.add(pgi)
    }
}
