/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.classes.Data

import org.json.JSONObject
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

/*
Sample:
    {
        "category": "Quizzes",
        "description": "Steps of the scientific process for science fair project",
        "name": "Scientific Method Quiz",
        "percentage": "86.96",
        "score": "20",
        "letterGrade": "A",
        "pointsPossible": "23.0",
        "date": "2017-09-11T16:00:00.000Z",
        "weight": "0.43",
        "includeInFinalGrade": "1"
    },
    {
        "category": "Quizzes",
        "description": null,
        "name": "Scientific Notation Quiz",
        "percentage": null,
        "score": null,
        "letterGrade": null,
        "pointsPossible": "10.0",
        "date": "2017-09-05T16:00:00.000Z",
        "weight": "1.0",
        "includeInFinalGrade": "1"
    }
*/
class AssignmentItem(json: JSONObject) : Serializable {
    val title: String = json.getString("name")
    val date: String = SimpleDateFormat("yyyy/MM/dd", Locale.CHINA).format(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US).parse(json.getString("date")))
    val percentage: String = json.optString("percent", "--").replace("null", "--")  // value like "86.96" or "--"
    val score: String = json.optString("score", "--").replace("null", "--")  // value like "20" or "--"
    val maximumScore: String = json.getString("pointsPossible") // value like "23.0"
    val letterGrade: String = json.optString("letterGrade", "--").replace("null", "--") // value like "A" or "--"
    val category: String = json.getString("category")
    val includeInFinalGrade: Boolean = json.getString("includeInFinalGrade") == "1"
    val weight: String = json.getString("weight")
    //val term: String

    var isNew = false

    fun getDividedScore() = "$score/$maximumScore"
}
