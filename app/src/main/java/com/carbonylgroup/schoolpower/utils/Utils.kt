/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Handler
import android.os.Message
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.data.Attendance
import com.carbonylgroup.schoolpower.data.StudentData
import com.carbonylgroup.schoolpower.data.StudentInformation
import com.carbonylgroup.schoolpower.data.Subject
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class Utils(private val context: Context) {

    private val gradeColorIds = intArrayOf(
            R.color.A_score_green,
            R.color.B_score_green,
            R.color.Cp_score_yellow,
            R.color.C_score_orange,
            R.color.Cm_score_red,
            R.color.primary_dark,
            R.color.primary,
            R.color.primary
    )

    private val gradeColorIdsPlain = intArrayOf(
            R.color.A_score_green,
            R.color.B_score_green,
            R.color.Cp_score_yellow,
            R.color.C_score_orange,
            R.color.Cm_score_red,
            R.color.primary_dark,
            R.color.primary
    )

    private val gradeDarkColorIdsPlain = intArrayOf(
            R.color.A_score_green_dark,
            R.color.B_score_green_dark,
            R.color.Cp_score_yellow_dark,
            R.color.C_score_orange_dark,
            R.color.Cm_score_red_dark,
            R.color.primary_darker,
            R.color.primary_dark
    )

    private val attendanceColorIds = mapOf(
            "A" to R.color.primary_dark,
            "E" to R.color.A_score_green_dark,
            "L" to R.color.Cp_score_yellow,
            "R" to R.color.Cp_score_yellow_dark,
            "H" to R.color.C_score_orange_dark,
            "T" to R.color.C_score_orange,
            "S" to R.color.primary,
            "I" to R.color.Cm_score_red,
            "X" to R.color.A_score_green,
            "M" to R.color.Cm_score_red_dark,
            "C" to R.color.B_score_green_dark,
            "D" to R.color.B_score_green,
            "P" to R.color.A_score_green,
            "NR" to R.color.C_score_orange,
            "TW" to R.color.primary,
            "RA" to R.color.Cp_score_yellow_darker,
            "NE" to R.color.Cp_score_yellow_light,
            "U" to R.color.Cp_score_yellow_lighter,
            "RS" to R.color.primary_light,
            "ISS" to R.color.primary,
            "FT" to R.color.B_score_green_dark
    )

    val citizenshipCodes: HashMap<String, String> = hashMapOf(
            "M" to "Meeting Expectations",
            "P" to "Partially Meeting Expectations",
            "N" to "Not Yet Meeting Expectations"
    )

    private fun indexOfString(searchString: String, domain: Array<String>):
            Int = domain.indices.firstOrNull { searchString == domain[it] } ?: -1

    /* Color Handler */
    fun getColorByLetterGrade(letterGrade: String) : Int {
        val colorIndex = indexOfString(letterGrade, arrayOf("A", "B", "C+", "C", "C-", "F", "I", "--"))
        return ContextCompat.getColor(context, if (colorIndex!=-1) gradeColorIds[colorIndex] else 7)
    }

    fun getDarkColorByPrimary(originalPrimary: Int) = ContextCompat.getColor(context,
            gradeDarkColorIdsPlain[gradeColorIdsPlain.takeWhile { originalPrimary != ContextCompat.getColor(context, it) }.count()])

    fun getColorByAttendance(context: Context, attendanceCode: String) = ContextCompat.getColor(context,
            attendanceColorIds[attendanceCode] ?: R.color.gray)
    /* Others */
    fun dpToPx(dp: Int) = Math.round(dp * (context.resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))

    fun getAssignmentFlag(key: String): Pair<Int, String> {

        val icon: Int
        val descrip: String

        when (key) {
            "collected" -> {
                icon = R.drawable.ic_check_box_white_24dp
                descrip = context.getString(R.string.collected)
            }
            "late" -> {
                icon = R.drawable.ic_late_white_24dp
                descrip = context.getString(R.string.late)
            }
            "missing" -> {
                icon = R.drawable.ic_missing_white_24dp
                descrip = context.getString(R.string.missing)
            }
            "exempt" -> {
                icon = R.drawable.ic_exempt_white_24dp
                descrip = context.getString(R.string.exempt)
            }
            "excludeInFinalGrade" -> {
                icon = R.drawable.ic_exclude_white_24dp
                descrip = context.getString(R.string.not_include_in_final)
            }
            else -> {
                icon = R.drawable.ic_info_black_24dp
                descrip = context.getString(R.string.unknown_flag)
            }
        }
        return Pair(icon, descrip)
    }

    fun getLatestPeriod(grades: Map<String, Subject.Grade>): String? {

        val termsList = grades.keys
        val forLatestSemester = getSettingsPreference().getString("list_preference_dashboard_display", "0") == "1"

        if (forLatestSemester) {
            if (termsList.contains("S2") && grades["S2"]!!.letter != "--") return "S2"
            else if (termsList.contains("S1") && grades["S1"]!!.letter != "--") return "S1"
            else if (termsList.contains("T4") && grades["T4"]!!.letter != "--") return "T4"
            else if (termsList.contains("T3") && grades["T3"]!!.letter != "--") return "T3"
            else if (termsList.contains("T2") && grades["T2"]!!.letter != "--") return "T2"
            else if (termsList.contains("T1")) return "T1"
        } else {
            if (termsList.contains("T4") && grades["T4"]!!.letter != "--") return "T4"
            else if (termsList.contains("T3") && grades["T3"]!!.letter != "--") return "T3"
            else if (termsList.contains("T2") && grades["T2"]!!.letter != "--") return "T2"
            else if (termsList.contains("T1")) return "T1"
        }

        return null
    }

    fun getLatestPeriodGrade(subject: Subject) = subject.grades[getLatestPeriod(subject.grades)]

    fun getLetterGradeByPercentageGrade(percentageGrade: Float): String {

        val letterGrades = arrayOf("A", "B", "C+", "C", "C-", "F", "I", "--")
        return when {
            percentageGrade >= 86 -> letterGrades[0]
            percentageGrade >= 73 -> letterGrades[1]
            percentageGrade >= 67 -> letterGrades[2]
            percentageGrade >= 60 -> letterGrades[3]
            percentageGrade >= 50 -> letterGrades[4]
            else -> letterGrades[5]
        }
    }

    fun showSnackBar(context: Context, view: View, msg: String, colorRed: Boolean) {

        val snackBar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT)
        if (colorRed) snackBar.view.setBackgroundColor(ContextCompat.getColor(context, R.color.Cm_score_red_dark))
        else snackBar.view.setBackgroundColor(ContextCompat.getColor(context, R.color.accent))
        snackBar.show()
    }

    // Parse the student data fetched.
    // if succeed: return the subject data and the student information.
    // if not：IllegalArgumentException or JSONException may be thrown.
    // the format of the JSON: (Sample)
    /*
    {
        "information": (StudentInformation),
        "sections": [
            (Subject)...
        ]
    }
     */
    @Throws(IllegalArgumentException::class, JSONException::class)
    fun parseJsonResult(jsonStr: String): StudentData {

        val studentData = JSONObject(jsonStr)
        if (!studentData.has("information")) { // not successful
            Log.e("Utils.parseJsonResult", studentData.toString())
            throw IllegalArgumentException("JSON Format Error")
        }
        val studentInfo = StudentInformation(studentData.getJSONObject("information"))
        val attendance = studentData.getJSONArray("attendances")
        val attendances = (0 until attendance.length()).map { Attendance(attendance.getJSONObject(it)) }
        val sections = studentData.getJSONArray("sections")
        val subjects = (0 until sections.length()).map { Subject(sections.getJSONObject(it)) }

        Collections.sort(subjects, Comparator<Subject> { o1, o2 ->
            if (o1.blockLetter == "HR(A-E)") return@Comparator -1
            if (o2.blockLetter == "HR(A-E)") return@Comparator 1
            o1.blockLetter.compareTo(o2.blockLetter)
        })
        val disabled = studentData.has("disabled")
        var disabledTitle : String? = null
        var disabledMessage : String? = null
        if(disabled){
            val disable = studentData.getJSONObject("disabled")
            disabledTitle = disable.getString("title")?:"Access is disabled"
            disabledMessage = disable.getString("message")?:context.getString(R.string.powerschool_disabled)
        }
        return StudentData(studentInfo, attendances, subjects, disabled, disabledTitle, disabledMessage)
    }

    /* IO */
    fun getSettingsPreference() =
            context.getSharedPreferences(context.getString(R.string.settings), Activity.MODE_PRIVATE)

    fun setSettingsPreference(key: String, value: String) {

        val spEditor = context.getSharedPreferences(context.getString(R.string.settings), Activity.MODE_PRIVATE).edit()
        spEditor.putString(key, value)
        spEditor.apply()
    }

    @Throws(IOException::class)
    fun readStringFromFile(fileName: String): String? {

        try {
            val data = StringBuilder("")
            val inputStream = context.openFileInput(fileName)
            val isr = InputStreamReader(inputStream)
            val buffReader = BufferedReader(isr)

            var readString = buffReader.readLine()
            while (readString != null) {
                data.append(readString)
                readString = buffReader.readLine()
            }

            isr.close()
            inputStream.close()

            return data.toString()

        } catch(e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(IOException::class)
    fun saveStringToFile(fileName: String, data: String) {

        val outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
        outputStream.write(data.toByteArray())
        outputStream.close()
    }

    @Throws(IOException::class)
    fun readDataArrayList() = parseJsonResult(readStringFromFile(context.getString(R.string.dataFileName))!!)

    @Throws(IOException::class)
    fun saveDataJson(jsonStr: String) = saveStringToFile(context.getString(R.string.dataFileName), jsonStr)

    @Throws(IOException::class)
    fun saveLangPref(langStr: String) = saveStringToFile(context.getString(R.string.langFileName), langStr)

    fun readLangPref() = readStringFromFile(context.getString(R.string.langFileName))?.toInt()

    // 1. read data into brief info
    // 2. calculate gpa
    // 3. read history grade from file
    // 4. update history grade
    // 5. save history grade
    fun saveHistoryGrade(data: List<Subject>?) {

        if (data == null) {
            saveStringToFile("history.json", "{}")
        } else {
            // 1. read data into brief info
            var pointSum = 0.0
            var count = 0
            val gradeInfo = JSONArray() // [{"name":"...","grade":80.0}, ...]
            for (subject in data) {
                val subInfo = JSONObject()
                val leastPeriod = getLatestPeriodGrade(subject) ?: continue
                if (leastPeriod.percentage == "--") continue

                subInfo.put("name", subject.name)
                subInfo.put("grade", leastPeriod.percentage.toDouble())
                if (!subject.name.contains("Homeroom")) {
                    pointSum += leastPeriod.percentage.toDouble()
                    count++
                }
                gradeInfo.put(subInfo)
            }

            // 2. calculate gpa
            val gpaInfo = JSONObject()
            gpaInfo.put("name", "GPA")
            if (count!=0)
                gpaInfo.put("grade", pointSum / count.toDouble())
            else
                gpaInfo.put("grade", 0.0)
            gradeInfo.put(gpaInfo)

            // 3. read history grade from file
            val historyData = JSONObject(readStringFromFile("history.json") ?: "{}")
            // {"2017-06-20": [{"name":"...","grade":"80"}, ...], ...}

            // 4. update history grade
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date())
            historyData.put(date, gradeInfo)

            // 5. save history grade
            saveStringToFile("history.json", historyData.toString())
        }
    }

    fun readHistoryGrade() = JSONObject(readStringFromFile("history.json") ?: "{}")

    fun checkUpdate() {

        Thread(PostData(context.getString(R.string.updateURL), "", object : Handler() {
            override fun handleMessage(msg: Message) {
                val message = msg.obj.toString()
                if (!message.contains("{")) return
                val updateJSON = JSONObject(message)

                if (updateJSON.getString("version") != context.packageManager.getPackageInfo("com.carbonylgroup.schoolpower", 0).versionName) {
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle(context.getString(R.string.upgrade_title))
                    builder.setMessage(updateJSON.getString("description"))
                    builder.setPositiveButton(context.getString(R.string.upgrade_pos)) {
                        dialog, _ ->
                        run {
                            dialog.dismiss()
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateJSON.getString("url")))
                            context.startActivity(intent)
                        }
                    }
                    builder.setNegativeButton(context.getString(R.string.upgrade_neg), null)
                    builder.create().show()
                }
            }
        })).start()
    }


    fun getFilteredSubjects(subjects: List<Subject>): List<Subject> {

        val filteredSubjects: List<Subject>
        if (!PreferenceManager.getDefaultSharedPreferences(context.applicationContext).getBoolean("list_preference_dashboard_show_inactive", false)) {

            filteredSubjects = ArrayList()
            subjects
                    .filter {
                        val currentTime = System.currentTimeMillis()
                        currentTime>it.startDate && currentTime<it.endDate
                    }
                    .forEach { filteredSubjects.add(it) }

        } else {
            filteredSubjects = subjects
        }
        return filteredSubjects
    }
    companion object {

        // convert date like "2018-01-21T16:00:00.000Z" to timestamp (unit: MILLISECOND)
        fun convertDateToTimestamp(date:String): Long{
            val temp = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(date.replace("T16:00:00.000Z", ""))
            temp.time+=24*60*60*1000
            return temp.time
        }

        fun getShortName(subjectTitle: String): String {
            val shorts = mapOf(
                    "Homeroom" to "HR",
                    "Planning" to "PL",
                    "Mandarin" to "CN",
                    "Chinese" to "CSS",
                    "Foundations" to "Maths",
                    "Physical" to "PE",
                    "English" to "ENG",
                    "Moral" to "ME",
                    "Physics" to "PHY",
                    "Chemistry" to "CHEM",
                    "Exercise" to "EXE",
                    "Social" to "SS"
            )
            val splitedSubject = subjectTitle.split(" ")
            var short = shorts[splitedSubject[0]]
            if (short != null) {
                if (splitedSubject[splitedSubject.size - 1] == "Music") short += "M"
                if (splitedSubject[splitedSubject.size - 1] == "Politics") short += "P"
                if (splitedSubject[splitedSubject.size - 1] == "Sci") short += "S"
                if (splitedSubject[splitedSubject.size - 1] == "Humanities") short += "H"
                if (splitedSubject[splitedSubject.size - 1] == "Arts") short += "A"
                return short
            }

            var ret = ""
            for (c in subjectTitle) {
                if (c.isUpperCase() || c.isDigit()) ret += c
            }
            return ret
        }

        /**
         * @param url    url
         * *
         * @param params name1=value1&name2=value2
         * *
         * @return result
         */
        internal fun sendPost(url: String, params: String): String {

            var out: PrintWriter? = null
            var `in`: BufferedReader? = null
            var result = ""
            try {

                val realUrl = URL(url)
                val conn = realUrl.openConnection()
                conn.setRequestProperty("user-agent", "SchoolPower")
                conn.doOutput = true
                conn.doInput = true
                out = PrintWriter(conn.getOutputStream())
                out.print(params)
                out.flush()
                `in` = BufferedReader(InputStreamReader(conn.getInputStream()))
                var line: String?
                while (true) {
                    line = `in`.readLine()
                    if (line == null) break
                    result += "\n" + line
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    if (out != null) out.close()
                    if (`in` != null) `in`.close()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
            return result
        }
    }
}
