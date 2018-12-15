package com.carbonylgroup.schoolpower.activities

import android.content.Context
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v7.app.AppCompatActivity
import com.carbonylgroup.schoolpower.utils.ContextWrapper
import com.carbonylgroup.schoolpower.utils.ThemeHelper
import com.carbonylgroup.schoolpower.utils.Utils

abstract class BaseActivity : AppCompatActivity() {

    companion object {
        private lateinit var curActivity: BaseActivity
        private var crashReportInitialized = false
        fun getCurActivity(): BaseActivity {
            return curActivity
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        val utils = Utils(newBase!!)
        val newLocale = utils.getPreferences().getString("list_preference_language", "0")!!.toInt()
        val context = ContextWrapper.wrap(newBase, Utils.localeSet[newLocale])
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if(!crashReportInitialized){
            Utils(this).crashReportRequest()
            crashReportInitialized = true
        }
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
