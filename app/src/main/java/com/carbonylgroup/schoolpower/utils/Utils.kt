/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
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


class Utils(private val context: Context) {

    private val gradeColorIds = intArrayOf(R.color.A_score_green, R.color.B_score_green, R.color.Cp_score_yellow, R.color.C_score_orange, R.color.Cm_score_red, R.color.primary_dark, R.color.primary, R.color.primary)
    private val gradeColorIdsPlain = intArrayOf(R.color.A_score_green, R.color.B_score_green, R.color.Cp_score_yellow, R.color.C_score_orange, R.color.Cm_score_red, R.color.primary_dark, R.color.primary)
    private val gradeDarkColorIdsPlain = intArrayOf(R.color.A_score_green_dark, R.color.B_score_green_dark, R.color.Cp_score_yellow_dark, R.color.C_score_orange_dark, R.color.Cm_score_red_dark, R.color.primary_darker, R.color.primary_dark)
    private fun indexOfString(searchString: String, domain: Array<String>): Int = domain.indices.firstOrNull { searchString == domain[it] } ?: -1

    /* Color Handler */
    fun getColorByLetterGrade(context: Context, letterGrade: String) = ContextCompat.getColor(context, gradeColorIds[indexOfString(letterGrade, arrayOf("A", "B", "C+", "C", "C-", "F", "I", "--"))])

    fun getDarkColorByPrimary(originalPrimary: Int) = ContextCompat.getColor(context, gradeDarkColorIdsPlain[gradeColorIdsPlain.takeWhile { originalPrimary != ContextCompat.getColor(context, it) }.count()])

    /* Others */
    fun dpToPx(dp: Int) = Math.round(dp * (context.resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))

    fun getLatestPeriod(grades: Map<String, Subject.Grade>): String? {

        val termsList = grades.keys
        val forLatestSemester = getSettingsPreference("list_preference_dashboard_display") == "1"

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

        return ""
    }

    fun getLatestPeriodGrade(subject: Subject) = subject.grades[getLatestPeriod(subject.grades)]

    fun getLetterGradeByPercentageGrade(percentageGrade: Float): String {

        val letterGrades = arrayOf("A", "B", "C+", "C", "C-", "F", "I", "--")
        if (percentageGrade >= 86) return letterGrades[0]
        else if (percentageGrade >= 73) return letterGrades[1]
        else if (percentageGrade >= 67) return letterGrades[2]
        else if (percentageGrade >= 60) return letterGrades[3]
        else if (percentageGrade >= 50) return letterGrades[4]
        else return letterGrades[5]
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
    fun parseJsonResult(jsonStr: String): Pair<StudentInformation, List<Subject>> {

        val studentData = JSONObject(jsonStr)
        if (!studentData.has("information")) { // not successful
            Log.e("Utils.parseJsonResult", studentData.toString())
            throw IllegalArgumentException("JSON Format Error")
        }
        val studentInfo = StudentInformation(studentData.getJSONObject("information"))
        val sections = studentData.getJSONArray("sections")
        val subjects = (0..sections.length() - 1).map { Subject(sections.getJSONObject(it)) }

        Collections.sort(subjects, Comparator<Subject> { o1, o2 ->
            if (o1.blockLetter == "HR(A-E)") return@Comparator -1
            if (o2.blockLetter == "HR(A-E)") return@Comparator 1
            o1.blockLetter.compareTo(o2.blockLetter)
        })
        return Pair(studentInfo, subjects)
    }

    /* IO */
    fun getSettingsPreference(key: String): String =
            context.getSharedPreferences(context.getString(R.string.settings), Activity.MODE_PRIVATE).getString(key, "0")

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

            filteredSubjects = ArrayList<Subject>()
            subjects
                    .filter {
                        val latest = getLatestPeriodGrade(it)
                        it.assignments.size != 0 || (latest != null && latest.letter != "--")
                    }
                    .forEach { filteredSubjects.add(it) }

        } else {
            filteredSubjects = subjects
        }
        return filteredSubjects
    }
    companion object {

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
