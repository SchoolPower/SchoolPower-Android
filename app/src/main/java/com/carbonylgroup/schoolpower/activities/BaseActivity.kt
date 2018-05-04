package com.carbonylgroup.schoolpower.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.support.annotation.CallSuper
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.carbonylgroup.schoolpower.utils.ThemeHelper

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("[][][", "Notification Sent (Subjects)")
        ThemeHelper(this).apply()
        setSupportActionBar(null)
        super.onCreate(savedInstanceState)
        initActivity()
    }

    override fun onResume() {
        super.onResume()
    }

    @CallSuper
    protected open fun initActivity() {
    }
}
