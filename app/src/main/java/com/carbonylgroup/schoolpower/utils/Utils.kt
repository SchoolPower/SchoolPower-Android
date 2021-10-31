/**
 * Copyright (C) 2019 SchoolPower Studio
 */

package com.carbonylgroup.schoolpower.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.support.annotation.ColorInt
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.view.View
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.data.Grade
import com.carbonylgroup.schoolpower.data.SortableTerm
import com.carbonylgroup.schoolpower.data.StudentData
import com.carbonylgroup.schoolpower.data.Subject
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class Utils(private val context: Context) {

    val localeSet = arrayListOf(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales[0]
        } else {
            Resources.getSystem().configuration.locale
        },
        Locale.ENGLISH,
        Locale.TRADITIONAL_CHINESE,
        Locale.SIMPLIFIED_CHINESE,
        Locale.JAPANESE
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

    private fun indexOfString(searchString: String, domain: Array<String>):
            Int = domain.indices.firstOrNull { searchString == domain[it] } ?: -1

    fun getTheme(): String {
        return getPreferences().getString(THEME, LIGHT)!!
    }

    fun getAccentColorIndex() = getPreferences().getInt(
        ACCENT_COLOR,
        ThemeHelper(context).lightArray.indexOf(R.style.ThemeLight_Cyan_500)
    )

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

    /* Color Handler */
    fun getColorByLetterGrade(letterGrade: String): Int {
        val colorIndex =
            indexOfString(letterGrade, arrayOf("A", "B", "C+", "C", "C-", "F", "I", "--"))
        return ContextCompat.getColor(
            context,
            gradeColorIds[if (colorIndex != -1) colorIndex else 7]
        )
    }

    fun getDarkColorByPrimary(originalPrimary: Int) = ContextCompat.getColor(context,
        gradeDarkColorIdsPlain[gradeColorIdsPlain.takeWhile {
            originalPrimary != ContextCompat.getColor(
                context,
                it
            )
        }.count()]
    )

    fun getColorByAttendance(context: Context, attendanceCode: String) = ContextCompat.getColor(
        context,
        attendanceColorIds[attendanceCode] ?: R.color.gray
    )

    /* Others */
    fun isDeveloperMode(): Boolean {
        return getPreferences(TmpData).getBoolean("developer_mode", false)
    }

    fun dpToPx(dp: Int) =
        Math.round(dp * (context.resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))


    fun getAllPeriods(subjects: List<Subject>): HashSet<String> {
        val allPeriods = HashSet<String>()
        subjects.indices.forEach { i ->
            subjects[i].grades.keys.filterTo(allPeriods) { subjects[i].grades[it]!!.hasGrade() }
        }
        return allPeriods
    }

    fun getLatestTermNameOverall(subjects: List<Subject>): String? {
        val latestPeriods = mutableMapOf<String, Grade>()

        subjects.forEach {
            if (getLatestTermName(it.grades) == null) {
                return@forEach
            }
            val key = getLatestTermName(it.grades)!!
            latestPeriods[key] = it.grades[key]!!
        }

        if (latestPeriods.isEmpty()) return null

        // overall latest period, usually indicates the current term
        return getLatestTermName(latestPeriods)
    }

    fun getLatestTermName(grades: Map<String, Grade>): String? {
        return sortTermsByLatest(grades.keys)
            .firstOrNull { grades[it]?.hasGrade() == true }
    }

    fun getLatestTermGrade(subject: Subject) = subject.grades[getLatestTermName(subject.grades)]

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
        if (colorRed) snackBar.view.setBackgroundColor(
            ContextCompat.getColor(
                context,
                R.color.Cm_score_red_dark
            )
        )
        else snackBar.view.setBackgroundColor(ContextCompat.getColor(context, R.color.accent))
        snackBar.show()
    }

    fun getAppVersion() =
        context.packageManager.getPackageInfo("com.carbonylgroup.schoolpower", 0).versionName!!

    fun getPreferences(database: String = ""): SharedPreferences =
        if (database != "") context.getSharedPreferences(database, Activity.MODE_PRIVATE)
        else PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    fun setPreference(key: String, value: Any, database: String = "") {
        if (key == "") {
            throw NullPointerException(
                String.format(
                    "Key and value not be null key=%s, value=%s",
                    key,
                    value
                )
            )
        }
        val edit = getPreferences(database).edit()

        when (value) {
            is String -> edit.putString(key, value)
            is Int -> edit.putInt(key, value)
            is Long -> edit.putLong(key, value)
            is Boolean -> edit.putBoolean(key, value)
            is Float -> edit.putFloat(key, value)
            else -> throw IllegalArgumentException(
                String.format(
                    "Type of value unsupported key=%s, value=%s",
                    key,
                    value
                )
            )
        }
        edit.apply()
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
    fun readDataArrayList() = StudentData(context, readStringFromFile(StudentDataFileName)!!, this)

    @Throws(IOException::class)
    fun saveDataJson(jsonStr: String) = saveStringToFile(StudentDataFileName, jsonStr)

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
                val leastPeriod = getLatestTermGrade(subject) ?: continue

                subInfo.put("name", subject.name)
                subInfo.put("grade", leastPeriod.getGrade() ?: continue)
                if (!subject.name.contains("Homeroom")) {
                    pointSum += leastPeriod.getGrade()!!
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

            val historyData = try {
                JSONObject(readStringFromFile(HistoryDataFileName))
            } catch (e: Exception) {
                e.printStackTrace()
                JSONObject("{}")
            }
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
                    setPreference("app_download_url", updateJSON.getString("url"), TmpData)
                    if (updateJSON.getString("version") != getAppVersion()
                        && (!updateJSON.has("prompt") || updateJSON.getBoolean("prompt"))
                    ) {
                        if (!(context as Activity).isFinishing && !context.isDestroyed) {
                            context.runOnUiThread {
                                val builder = AlertDialog.Builder(context)
                                builder.setTitle(context.getString(R.string.upgrade_title))
                                builder.setMessage(updateJSON.getString("description"))
                                builder.setPositiveButton(context.getString(R.string.upgrade_pos)) { dialog, _ ->
                                    run {
                                        dialog.dismiss()
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(updateJSON.getString("url"))
                                        )
                                        context.startActivity(intent)
                                    }
                                }
                                builder.setNegativeButton(
                                    context.getString(R.string.upgrade_neg),
                                    null
                                )
                                builder.create().show()
                            }
                        }
                    }
                }
            })
    }

    fun getGradedSubjects(subjects: List<Subject>): List<Subject> {
        val gradedSubjects = ArrayList<Subject>() // Subjects that have grades

        for (subjectNow in subjects) {

            if (!getPreferences().getBoolean("list_preference_dashboard_show_inactive", true)) {
                val currentTime = System.currentTimeMillis()
                val it = subjects.find { it.name == subjectNow.name }
                    ?: continue
                if (currentTime < it.startDate || currentTime > it.endDate) continue
            }
            val grade = getLatestTermGrade(subjectNow)
            if (grade?.hasGrade() == true) gradedSubjects.add(subjectNow)
        }
        return gradedSubjects
    }

    fun getFilteredSubjects(subjects: List<Subject>): List<Subject> {
        val filteredSubjects: List<Subject>
        if (!getPreferences().getBoolean("list_preference_dashboard_show_inactive", true)) {

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
            intArrayOf(android.R.attr.actionBarSize)
        )
        val actionBarSize = styledAttributes.getDimension(0, 0f).toInt()
        styledAttributes.recycle()
        return actionBarSize
    }

    fun getLastDonateShowedDate(): Date {
        val dateStr = getPreferences(Utils.TmpData)
            .getString("LastTimeDonateShowed", "")

        return if (dateStr != "") SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.CHINA
        ).parse(dateStr) else Date(0)
    }

    fun isEarlyDonators(): Boolean {
        val start = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse("2018-07-01")
        val end = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse("2018-08-28")
        val shown = getLastDonateShowedDate()
        return start <= shown && shown <= end
    }

    fun isDonated(): Boolean {
        return getPreferences(Utils.TmpData).getBoolean("Donated", false)
    }

    private fun checkConnectionToUrl(url: String): Boolean {
        return try {
            buildNetworkRequest(url, "GET", null).execute()
            true
        } catch (e: IOException) {
            false
        }
    }

    // Do NOT call in the main thread
    // Return null when none found
    // See getRouteFromString for valid routes
    fun getBackupServerUrl(route: String): String? {
        val serversStr = try {
            buildNetworkRequest(context.getString(R.string.backupServersURL), "GET", null).execute()
                .body()?.string()
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
        val data = getPreferences(AccountData).getLong("dob", 0)
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

    fun getAge(withSuffix: Boolean): String {
        val dat = getPreferences(AccountData).getLong("dob", 0)
        if (dat == 0L) return ""
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = Date()
        cal2.time = Date(dat)
        val ageStr = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR)
        return ageStr.toString() + if (withSuffix) getSuffixForNumber(ageStr) else ""
    }

    fun errorHandler(e: Exception, customTitle: String? = null, customContent: String? = null) {
        val crashReportEnabled = getPreferences().getBoolean("crash_report_enabled", false)
        if (crashReportEnabled) {
            Crashlytics.logException(e)
        }

        val sw = StringWriter()
        e.printStackTrace(PrintWriter(sw))
        if (context !is Activity) return
        if (!context.isFinishing && !context.isDestroyed)
            context.runOnUiThread {
                val emergencyDialogBuilder = AlertDialog.Builder(context)
                emergencyDialogBuilder.setTitle(
                    customTitle ?: context.getString(R.string.fatel_error)
                )
                emergencyDialogBuilder.setMessage(
                    customContent ?: context.getString(R.string.fatel_error_message) + sw.toString()
                )
                val sendEmail = DialogInterface.OnClickListener { _, _ ->
                    val version = context.packageManager.getPackageInfo(
                        "com.carbonylgroup.schoolpower",
                        0
                    ).versionName
                    val uri = Uri.parse(context.getString(R.string.bug_report_email))
                    val intent = Intent(Intent.ACTION_SENDTO, uri)
                    intent.putExtra(
                        Intent.EXTRA_SUBJECT,
                        context.getString(R.string.bug_report_email_subject)
                    )
                    intent.putExtra(
                        Intent.EXTRA_TEXT,
                        String.format(
                            context.getString(R.string.bug_report_email_content),
                            version
                        ) +
                                "\n\nError message: \n" + (customContent ?: sw.toString())
                    )
                    context.startActivity(
                        Intent.createChooser(
                            intent,
                            context.getString(R.string.choose_email_app)
                        )
                    )
                }
                emergencyDialogBuilder.setNegativeButton(context.getString(R.string.cancel), null)
                if (!crashReportEnabled) {
                    emergencyDialogBuilder.setNeutralButton(
                        context.getString(R.string.email),
                        sendEmail
                    )

                    emergencyDialogBuilder.setPositiveButton(context.getString(R.string.send_crash)) { _, _ ->
                        Fabric.with(context, Crashlytics())
                        Crashlytics.logException(e)
                        Crashlytics.getInstance().core.logException(e)
                    }
                } else {
                    emergencyDialogBuilder.setPositiveButton(
                        context.getString(R.string.email),
                        sendEmail
                    )
                }
                emergencyDialogBuilder.create().setCanceledOnTouchOutside(false)
                emergencyDialogBuilder.create().show()
            }
    }

    fun isCrashReportEnabled() = getPreferences().getBoolean("crash_report_enabled", false)

    fun crashReportRequest() {
        if (isCrashReportEnabled()) {
            Fabric.with(context, Crashlytics())
            return
        }

        if (getPreferences(Utils.TmpData).getBoolean("crash_report_requested", false))
            return
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.crash_report_enable))
        builder.setMessage(context.getString(R.string.crash_report_enable_summary))
        builder.setPositiveButton(context.getString(R.string.accept)) { _, _ ->
            setPreference("crash_report_enabled", true)
            Fabric.with(context, Crashlytics())
        }
        builder.setNegativeButton(context.getString(R.string.decline), null)
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        setPreference("crash_report_requested", true, Utils.TmpData)
    }

    fun analyticsRequest() {
        if (getPreferences(Utils.TmpData).getBoolean("analytics_requested", false))
            return
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.analytics_enable))
        builder.setMessage(context.getString(R.string.analytics_enable_summary))
        builder.setPositiveButton(context.getString(R.string.accept)) { _, _ ->
            setPreference("analytics_enabled", true)
        }
        builder.setNegativeButton(context.getString(R.string.decline), null)
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        setPreference("analytics_requested", true, Utils.TmpData)
    }

    fun autoAdjustWeekType() {
        val storeKey = "week_last_updated_date"
        val weekTypeStoreKey = "list_preference_is_even_week"

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        val lastUpdatedDate =
            try {
                sdf.parse(getPreferences(TmpData).getString(storeKey, ""))
            } catch (e: Exception) {
                Date()
            }
        setPreference(storeKey, sdf.format(Date()), TmpData)
        val lastUpdatedCal = Calendar.getInstance()
        lastUpdatedCal.time = lastUpdatedDate
        val currentCal = Calendar.getInstance()
        currentCal.time = Date()
        if (lastUpdatedCal.get(Calendar.WEEK_OF_YEAR) % 2 !=
            currentCal.get(Calendar.WEEK_OF_YEAR) % 2
        )
            setPreference(
                weekTypeStoreKey,
                !getPreferences().getBoolean(weekTypeStoreKey, false)
            )
    }

    companion object {

        const val THEME = "appTheme"
        const val ACCENT_COLOR = "accentColor"
        const val LIGHT = "LIGHT"
        const val DARK = "DARK"

        fun getLocaleSet(): ArrayList<Locale> = arrayListOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Resources.getSystem().configuration.locales[0]
            } else {
                Resources.getSystem().configuration.locale
            },
            Locale.ENGLISH,
            Locale.TRADITIONAL_CHINESE,
            Locale.SIMPLIFIED_CHINESE,
            Locale.JAPANESE
        )

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

        const val AccountData: String = "accountData"
        const val TmpData: String = "Tmp"
        const val CategoryWeightData: String = "category"

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
            "#a49a85",
            "#00695c",
            "#1de9b6",
            "#cddc39",
            "#e65100",
            "#ff3d00",
            "#37474f",
            "#cfd8dc",
            "#6d4c41",
            "#2e7d32",
            "#01579b"
        )

        // note that the "https://host/api/" part is not included
        private fun getRouteFromString(route: String): String? {
            return when (route) {
                "pull_data_2" -> "2.0/get_data.php"
                "update" -> "update.json"
                "set_avatar" -> "2.0/set_avatar.php"
                else -> null
            }
        }

        fun isSameDayAndMonth(cal1: Calendar, cal2: Calendar): Boolean {
            return cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                    cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
        }

        fun isThisYearLeapYear(): Boolean {
            return Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_YEAR) > 365
        }

        private fun getSuffixForNumber(num: Int): String {
            return when (num % 10) {
                1 -> "st"
                2 -> "nd"
                3 -> "rd"
                else -> "th"
            }
        }

        // convert date like "2018-01-21T16:00:00.000Z" to timestamp (unit: MILLISECOND)
        fun convertDateToTimestamp(date: String): Long {
            return try {
                val temp = SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.CHINA
                ).parse(date.replace("T16:00:00.000Z", ""))

                temp.time += 24 * 60 * 60 * 1000
                temp.time
            } catch (e: Exception) {
                0
            }
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
            val splitSubject = subjectTitle.split(" ")
            var short = shorts[splitSubject[0]]
            if (short != null) {
                if (splitSubject[splitSubject.size - 1] == "Music") short += "M"
                if (splitSubject[splitSubject.size - 1] == "Politics") short += "P"
                if (splitSubject[splitSubject.size - 1] == "Sci") short += "S"
                if (splitSubject[splitSubject.size - 1] == "Humanities") short += "H"
                if (splitSubject[splitSubject.size - 1] == "Arts") short += "A"
                for (c in subjectTitle) {
                    if (c.isDigit()) short += c
                }
                return short
            }


            return if (subjectTitle.length > 3) {
                var ret = subjectTitle.substring(0, 3).capitalize()
                for (c in subjectTitle.substring(3)) {
                    if (c.isUpperCase() || c.isDigit()) ret += c
                }
                ret
            } else {
                subjectTitle
            }
        }

        fun sortTerm(terms: List<String>): List<String> {
            return terms
                .map { SortableTerm(it) }
                .sortedBy { it.letterValue }
                .sortedByDescending { it.index }
                .map { it.getRaw() }
        }

        fun sortTerm(terms: ArrayList<String>): List<String> {
            return terms
                .map { SortableTerm(it) }
                .sortedBy { it.letterValue }
                .sortedByDescending { it.index }
                .map { it.getRaw() }
        }

        fun sortTerm(terms: HashSet<String>): List<String> {
            return terms
                .map { SortableTerm(it) }
                .sortedBy { it.letterValue }
                .sortedByDescending { it.index }
                .map { it.getRaw() }
        }

        fun sortTermsByLatest(terms: Set<String>): List<String> {
            return terms
                .map { SortableTerm(it) }
                .sortedByDescending { it.index }
                .sortedBy { it.letterValue }
                .map { it.getRaw() }
        }

        fun Double.roundTo(n : Int) : Double {
            return "%.${n}f".format(this).toDouble()
        }
    }
}
