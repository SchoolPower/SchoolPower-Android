/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.data

import com.carbonylgroup.schoolpower.utils.Utils
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
        "finalGrades": {
                "X1": {
                    "percentage": "0.0",
                    "letter": "--"
                },
                "T2": {
                    "percentage": "0.0",
                    "letter": "--"
                },
                "T1": {
                    "percentage": "80.0",
                    "letter": "B"
                },
                "S1": {
                    "percentage": "80.0",
                    "letter": "B"
                }
            ,
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
    data class Grade(val percentage: String, val letter: String)

    val name: String = json.getString("name")
    val teacherName: String = json.getJSONObject("teacher").let { obj -> obj.getString("firstName") + " " + obj.getString("lastName") }
    val blockLetter: String = json.getString("expression")
    val roomNumber: String = json.getString("roomName")
    val assignments: ArrayList<AssignmentItem> = arrayListOf()
    val grades: HashMap<String, Grade> = hashMapOf()

    init {
        if (!json.isNull("assignments")) {
            val jsonAssignments = json.getJSONArray("assignments")
            (0..jsonAssignments.length() - 1).mapTo(assignments) { AssignmentItem(jsonAssignments.getJSONObject(it)) }
        }

        if (!json.isNull("finalGrades")) {
            val finalGrades = json.getJSONObject("finalGrades")
            for (key in finalGrades.keys()) {
                val grade = finalGrades.getJSONObject(key)
                grades[key] = Grade(grade.getString("percent").toDouble().toInt().toString(), grade.getString("letter"))
            }
        }

    }

    fun getShortName() = Utils.getShortName(name)
}