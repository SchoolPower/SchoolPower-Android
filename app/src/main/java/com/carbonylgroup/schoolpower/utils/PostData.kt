/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.utils

import android.os.Handler
import android.os.Message
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.URL


class PostData(private val url: String, private val params: String, private val handler: Handler) : Runnable {

    /**
     * @param url    url
     * *
     * @param params name1=value1&name2=value2
     * *
     * @return result
     */
    private fun sendPost(url: String, params: String): String {

        var out: PrintWriter? = null
        var `in`: BufferedReader? = null
        var result = ""
        try {

            val realUrl = URL(url)
            val conn = realUrl.openConnection()
            conn.setRequestProperty("user-agent", "SchoolPower Android")
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

    override fun run() {
        val message = Message()
        message.obj = sendPost(url, params)
        handler.sendMessage(message)
    }
}
