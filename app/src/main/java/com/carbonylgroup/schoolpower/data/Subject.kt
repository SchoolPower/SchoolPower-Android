/**
 * Copyright (C) 2018 SchoolPower Studio
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

class Subject(json: JSONObject, utils: Utils) : Serializable {

    val name: String = json.getString("name")
    val teacherName: String = json.getJSONObject("teacher").let { obj -> obj.getString("firstName") + " " + obj.getString("lastName") }
    val teacherEmail: String = json.getJSONObject("teacher").optString("email")
    val blockLetter: String // init in code
    val roomNumber: String = json.getString("roomName")
    val assignments: ArrayList<AssignmentItem> = arrayListOf()
    val grades: HashMap<String, Grade> = hashMapOf()
    val startDate: Long
    val endDate: Long

    var margin = 0

    private fun getAdjustedExpression(utils: Utils, blockLetterRaw: String): String {
        // Smart block display
        if (!utils.getPreferences().getBoolean("list_preference_even_odd_filter", false)
                || blockLetterRaw == "")
            return blockLetterRaw

        val blockStartIndex = blockLetterRaw.indexOf('(')
        val currentWeekIsEven =
                utils.getPreferences().getBoolean("list_preference_is_even_week", false)

        val blocksToDisplay = arrayListOf<String>()

        var oddEvenWeekFeatureEnabled = false
        // e.g. 1(A-J), 2(B-C,F,H)
        for (block in blockLetterRaw.substring(blockStartIndex + 1, blockLetterRaw.length - 1)
                .split(",")) {
            // A, B, C, D, E
            val odd = block.indexOf('A') != -1
                    || block.indexOf('B') != -1
                    || block.indexOf('C') != -1
                    || block.indexOf('D') != -1
                    || block.indexOf('E') != -1
            // F, G, H, I, J
            val even = block.indexOf('F') != -1
                    || block.indexOf('G') != -1
                    || block.indexOf('H') != -1
                    || block.indexOf('I') != -1
                    || block.indexOf('J') != -1
            if (even) oddEvenWeekFeatureEnabled = true

            if ((even && currentWeekIsEven) || (odd && !currentWeekIsEven))
                blocksToDisplay.add(block)
        }
        if (oddEvenWeekFeatureEnabled) {
            return blockLetterRaw.substring(0, blockStartIndex + 1) +
                    blocksToDisplay.joinToString(",") + ")"
        } else {
            return blockLetterRaw
        }
    }

    init {
        if (!json.isNull("assignments")) {
            val jsonAssignments = json.getJSONArray("assignments")
            (0 until jsonAssignments.length()).mapTo(assignments) { AssignmentItem(jsonAssignments.getJSONObject(it)) }
        }

        val categoriesWeights = CategoryWeightData(utils)

        if (!json.isNull("finalGrades")) {
            val finalGrades = json.getJSONObject("finalGrades")
            for (key in finalGrades.keys()) {
                val grade = finalGrades.getJSONObject(key)
                grades[key] = Grade(grade.getString("percent").toDouble().toInt().toString(),
                        grade.getString("letter"), grade.getString("comment"), grade.getString("eval"),
                        CalculatedGrade(this, key, categoriesWeights))
            }
        }

        startDate = Utils.convertDateToTimestamp(json.getString("startDate"))
        endDate = Utils.convertDateToTimestamp(json.getString("endDate"))


        val blockLetterRaw = json.getString("expression")
        blockLetter = try {
            getAdjustedExpression(utils, blockLetterRaw)
        } catch (e: Exception) {
            blockLetterRaw
        }
    }

    // Call it when weights of categories have been changed.
    fun recalculateGrades(weights: CategoryWeightData) {
        for ((name, grade) in grades) {
            grade.calculatedGrade = CalculatedGrade(this, name, weights)
        }
    }

    // Compare `oldSubject` with this one and mark changed assignments
    fun markNewAssignments(oldSubject: Subject, utils: Utils) {
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
                margin = 0

                val oldPercent = utils.getLatestTermGrade(oldSubject)?.getGrade() ?: continue
                val newPercent = utils.getLatestTermGrade(this)?.getGrade() ?: continue

                if (oldPercent != newPercent)
                    margin = newPercent - oldPercent
            }
        }
    }

    fun getShortName() = Utils.getShortName(name)

    fun getLatestTermName(utils: Utils, forceLastTerm: Boolean = false, preferSemester: Boolean = false): String? =
            utils.getLatestTermName(this.grades, forceLastTerm, preferSemester)

    fun getLatestTermGrade(utils: Utils) = utils.getLatestTermGrade(this)
}