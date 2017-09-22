/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.utils

import android.os.Handler
import android.os.Message


class PostData(private val url: String, private val params: String, private val handler: Handler) : Runnable {

    override fun run() {
        val message = Message()
        message.obj = Utils.sendPost(url, params)
        handler.sendMessage(message)
    }
}
