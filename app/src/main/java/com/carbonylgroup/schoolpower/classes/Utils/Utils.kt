/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.classes.Utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.support.v4.content.ContextCompat
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log

import org.apache.commons.io.output.ByteArrayOutputStream
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.URL
import java.net.URLConnection
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.HashMap

import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.classes.ListItems.AssignmentItem
import com.carbonylgroup.schoolpower.classes.ListItems.MainListItem
import com.carbonylgroup.schoolpower.classes.ListItems.PeriodGradeItem


class Utils(private val context: Context) {

    private val gradeColorIds = intArrayOf(R.color.A_score_green, R.color.B_score_green, R.color.Cp_score_yellow, R.color.C_score_orange, R.color.Cm_score_red, R.color.primary_dark, R.color.primary, R.color.primary)
    private val gradeColorIdsPlain = intArrayOf(R.color.A_score_green, R.color.B_score_green, R.color.Cp_score_yellow, R.color.C_score_orange, R.color.Cm_score_red, R.color.primary_dark, R.color.primary)
    private val gradeDarkColorIdsPlain = intArrayOf(R.color.A_score_green_dark, R.color.B_score_green_dark, R.color.Cp_score_yellow_dark, R.color.C_score_orange_dark, R.color.Cm_score_red_dark, R.color.primary_darker, R.color.primary_dark)

    private fun indexOfString(searchString: String, domain: Array<String>): Int =
            domain.indices.firstOrNull { searchString == domain[it] } ?: -1

    fun dpToPx(dp: Int) = Math.round(dp * (context.resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))

    /* Color Handler */
    fun getColorByLetterGrade(context: Context, letterGrade: String): Int {

        val letterGrades = arrayOf("A", "B", "C+", "C", "C-", "F", "I", "--")
        return ContextCompat.getColor(context, gradeColorIds[indexOfString(letterGrade, letterGrades)])
    }

    fun getColorByPeriodItem(context: Context, item: PeriodGradeItem) = getColorByLetterGrade(context, item.termLetterGrade)

    fun getDarkColorByPrimary(originalPrimary: Int): Int {

        val count = gradeColorIdsPlain
                .takeWhile { originalPrimary != ContextCompat.getColor(context, it) }
                .count()

        return ContextCompat.getColor(context, gradeDarkColorIdsPlain[count])
    }

    @Throws(IOException::class)
    fun readDataArrayList(): ArrayList<MainListItem>? {
        val data = StringBuilder("")
        val inputStream = context.openFileInput(context.getString(R.string.dataFileName))
        val isr = InputStreamReader(inputStream)
        val buffReader = BufferedReader(isr)

        var readString = buffReader.readLine()
        while (readString != null) {
            data.append(readString)
            readString = buffReader.readLine()
        }

        isr.close()
        inputStream.close()
        return parseJsonResult(data.toString())
    }

    @Throws(IOException::class)
    fun saveDataJson(jsonStr: String) {

        val outputStream = context.openFileOutput(context.getString(R.string.dataFileName), Context.MODE_PRIVATE)
        outputStream.write(jsonStr.toByteArray())
        outputStream.close()

    }

    fun parseJsonResult(jsonStr: String): ArrayList<MainListItem>? {
        try {
            val jsonData = JSONArray(jsonStr)
            val dataMap = HashMap<String, MainListItem>()

            for (i in 0..jsonData.length() - 1) {

                val termObj = jsonData.getJSONObject(i)

                // Turns assignments into an ArrayList
                val assignmentList = ArrayList<AssignmentItem>()
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

                val periodGradeItem = PeriodGradeItem(termObj.getString("term"),
                        if (termObj.getString("grade") == "") "--" else termObj.getString("grade"), termObj.getString("mark"), assignmentList)

                // Put the term data into the course data, either already exists or be going to be created.
                val mainListItem = dataMap[termObj.getString("name")]
                if (mainListItem == null) { // The course data does not exist yet.

                    val periodGradeList = ArrayList<PeriodGradeItem>()
                    periodGradeList.add(periodGradeItem)

                    dataMap.put(termObj.getString("name"),
                            MainListItem(termObj.getString("name"), termObj.getString("teacher"),
                                    termObj.getString("block"), termObj.getString("room"), periodGradeList))

                } else { // Already exist. Just insert into it.

                    mainListItem.addPeriodGradeItem(periodGradeItem)

                }
            }

            // Convert from HashMap to ArrayList
            val dataList = ArrayList<MainListItem>()
            dataList.addAll(dataMap.values)
            Collections.sort(dataList, Comparator<MainListItem> { o1, o2 ->
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

    companion object {

        private val ALGORITHM = "RSA/ECB/PKCS1Padding"
        private val RSA = "RSA"

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
                var line: String
                while (true){
                    line = `in`.readLine()
                    if(line==null) break
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