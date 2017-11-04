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

    }
*/
class Attendance(json: JSONObject) : Serializable {

    val code: String = json.getString("code")
    val description: String = json.optString("percent", "--").replace("null", "--")  // comment or "--"
    var date: String
    val period: String = json.optString("period", "--").replace("null", "--")  // value like "5(A-E)" or "--"
    val subject: String = json.optString("name", "--").replace("null", "--") // value like "English 11" or "--"

    init{
        val temp = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(json.getString("date")
                .replace("T16:00:00.000Z", ""))
        temp.time+=24*60*60*1000
        date = SimpleDateFormat("yyyy/MM/dd", Locale.CHINA).format(temp)
    }
}