package com.carbonylgroup.schoolpower.data

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import com.carbonylgroup.schoolpower.R
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Null on 2017/11/5.
 */

// Parse the student data fetched.
// if not succeed：IllegalArgumentException or JSONException may be thrown.
// the format of the JSON: (Sample)
/*
{
    "information": (StudentInformation),
    "sections": [
        (Subject)...
    ]
}
 */
class StudentData(context: Context, jsonStr: String) {

    val studentInfo: StudentInformation
    val attendances: List<Attendance>
    val subjects: List<Subject>
    val disabled: Boolean
    val disabledTitle: String?
    val disabledMessage: String?
    val extraInfo: ExtraInfo
    val ildNotification: ILDNotification

    init {
        val studentData = JSONObject(jsonStr)
        if (!studentData.has("information")) { // not successful
            Log.e("Utils.parseJsonResult", studentData.toString())
            throw IllegalArgumentException("JSON Format Error")
        }
        studentInfo = StudentInformation(studentData.getJSONObject("information"))
        val attendance = studentData.getJSONArray("attendances")
        attendances = (0 until attendance.length()).map { Attendance(attendance.getJSONObject(it)) }
        val sections = studentData.getJSONArray("sections")
        subjects = (0 until sections.length()).map { Subject(sections.getJSONObject(it)) }

        Collections.sort(subjects, Comparator<Subject> { o1, o2 ->
            if (o1.blockLetter == "HR(A-E)") return@Comparator -1
            if (o2.blockLetter == "HR(A-E)") return@Comparator 1
            o1.blockLetter.compareTo(o2.blockLetter)
        })
        disabled = studentData.has("disabled")
        if (disabled) {
            val disable = studentData.getJSONObject("disabled")
            disabledTitle = disable.getString("title") ?: "Access is disabled"
            disabledMessage = disable.getString("message") ?: context.getString(R.string.powerschool_disabled)
        } else {
            disabledTitle = null
            disabledMessage = null
        }
        extraInfo = if (studentData.has("additional")) {
            val additional = studentData.getJSONObject("additional")
            ExtraInfo(avatar = additional["avatar"].toString())
        } else {
            ExtraInfo(avatar = "")
        }
        ildNotification = if (studentData.has("ild_notification")) {
            val notification = studentData.getJSONObject("ildNotification")
            ILDNotification(
                    present = true,
                    uuid = notification["uuid"].toString(),
                    headerImageURL = notification["image"].toString(),
                    titles = notification.getJSONArray("titles").toArrayList(),
                    messages = notification.getJSONArray("messages").toArrayList(),
                    primaryTexts = notification.getJSONArray("primaryTexts").toArrayList(),
                    secondaryTexts = notification.getJSONArray("secondaryTexts").toArrayList(),
                    dismissTexts = notification.getJSONArray("dismissTexts").toArrayList(),
                    hideDismiss = notification["hideDismiss"].toString().toBoolean(),
                    hideSecondary = notification["hideSecondary"].toString().toBoolean(),
                    onlyOnce = notification["onlyOnce"].toString().toBoolean(),
                    primaryOnClickListenerIndex = notification["primaryOnClick"].toString().toInt()
            )
        } else {
            ILDNotification(present = false)
        }
    }

    private fun JSONArray.toArrayList(): ArrayList<String> {
        val list = ArrayList<String>()
        val jArray = this
        for (i in 0 until jArray.length()) {
            list.add(jArray.getString(i))
        }
        return list
    }
}

class ExtraInfo(val avatar: String)

class ILDNotification(
        val present: Boolean,
        val uuid: String = "",
        val headerImageURL: String = "",
        val titles: ArrayList<String> = arrayListOf("", "", ""),
        val messages: ArrayList<String> = arrayListOf("", "", ""),
        val primaryTexts: ArrayList<String> = arrayListOf("", "", ""),
        val secondaryTexts: ArrayList<String> = arrayListOf("", "", ""),
        val dismissTexts: ArrayList<String> = arrayListOf("", "", ""),
        val hideDismiss: Boolean = true,
        val hideSecondary: Boolean = true,
        val onlyOnce: Boolean = true,
        // Experimental
        val primaryOnClickListenerIndex: Int = -1
)
