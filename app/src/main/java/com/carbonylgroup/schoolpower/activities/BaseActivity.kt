package com.carbonylgroup.schoolpower.activities

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.carbonylgroup.schoolpower.utils.ThemeHelper

abstract class BaseActivity : AppCompatActivity() {

    companion object {
        private lateinit var curActivity: BaseActivity
        fun getCurActivity(): BaseActivity {
            return curActivity
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("[][][", "Notification Sent (Subjects)")
        ThemeHelper(this).apply()
        setSupportActionBar(null)
        super.onCreate(savedInstanceState)
        initActivity()
    }

    override fun onResume() {
        super.onResume()
        curActivity = getActivity()
    }

    @CallSuper
    protected open fun initActivity() {
    }

    protected fun getActivity(): BaseActivity {
        return this
    }
}
