package com.carbonylgroup.schoolpower.activities

import android.content.Context
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import com.carbonylgroup.schoolpower.utils.ContextWrapper
import com.carbonylgroup.schoolpower.utils.ThemeHelper
import com.carbonylgroup.schoolpower.utils.Utils
import com.google.firebase.analytics.FirebaseAnalytics

abstract class BaseActivity : AppCompatActivity() {

    companion object {
        private lateinit var curActivity: BaseActivity
        private var crashReportInitialized = false
        fun getCurActivity(): BaseActivity {
            return curActivity
        }
    }
    lateinit var firebaseAnalytics: FirebaseAnalytics
    lateinit var utils: Utils

    override fun attachBaseContext(newBase: Context?) {
        val utils = Utils(newBase!!)
        val newLocale = utils.getPreferences().getString("list_preference_language", "0")!!.toInt()
        val context = ContextWrapper.wrap(newBase, Utils.getLocaleSet()[newLocale])
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        utils = Utils(this)
        if(!crashReportInitialized){
            utils.crashReportRequest()
            crashReportInitialized = true
        }
        utils.analyticsRequest()
        if (utils.getPreferences().getBoolean("analytics_enabled", false)) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(this)
            firebaseAnalytics.setUserProperty("developer_mode", utils.isDeveloperMode().toString())
            firebaseAnalytics.setUserProperty("theme", utils.getTheme())
            firebaseAnalytics.setUserProperty("is_donated", utils.isDonated().toString())
            firebaseAnalytics.setUserProperty("accent_color", utils.getAccentColorIndex().toString())
            firebaseAnalytics.setUserProperty("crash_report_enabled", utils.isCrashReportEnabled().toString())
            firebaseAnalytics.setUserProperty("for_latest_semester", utils.getPreferences()
                    .getString("list_preference_dashboard_display", "0"))
            firebaseAnalytics.setUserProperty("enable_advertisement", utils.getPreferences()
                    .getBoolean("preference_enable_advertisement", true).toString())
            firebaseAnalytics.setUserProperty("smart_block", utils.getPreferences()
                    .getBoolean("list_preference_even_odd_filter", false).toString())
            firebaseAnalytics.setUserProperty("show_inactive", utils.getPreferences()
                    .getBoolean("list_preference_dashboard_show_inactive", true).toString())
            val langCode = utils.getPreferences().getString("list_preference_language", "0")!!
            firebaseAnalytics.setUserProperty("language", langCode)
            firebaseAnalytics.setUserProperty("actual_language", utils.localeSet[langCode.toInt()].toString())
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
