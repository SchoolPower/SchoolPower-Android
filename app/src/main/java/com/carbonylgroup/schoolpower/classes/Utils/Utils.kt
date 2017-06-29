/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.classes.Utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Message
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.util.Base64
import android.util.DisplayMetrics
import android.view.View
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.classes.ListItems.AssignmentItem
import com.carbonylgroup.schoolpower.classes.ListItems.Subject
import com.carbonylgroup.schoolpower.classes.ListItems.Period
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.URL
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher


class Utils(private val context: Context) {

    private val gradeColorIds = intArrayOf(R.color.A_score_green, R.color.B_score_green, R.color.Cp_score_yellow, R.color.C_score_orange, R.color.Cm_score_red, R.color.primary_dark, R.color.primary, R.color.primary)
    private val gradeColorIdsPlain = intArrayOf(R.color.A_score_green, R.color.B_score_green, R.color.Cp_score_yellow, R.color.C_score_orange, R.color.Cm_score_red, R.color.primary_dark, R.color.primary)
    private val gradeDarkColorIdsPlain = intArrayOf(R.color.A_score_green_dark, R.color.B_score_green_dark, R.color.Cp_score_yellow_dark, R.color.C_score_orange_dark, R.color.Cm_score_red_dark, R.color.primary_darker, R.color.primary_dark)
    private val localeSet = arrayListOf(Locale.getDefault(), Locale.ENGLISH, Locale.TRADITIONAL_CHINESE, Locale.SIMPLIFIED_CHINESE)
    private fun indexOfString(searchString: String, domain: Array<String>): Int = domain.indices.firstOrNull { searchString == domain[it] } ?: -1

    /* Color Handler */
    fun getColorByLetterGrade(context: Context, letterGrade: String) = ContextCompat.getColor(context, gradeColorIds[indexOfString(letterGrade, arrayOf("A", "B", "C+", "C", "C-", "F", "I", "--"))])

    fun getColorByPeriodItem(context: Context, item: Period) = getColorByLetterGrade(context, item.termLetterGrade)

    fun getDarkColorByPrimary(originalPrimary: Int) = ContextCompat.getColor(context, gradeDarkColorIdsPlain[gradeColorIdsPlain.takeWhile { originalPrimary != ContextCompat.getColor(context, it) }.count()])

    /* Others */
    fun dpToPx(dp: Int) = Math.round(dp * (context.resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))

    fun getLatestItem(item: Subject): Period? {

        var forLatestSemester = false
        val latestTerm: String
        val periodGradeItemList = item.periodArrayList
        val termsList: ArrayList<String> = ArrayList()
        termsList.add(context.getString(R.string.all_terms))

        if (getSettingsPreference(context.getString(R.string.list_preference_dashboard_display)) == "1") forLatestSemester = true
        periodGradeItemList.indices.forEach { termsList.add(periodGradeItemList[it].termIndicator) }

        if (forLatestSemester)
            if (termsList.contains("S2")) latestTerm = "S2"
            else if (termsList.contains("S1")) latestTerm = "S1"
            else if (termsList.contains("T4")) latestTerm = "T4"
            else if (termsList.contains("T3")) latestTerm = "T3"
            else if (termsList.contains("T2")) latestTerm = "T2"
            else latestTerm = "T1"
        else
            if (termsList.contains("T4")) latestTerm = "T4"
            else if (termsList.contains("T3")) latestTerm = "T3"
            else if (termsList.contains("T2")) latestTerm = "T2"
            else latestTerm = "T1"

        periodGradeItemList.forEach { if (it.termIndicator == latestTerm) return it }
        return null
    }

    fun showSnackBar(context: Context, view: View, msg: String, colorRed: Boolean) {

        val snackBar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT)
        if (colorRed) snackBar.view.setBackgroundColor(context.resources.getColor(R.color.Cm_score_red_dark))
        else snackBar.view.setBackgroundColor(context.resources.getColor(R.color.accent))
        snackBar.show()
    }

    fun parseJsonResult(jsonStr: String): ArrayList<Subject>? {
        try {
            val jsonData = JSONArray(jsonStr)
            val dataMap = HashMap<String, Subject>()

            for (i in 0..jsonData.length() - 1) {

                val termObj = jsonData.getJSONObject(i)
                // Turns assignments into an ArrayList
                val assignmentList = ArrayList<AssignmentItem>()
                if(!termObj.has("assignments")) continue;
                val asmArray = termObj.getJSONArray("assignments")
                for (j in 0..asmArray.length() - 1) {
                    val asmObj = asmArray.getJSONObject(j)
                    val dates = asmObj.getString("date").split("/")
                    val date = dates[2] + "/" + dates[0] + "/" + dates[1]
                    assignmentList.add(AssignmentItem(asmObj.getString("assignment"),
                            date, if (asmObj.getString("grade") == "") "--" else asmObj.getString("percent"),
                            if (asmObj.getString("score").endsWith("d")) context.getString(R.string.unpublished) else asmObj.getString("score"),
                            if (asmObj.getString("grade") == "") "--" else asmObj.getString("grade"), asmObj.getString("category"), termObj.getString("term")))
                }

                val periodGradeItem = Period(termObj.getString("term"),
                        if (termObj.getString("grade") == "") "--" else termObj.getString("grade"), termObj.getString("mark"), assignmentList)

                // Put the term data into the course data, either already exists or be going to be created.
                val mainListItem = dataMap[termObj.getString("name")]
                if (mainListItem == null) { // The course data does not exist yet.

                    val periodGradeList = ArrayList<Period>()
                    periodGradeList.add(periodGradeItem)

                    dataMap.put(termObj.getString("name"),
                            Subject(termObj.getString("name"), termObj.getString("teacher"),
                                    termObj.getString("block"), termObj.getString("room"), periodGradeList))

                } else { // Already exist. Just insert into it.
                    mainListItem.addPeriodGradeItem(periodGradeItem)
                }
            }

            // Convert from HashMap to ArrayList
            val dataList = ArrayList<Subject>()
            dataList.addAll(dataMap.values)
            Collections.sort(dataList, Comparator<Subject> { o1, o2 ->
                if (o1.blockLetter == "HR(1)") return@Comparator -1
                if (o2.blockLetter == "HR(1)") return@Comparator 1
                o1.blockLetter.compareTo(o2.blockLetter)
            })
            return dataList

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return null
    }

    /* IO */
    fun getSettingsPreference(key: String) = context.getSharedPreferences(context.getString(R.string.settings), Activity.MODE_PRIVATE).getString(key, "0")!!

    fun readFileFromString(fileName : String) : String?{
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
        }catch(e: Exception){
            e.printStackTrace()
            return null
        }

    }

    fun getLocaleWithIndex(index: Int): Locale = localeSet[index]

    @Throws(IOException::class)
    fun saveStringToFile(fileName:String, data: String) {
        val outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
        outputStream.write(data.toByteArray())
        outputStream.close()
    }

    @Throws(IOException::class)
    fun readDataArrayList(): ArrayList<Subject>?
            = parseJsonResult(readFileFromString(context.getString(R.string.dataFileName))!!)

    @Throws(IOException::class)
    fun saveDataJson(jsonStr: String)
            = saveStringToFile(context.getString(R.string.dataFileName),jsonStr)

    // 1. read data into brief info
    // 2. calculate gpa
    // 3. read history grade from file
    // 4. update history grade
    // 5. save history grade
    fun saveHistoryGrade(data: ArrayList<Subject>){
        // 1. read data into brief info
        var pointSum = 0
        var count = 0
        val gradeInfo = JSONArray() // [{"name":"...","grade":80.0}, ...]
        for(subject in data){
            val subInfo = JSONObject()
            val leastPeriod = getLatestItem(subject) ?: continue
            subInfo.put("name", subject.subjectTitle)
            subInfo.put("grade", leastPeriod.termPercentageGrade.toDouble())
            if(!subject.subjectTitle.contains("Homeroom")) {
                pointSum += leastPeriod.termPercentageGrade.toInt()
                count += 1
            }
            gradeInfo.put(subInfo)
        }

        // 2. calculate gpa
        val gpaInfo = JSONObject()
        gpaInfo.put("name", "GPA")
        gpaInfo.put("grade", pointSum/count.toDouble())
        gradeInfo.put(gpaInfo)

        // 3. read history grade from file
        val historyData = JSONObject(readFileFromString("history.json")?:"{}")
        // {"2017-06-20": [{"name":"...","grade":"80"}, ...], ...}

        // 4. update history grade
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date())
        historyData.put(date, gradeInfo)

        // 5. save history grade
        saveStringToFile("history.json", historyData.toString())
    }

    fun readHistoryGrade() = JSONObject(readFileFromString("history.json")?:"{}")

    fun checkUpdate() {
        Thread(postData(context.getString(R.string.updateURL), "", object : Handler() {
            override fun handleMessage(msg: Message) {
                val message = msg.obj.toString()
                if (!message.contains("{")) return;
                val updateJSON = JSONObject(message)
                if (updateJSON.getString("version") != context.packageManager.getPackageInfo("com.carbonylgroup.schoolpower", 0).versionName) {
                    val builder = AlertDialog.Builder(context);
                    builder.setTitle("New upgrade is available!")
                    builder.setMessage(updateJSON.getString("description"))
                    builder.setPositiveButton("Upgrade") {
                        dialog, _ ->
                        run {
                            dialog.dismiss()
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateJSON.getString("url")))
                            context.startActivity(intent)
                        }
                    }
                    builder.setNegativeButton("Later", null)
                    builder.create().show()
                }
            }
        })).start()
    }

    companion object {

        private val ALGORITHM = "RSA/ECB/PKCS1Padding"
        private val RSA = "RSA"

        fun getShortName(subjectTitle:String) : String{
            val shorts= mapOf("Homeroom" to "HR", "Planning" to "PL", "Mandarin" to "CN",
                    "Chinese" to "CSS", "Foundations" to "Maths", "Physical" to "PE",
                    "English" to "EN", "Moral" to "ME")
            val short=shorts[subjectTitle.split(" ")[0]]
            if(short!=null) return short

            var ret=""
            for(c in subjectTitle){
                if(c.isUpperCase()||c.isDigit()) ret+=c
            }
            return ret
        }

        fun restorePublicKey(key: String): PublicKey =
                KeyFactory.getInstance(RSA).generatePublic(X509EncodedKeySpec(Base64.decode(key, Base64.DEFAULT)))

        fun RSAEncode(key: PublicKey, plainText: String): String {

            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            return Base64.encodeToString(cipher.doFinal(plainText.toByteArray()), Base64.URL_SAFE)

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