package com.carbonylgroup.schoolpower.data

import org.json.JSONObject
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by carbonyl on 04/11/2017.
 */

/*
Sample:
    {
        "code": "E",
        "description": "Excused Absent",
        "date": "2017-10-16T16:00:00.000Z",
        "period": "3(B,D)",
        "name": "Chinese Social Studies 11"
    }
*/
class Attendance(json: JSONObject) : Serializable {

    val code: String = json.getString("code")
    val description: String = json.optString("description")
    val date: String
    val period: String = json.optString("period")
    val name: String = json.optString("name", "--")

    init{
        val temp = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(json.getString("date")
                .replace("T16:00:00.000Z", ""))
        temp.time+=24*60*60*1000
        date = SimpleDateFormat("yyyy/MM/dd", Locale.CHINA).format(temp)
    }

    var isNew = false
}