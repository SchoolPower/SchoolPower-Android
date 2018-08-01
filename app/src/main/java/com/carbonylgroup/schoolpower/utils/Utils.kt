/**
 * Copyright (C) 2018 SchoolPower Studio
 */

package com.carbonylgroup.schoolpower.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.preference.PreferenceManager
import android.support.annotation.ColorInt
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.view.View
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.data.StudentData
import com.carbonylgroup.schoolpower.data.Subject
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class Utils(private val context: Context) {

    val THEME = "appTheme"
    val ACCENT_COLOR = "accentColor"
    val LIGHT = "LIGHT"
    val DARK = "DARK"

    val LIGHT_BLUE = 0
    val BLUE = 1
    val INDIGO = 2
    val ORANGE = 3

    val YELLOW = 4
    val AMBER = 5
    val GREY = 6
    val BROWN = 7

    val CYAN = 8
    val TEAL = 9
    val LIME = 10
    val GREEN = 11

    val PINK = 12
    val RED = 13
    val PURPLE = 14
    val DEEP_PURPLE = 15

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
            R.color.primary,
            R.color.dark_color_primary
    )

    private val gradeDarkColorIdsPlain = intArrayOf(
            R.color.A_score_green_dark,
            R.color.B_score_green_dark,
            R.color.Cp_score_yellow_dark,
            R.color.C_score_orange_dark,
            R.color.Cm_score_red_dark,
            R.color.primary_darker,
            R.color.primary_dark,
            R.color.dark_color_primary_dark
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


    fun getDefaultSp(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun getTheme(): String {
        return getDefaultSp(context).getString(THEME, LIGHT)
    }

    fun getAccentColorIndex() = getDefaultSp(context).getInt(ACCENT_COLOR, 8)

    @ColorInt
    fun getPrimaryColor() = getColorAttr(R.attr.colorPrimary)

    @ColorInt
    fun getPrimaryDarkColor() = getColorAttr(R.attr.colorPrimaryDark)

    @ColorInt
    fun getAccentColor() = getColorAttr(R.attr.colorAccent)

    @ColorInt
    fun getPrimaryTextColor() = getColorAttr(android.R.attr.textColorPrimary)

    @ColorInt
    fun getSecondaryTextColor() = getColorAttr(android.R.attr.textColorSecondary)

    @ColorInt
    fun getWindowBackground() = getColorAttr(android.R.attr.windowBackground)

    @ColorInt
    fun getCardBackground() = getColorAttr(R.attr.card_background)

    @ColorInt
    fun getIconColor() = getColorAttr(R.attr.icon_color)

    @ColorInt
    fun getTitleColor() = getColorAttr(R.attr.title_color)

    @ColorInt
    fun getSubTitleColor() = getColorAttr(R.attr.subtitle_color)

    @ColorInt
    fun getSelectedColor() = getColorAttr(R.attr.selected_color)

    @ColorInt
    private fun getColorAttr(attr: Int): Int {
        val theme = context.theme
        val typedArray = theme.obtainStyledAttributes(intArrayOf(attr))
        val color = typedArray.getColor(0, Color.LTGRAY)
        typedArray.recycle()
        return color
    }

    operator fun set(key: String, value: Any) {
        if (StringUtils.isBlank(key)) {
            throw NullPointerException(String.format("Key and value not be null key=%s, value=%s", key, value))
        }
        val edit = getDefaultSp(context).edit()
        when (value) {
            is String -> edit.putString(key, value)
            is Int -> edit.putInt(key, value)
            is Long -> edit.putLong(key, value)
            is Boolean -> edit.putBoolean(key, value)
            is Float -> edit.putFloat(key, value)
            is Set<*> -> edit.putStringSet(key, value as Set<String>)
            else -> throw IllegalArgumentException(String.format("Type of value unsupported key=%s, value=%s", key, value))
        }
        edit.apply()
    }

    /* Color Handler */
    fun getColorByLetterGrade(letterGrade: String): Int {
        val colorIndex = indexOfString(letterGrade, arrayOf("A", "B", "C+", "C", "C-", "F", "I", "--"))
        return ContextCompat.getColor(context, if (colorIndex != -1) gradeColorIds[colorIndex] else 7)
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

    fun getLatestPeriod(grades: Map<String, Subject.Grade>, forceLastTerm: Boolean = false): String? {

        val termsList = grades.keys
        val forLatestSemester = getSharedPreference(SettingsPreference)
                .getString("list_preference_dashboard_display", "0") == "1"

        if (forLatestSemester && !forceLastTerm) {
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

    fun showSnackBar(view: View, msg: String, colorRed: Boolean) {
        val snackBar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT)
        if (colorRed) snackBar.view.setBackgroundColor(ContextCompat.getColor(context, R.color.Cm_score_red_dark))
        else snackBar.view.setBackgroundColor(ContextCompat.getColor(context, R.color.accent))
        snackBar.show()
    }

    fun getAppVersion() = context.packageManager.getPackageInfo("com.carbonylgroup.schoolpower", 0).versionName!!

    fun getSharedPreference(database: String) =
            context.getSharedPreferences(database, Activity.MODE_PRIVATE)!!

    fun setSharedPreference(database: String, key: String, value: String) {
        val spEditor = context.getSharedPreferences(database, Activity.MODE_PRIVATE).edit()
        spEditor.putString(key, value)
        spEditor.apply()
    }

    fun setSharedPreference(database: String, key: String, value: Long) {
        val spEditor = context.getSharedPreferences(database, Activity.MODE_PRIVATE).edit()
        spEditor.putLong(key, value)
        spEditor.apply()
    }

    fun setSharedPreference(database: String, key: String, value: Boolean) {
        val spEditor = context.getSharedPreferences(database, Activity.MODE_PRIVATE).edit()
        spEditor.putBoolean(key, value)
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

        } catch (e: Exception) {
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
    fun readDataArrayList() = StudentData(context, readStringFromFile(StudentDataFileName)!!)

    @Throws(IOException::class)
    fun saveDataJson(jsonStr: String) = saveStringToFile(StudentDataFileName, jsonStr)

    fun updateStatisticalData(data: List<Subject>?) {
        // it is organized into a json like {"subject-name": [{"sum": 100, "cat-1":100, ...}, ...], ...}

        if (data == null) return

        val json = JSONObject(readStringFromFile(StatisticalDataFileName) ?: "{}")

        for (subject in data) {

            val term = getLatestPeriod(subject.grades, true) ?: continue
            if (subject.grades[term]!!.percentage == "--") continue
            val percentage = subject.grades[term]!!.percentage.toDouble()

            class GradeInfo {
                var grade: Double = 0.0
                var maxGrade: Double = 0.0
            }

            val categories = HashMap<String, GradeInfo>()
            for (assignment in subject.assignments) {
                if (!categories.containsKey(assignment.category)) categories[assignment.category] = GradeInfo()
                if (assignment.score.toDoubleOrNull() == null) continue
                if (!assignment.terms.contains(term)) continue
                categories[assignment.category]!!.grade += assignment.score.toDouble() * assignment.weight.toDouble()
                categories[assignment.category]!!.maxGrade += assignment.maximumScore.toDouble() * assignment.weight.toDouble()
            }
            val sample = JSONObject() // categories {"sum": 100, "cat-1":100, ...}

            for (cat in categories.entries)
                if (cat.value.maxGrade != 0.0)
                    sample.put(cat.key, cat.value.grade / cat.value.maxGrade)
            sample.put("sum", percentage)

            if (!json.has(subject.name)) json.put(subject.name, JSONArray())
            val jsonSubjectSamples = json.getJSONArray(subject.name) // [sample1, sample2, ...]
            var duplicate = false
            for (i in 0 until jsonSubjectSamples.length()) {
                var same = true
                val tested = jsonSubjectSamples.getJSONObject(i)
                for (key in tested.keys()) {
                    if (!sample.has(key) || tested.getDouble(key) != sample.getDouble(key)) {
                        same = false
                        break
                    }
                }
                if (same) duplicate = true
                break
            }
            if (duplicate) continue
            json.getJSONArray(subject.name).put(sample)
        }

        saveStringToFile(StatisticalDataFileName, json.toString())
    }

    fun saveHistoryGrade(data: List<Subject>?) {
        // 1. read data into brief info
        // 2. calculate gpa
        // 3. read history grade from file
        // 4. update history grade
        // 5. save history grade
        if (data == null) {
            saveStringToFile(HistoryDataFileName, "{}")
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
            if (count != 0)
                gpaInfo.put("grade", pointSum / count.toDouble())
            else
                gpaInfo.put("grade", 0.0)
            gradeInfo.put(gpaInfo)

            // 3. read history grade from file
            val historyData = JSONObject(readStringFromFile(HistoryDataFileName) ?: "{}")
            // {"2017-06-20": [{"name":"...","grade":"80"}, ...], ...}

            // 4. update history grade
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date())
            historyData.put(date, gradeInfo)

            // 5. save history grade
            saveStringToFile(HistoryDataFileName, historyData.toString())
        }
    }

    fun readHistoryGrade() = JSONObject(readStringFromFile(HistoryDataFileName) ?: "{}")

    fun buildNetworkRequest(url: String, method: String, body: MultipartBody?): Call {
        val request = Request.Builder()
                .url(url)
                .method(method, body)
                .header("User-Agent", "SchoolPower Android")
                .build()

        val client = OkHttpClient.Builder().build()
        return client.newCall(request)
    }

    fun checkApplicationUpdate() {
        buildNetworkRequest(context.getString(R.string.updateURL), "GET", null)
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val message = response.body()!!.string()
                        response.close()
                        if (!message.contains("{")) return
                        val updateJSON = JSONObject(message)
                        setSharedPreference(OtherData, "app_download_url", updateJSON.getString("url"))
                        if (updateJSON.getString("version") != getAppVersion()) {
                            (context as Activity).runOnUiThread {
                                val builder = AlertDialog.Builder(context)
                                builder.setTitle(context.getString(R.string.upgrade_title))
                                builder.setMessage(updateJSON.getString("description"))
                                builder.setPositiveButton(context.getString(R.string.upgrade_pos)) { dialog, _ ->
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
                    }
                })
    }

    fun getFilteredSubjects(subjects: List<Subject>): List<Subject> {

        val filteredSubjects: List<Subject>
        if (!PreferenceManager.getDefaultSharedPreferences(context.applicationContext).getBoolean("list_preference_dashboard_show_inactive", false)) {

            filteredSubjects = ArrayList()
            subjects
                    .filter {
                        val currentTime = System.currentTimeMillis()
                        currentTime > it.startDate && currentTime < it.endDate
                    }
                    .forEach { filteredSubjects.add(it) }

        } else {
            filteredSubjects = subjects
        }
        return filteredSubjects
    }

    fun getActionBarSizePx(): Int {
        val styledAttributes = context.theme.obtainStyledAttributes(
                intArrayOf(android.R.attr.actionBarSize))
        val actionBarSize = styledAttributes.getDimension(0, 0f).toInt()
        styledAttributes.recycle()
        return actionBarSize
    }

    private fun checkConnectionToUrl(url: String): Boolean {
        try {
            buildNetworkRequest(url, "GET", null).execute()
            return true
        } catch (e: IOException) {
            return false
        }
    }

    // note that the "https://host/api/" part is not included
    private fun getRouteFromString(route: String): String? {
        return when (route) {
            "pull_data_2" -> "2.0/get_data.php"
            "update" -> "update.json"
            "set_avatar" -> "2.0/set_avatar.php"
            else -> null
        }
    }

    // Do NOT call in the main thread
    // Return null when none found
    // See getRouteFromString for valid routes
    fun getBackupServerUrl(route: String): String? {
        val serversStr = try {
            buildNetworkRequest(context.getString(R.string.backupServersURL), "GET", null).execute().body()?.string()
                    ?: return null
        } catch (e: IOException) {
            return null
        } // return null when we can't even fetch server list. usually it's because of network issue

        val servers = try {
            JSONArray(serversStr)
        } catch (e: JSONException) {
            return null
        }
        for (i in 0 until servers.length()) {
            val server = servers.getJSONObject(i)
            if (server.has(route) && checkConnectionToUrl(server.getString(route))) // find full match first
                return server.getString(route)
            else if (server.has("generic")) { // if failed, try "generic" url
                val url = server.getString("generic") + (getRouteFromString(route) ?: continue)
                if (checkConnectionToUrl(url)) return url
            }
        }
        return null
    }

    fun isBirthDay(): Boolean {
        val data = getSharedPreference(AccountData).getLong("dob", 0)
        if (data == 0L) return false
        val now = Calendar.getInstance()
        val dob = Calendar.getInstance()
        now.time = Date()
        dob.time = Date(data)
        if (isThisYearLeapYear()) {
            val feb29 = Calendar.getInstance()
            feb29.set(Calendar.MONTH, 2)
            feb29.set(Calendar.DAY_OF_MONTH, 29)
            if (isSameDayAndMonth(dob, feb29)) {
                val mar1 = Calendar.getInstance()
                mar1.set(Calendar.MONTH, 3)
                mar1.set(Calendar.DAY_OF_MONTH, 1)
                return isSameDayAndMonth(now, mar1)
            }
        }
        return isSameDayAndMonth(now, dob)
    }

    fun isSameDayAndMonth(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    fun isThisYearLeapYear(): Boolean {
        return Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_YEAR) > 365
    }

    fun getAge(withSuffix: Boolean): String {
        val dat = getSharedPreference(AccountData).getLong("dob", 0)
        if (dat == 0L) return ""
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = Date()
        cal2.time = Date(dat)
        val ageStr = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR)
        return ageStr.toString() + if (withSuffix) getSuffixForNumber(ageStr) else ""
    }

    fun getSuffixForNumber(num: Int): String {
        return when (num % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }

    companion object {

        const val SettingsPreference: String = "Settings"
        const val AccountData: String = "accountData"
        const val OtherData: String = "other"

        const val StudentDataFileName: String = "dataMap.json"
        const val HistoryDataFileName: String = "history.json"
        const val StatisticalDataFileName: String = "statistics.json"

        val chartColorList = arrayOf(
                "#534550",
                "#c0de32",
                "#904cdb",
                "#76ca52",
                "#c953b8",
                "#67bc84",
                "#5d4da6",
                "#c3a83f",
                "#858fbf",
                "#d7552d",
                "#65bebe",
                "#b8517b",
                "#919652",
                "#ad5844",
                "#a49a85"
        )

        // convert date like "2018-01-21T16:00:00.000Z" to timestamp (unit: MILLISECOND)
        fun convertDateToTimestamp(date: String): Long {
            try {
                val temp = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(date.replace("T16:00:00.000Z", ""))

                temp.time += 24 * 60 * 60 * 1000
                return temp.time
            }catch(e: Exception){return 0}
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
                for (c in subjectTitle) {
                    if (c.isDigit()) short += c
                }
                return short
            }


            if (subjectTitle.length > 3) {
                var ret = subjectTitle.substring(0, 3).capitalize()
                for (c in subjectTitle.substring(3)) {
                    if (c.isUpperCase() || c.isDigit()) ret += c
                }
                return ret
            } else {
                return subjectTitle
            }
        }

    }
}
