/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.classes.Utils;

import android.os.Handler;
import android.os.Message;


public class postData implements Runnable {

    private String url;
    private String params;
    private Handler h;

    public postData(String url, String params, Handler h) {
        this.url = url;
        this.params = params;
        this.h = h;
    }

    @Override
    public void run() {
        Message m = new Message();
        m.obj = Utils.sendPost(url, params);
        h.sendMessage(m);
    }
}
