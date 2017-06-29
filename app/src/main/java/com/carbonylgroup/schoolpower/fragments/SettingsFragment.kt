/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.fragments

import android.app.Activity
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.preference.ListPreference
import android.preference.PreferenceFragment
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.MainActivity
import android.content.Intent
import java.util.Locale

class SettingsFragment : PreferenceFragment(), OnSharedPreferenceChangeListener {

    private val localeSet = arrayListOf(Locale.getDefault(), Locale.ENGLISH, Locale.TRADITIONAL_CHINESE, Locale.SIMPLIFIED_CHINESE)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        MainActivity.of(activity).expandToolBar(true, true)
        addPreferencesFromResource(R.xml.preferences_content)
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
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
            dashboard_display.summary = getString(R.string.dashboard_display_preference_summary_prefix) + " " + dashboard_display.entry + activity.getString(R.string.dashboard_display_preference_summary_suffix)
            val spEditor = activity.getSharedPreferences(getString(R.string.settings), Activity.MODE_PRIVATE).edit()
            spEditor.putString(getString(R.string.list_preference_dashboard_display), sharedPreferences!!.getString(key, "0"))
            spEditor.apply()
        }

        if (key == getString(R.string.list_preference_language)) {

            val selected = sharedPreferences!!.getString(key, "0").toInt()
            val spEditor = activity.getSharedPreferences(getString(R.string.settings), Activity.MODE_PRIVATE).edit()
            spEditor.putString(getString(R.string.list_preference_language), selected.toString())
            spEditor.apply()
            setLocale(selected)
        }
    }

    private fun setLocale(index: Int) {

        val newLocale = localeSet[index]
        val res = resources
        val dm = res.displayMetrics
        val conf = res.configuration
        conf.locale = newLocale
        res.updateConfiguration(conf, dm)
        val refresh = Intent(activity, activity.javaClass)
        startActivity(refresh)
        activity.finish()
    }
}
