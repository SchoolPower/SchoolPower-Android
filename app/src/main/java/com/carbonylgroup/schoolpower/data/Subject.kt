/**
 * Copyright (C) 2018 SchoolPower Studio
 */

package com.carbonylgroup.schoolpower.data

import android.content.Context
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
        "startDate": "2017-08-31T16:00:00.000Z",
        "endDate": "2018-01-21T16:00:00.000Z",
        "finalGrades": {
            "X1": {
                "percent": "0.0",
                "letter": "--",
                "comment": null,
                "eval": "--",
                "startDate": 1515945600,
                "endDate": 1515945600
            },
            "T2": {
                "percent": "92.0",
                "letter": "A",
                "comment": "Some comments",
                "eval": "M",
                "startDate": 1510502400,
                "endDate": 1510502400
            },
            "T1": {
                "percent": "90.0",
                "letter": "A",
                "comment": "Some comments",
                "eval": "M",
                "startDate": 1504195200,
                "endDate": 1504195200
            },
            "S1": {
                "percent": "91.0",
                "letter": "A",
                "comment": null,
                "eval": "M",
                "startDate": 1504195200,
                "endDate": 1504195200
            }
        },
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

    data class Grade(val percentage: String, val letter: String, val comment: String, val evaluation: String)

    val name: String = json.getString("name")
    val teacherName: String = json.getJSONObject("teacher").let { obj -> obj.getString("firstName") + " " + obj.getString("lastName") }
    val teacherEmail: String = json.getJSONObject("teacher").optString("email")
    val blockLetter: String = json.getString("expression")
    val roomNumber: String = json.getString("roomName")
    val assignments: ArrayList<AssignmentItem> = arrayListOf()
    val grades: HashMap<String, Grade> = hashMapOf()
    val startDate: Long
    val endDate: Long

    var margin = 0

    init {
        if (!json.isNull("assignments")) {
            val jsonAssignments = json.getJSONArray("assignments")
            (0 until jsonAssignments.length()).mapTo(assignments) { AssignmentItem(jsonAssignments.getJSONObject(it)) }
        }

        if (!json.isNull("finalGrades")) {
            val finalGrades = json.getJSONObject("finalGrades")
            for (key in finalGrades.keys()) {
                val grade = finalGrades.getJSONObject(key)
                grades[key] = Grade(grade.getString("percent").toDouble().toInt().toString(),
                        grade.getString("letter"), grade.getString("comment"), grade.getString("eval"))
            }
        }

        startDate = Utils.convertDateToTimestamp(json.getString("startDate"))
        endDate = Utils.convertDateToTimestamp(json.getString("endDate"))
    }

    // Compare `oldSubject` with this one and mark changed assignments
    fun markNewAssignments(oldSubject: Subject, context: Context) {
        val utils = Utils(context)
        // Mark new or changed assignments
        val newAssignmentListCollection = assignments
        val oldAssignmentListCollection = oldSubject.assignments
        for (item in newAssignmentListCollection) {
            // if no item in oldAssignmentListCollection has the same title, score and date as those of the new one, then the assignment should be marked.
            val found = oldAssignmentListCollection.any {
                it.title == item.title && it.score == item.score && it.date == item.date && !it.isNew
            }
            if (!found) {
                item.isNew = true

                var oldPercent = 0
                var newPercent = 0
                var oldPercentStr = "--"
                var newPercentStr = "--"
                if (utils.getLatestPeriodGrade(oldSubject) != null)
                    oldPercentStr = utils.getLatestPeriodGrade(oldSubject)!!.percentage
                if (utils.getLatestPeriodGrade(this) != null)
                    newPercentStr = utils.getLatestPeriodGrade(this)!!.percentage
                if (oldPercentStr != "--") oldPercent = oldPercentStr.toInt()
                if (newPercentStr != "--") newPercent = newPercentStr.toInt()

                if (oldPercent != newPercent)
                    margin = newPercent - oldPercent
            }
        }
    }

    fun getShortName() = Utils.getShortName(name)
}