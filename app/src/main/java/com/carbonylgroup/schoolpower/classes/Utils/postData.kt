/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.classes.Utils

import android.os.Handler
import android.os.Message


class postData(private val url: String, private val params: String, private val h: Handler) : Runnable {

    override fun run() {
        val m = Message()
        m.obj = Utils.sendPost(url, params)
        h.sendMessage(m)
    }
}
