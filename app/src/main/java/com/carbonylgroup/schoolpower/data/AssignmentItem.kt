/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.data

import android.util.Log
import com.carbonylgroup.schoolpower.utils.Utils
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

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
    val date: String
    val percentage: String = json.optString("percent", "--").replace("null", "--")  // value like "86.96" or "--"
    val score: String = json.optString("score", "--").replace("null", "--")  // value like "20" or "--"
    val maximumScore: String = json.getString("pointsPossible") // value like "23.0"
    val letterGrade: String = json.optString("letterGrade", "--").replace("null", "--") // value like "A" or "--"
    val category: String = json.getString("category")
    val includeInFinalGrade: Boolean = json.getString("includeInFinalGrade") == "1"
    val weight: String = json.getString("weight")
    val terms: List<String>

//    val flags: ArrayList<Pair<String, Boolean>> = arrayListOf(
//            Pair("collected", true),
//            Pair("late", true),
//            Pair("missing", true),
//            Pair("exempt", true),
//            Pair("excludeInFinalGrade", true)
//    )

    var flags: ArrayList<Pair<String, Boolean>> = arrayListOf()
    val trueFlags: ArrayList<Pair<String, Boolean>> = arrayListOf()
    var isNew = false

    init {
        val termsJSON = json.getJSONArray("terms")
        terms = (0 until termsJSON.length()).map { termsJSON.getString(it) }
        date = SimpleDateFormat("yyyy/MM/dd", Locale.CHINA)
                .format(Utils.convertDateToTimestamp(json.getString("date")))

        try {
            flags = arrayListOf(
                    Pair("collected", json.getJSONObject("status").getBoolean("collected")),
                    Pair("late", json.getJSONObject("status").getBoolean("late")),
                    Pair("missing", json.getJSONObject("status").getBoolean("missing")),
                    Pair("exempt", json.getJSONObject("status").getBoolean("exempt")),
                    Pair("excludeInFinalGrade",
                            if (json.getJSONObject("status").has("includeInFinalGrade"))
                                !json.getJSONObject("status").getBoolean("includeInFinalGrade")
                            else false)
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        for (flag in flags) {
            if (flag.second) trueFlags.add(flag)
        }
    }

    fun getDividedScore() = "$score/$maximumScore"
}
