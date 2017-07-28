/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.fragments

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.preference.ListPreference
import android.preference.PreferenceFragment
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.MainActivity
import android.content.Intent
import com.carbonylgroup.schoolpower.classes.Utils.Utils

class SettingsFragment : PreferenceFragment(), OnSharedPreferenceChangeListener {

    private var utils: Utils? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        MainActivity.of(activity).expandToolBar(true, true)
        addPreferencesFromResource(R.xml.preferences_content)
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        utils = Utils(activity)
        initPreferences()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (isAdded) refreshPreferences(sharedPreferences, key)
    }

    private fun initPreferences() {

        val dashboard_display = (findPreference(activity.getString(R.string.list_preference_dashboard_display)) as ListPreference)
        dashboard_display.summary = activity.getString(R.string.dashboard_display_preference_summary_prefix) + dashboard_display.entry + activity.getString(R.string.dashboard_display_preference_summary_suffix)
    }

    private fun refreshPreferences(sharedPreferences: SharedPreferences?, key: String?) {

        if (key == getString(R.string.list_preference_dashboard_display)) {

            val dashboard_display = (findPreference(getString(R.string.list_preference_dashboard_display)) as ListPreference)
            dashboard_display.summary = getString(R.string.dashboard_display_preference_summary_prefix) + dashboard_display.entry + activity.getString(R.string.dashboard_display_preference_summary_suffix)
            utils!!.setSettingsPreference(key!!, sharedPreferences!!.getString(key, "0"))

        }

        if (key == getString(R.string.list_preference_language)) {

            utils!!.saveLangPref(sharedPreferences!!.getString(key, "0"))
            restart()
        }
    }

    private fun restart() {

        val refresh = Intent(activity, activity.javaClass)
        startActivity(refresh)
        activity.finish()
    }
}
