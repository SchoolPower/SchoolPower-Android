package com.carbonylgroup.schoolpower.fragments

import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.v14.preference.MultiSelectListPreference
import android.support.v14.preference.PreferenceFragment
import android.support.v14.preference.SwitchPreference
import android.support.v7.app.AlertDialog
import android.support.v7.preference.ListPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceManager
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.MainActivity
import com.carbonylgroup.schoolpower.activities.SettingsActivity
import com.carbonylgroup.schoolpower.utils.Utils
import com.carbonylgroup.schoolpower.utils.colorChooser.ColorChooserPreference
import java.util.*

class SettingsFragment : PreferenceFragment(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        ColorChooserPreference.ColorChooserCallback{

    override fun onColorChanged(@ColorInt oriColor: Int, @ColorInt selectedColor: Int) {
        recreateMain()
    }

    interface SettingsCallBack {
        fun onRecreate()
    }
    private var callBack: SettingsCallBack? = null
    private var utils: Utils? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        callBack = context as SettingsCallBack
    }

    override fun onDetach() {
        super.onDetach()
        callBack = null
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences_content)
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        utils = Utils(activity)
        initPreferences()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (isAdded) refreshPreferences(sharedPreferences, key)
    }

    private fun initPreferences() {

        (findPreference("list_preference_accent_color") as ColorChooserPreference)
                .setColorChooserCallback(this)

        val dashboardDisplay = (findPreference("list_preference_dashboard_display") as ListPreference)
        dashboardDisplay.summary = activity.getString(R.string.dashboard_display_preference_summary_prefix) + dashboardDisplay.entry + activity.getString(R.string.dashboard_display_preference_summary_suffix)

        val gpaRule = findPreference("list_preference_custom_gpa_calculate") as ListPreference
        gpaRule.summary = getString(R.string.dashboard_gpa_rule_summary_prefix) + gpaRule.entry.toString().toLowerCase() + activity.getString(R.string.dashboard_gpa_rule_summary_suffix)


        findPreference("report_bug").onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val uri = Uri.parse(getString(R.string.bug_report_email))
            val intent = Intent(Intent.ACTION_SENDTO, uri)
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.bug_report_email_subject))
            intent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.bug_report_email_content),
                    activity.packageManager.getPackageInfo("com.carbonylgroup.schoolpower", 0).versionName))
            startActivity(Intent.createChooser(intent, getString(R.string.choose_email_app)))
            true
        }
        /*
        findPreference("feedback_forum").onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.feedback_forum_address))))
            true
        }
        */
        findPreference("website").onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website_address))))
            true
        }
        findPreference("source_code").onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.source_code_address))))
            true
        }
        val customGPA = findPreference("list_preference_customize_gpa") as MultiSelectListPreference
        customGPA.entries = activity.intent.getCharSequenceArrayExtra("subjects")
        customGPA.entryValues = activity.intent.getCharSequenceArrayExtra("subjects_values")
    }

    private fun refreshPreferences(sharedPreferences: SharedPreferences?, key: String?) {

        when (key) {
            "list_preference_dashboard_display" -> {
                val dashboard_display = (findPreference("list_preference_dashboard_display") as ListPreference)
                dashboard_display.summary = getString(R.string.dashboard_display_preference_summary_prefix) + dashboard_display.entry + activity.getString(R.string.dashboard_display_preference_summary_suffix)
                utils!!.setSharedPreference(Utils.SettingsPreference, key, sharedPreferences!!.getString(key, "0"))
            }
            "list_preference_language" -> {
                utils!!.setSharedPreference(Utils.SettingsPreference, "lang", sharedPreferences!!.getString(key, "0"))
                recreateMain()
            }
            "preference_enable_notification" -> {
                val jobScheduler = activity.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                if (!PreferenceManager.getDefaultSharedPreferences(activity.applicationContext).getBoolean("preference_enable_notification", true)) {
                    jobScheduler.cancelAll()
                }
            }
            "list_preference_custom_gpa_calculate" -> {
                val gpaRule = findPreference(key) as ListPreference
                gpaRule.summary = getString(R.string.dashboard_gpa_rule_summary_prefix) + gpaRule.entry.toString().toLowerCase() + activity.getString(R.string.dashboard_gpa_rule_summary_suffix)
                utils!!.setSharedPreference(Utils.SettingsPreference, key, sharedPreferences!!.getString(key, "0"))
            }
            "switch_preference_theme_dark" -> {
                val utils = Utils(activity)
                utils.set(utils.THEME, if ((findPreference(key) as SwitchPreference).isChecked) utils.DARK else utils.LIGHT)
                recreateMain()
            }
        }
    }

    private fun showThemeChooser() {
        val utils = Utils(activity)
        val valueList = Arrays.asList(*resources.getStringArray(R.array.theme_array))
        val theme = utils.getTheme()
        val selectIndex = valueList.indexOf(theme)
        AlertDialog.Builder(activity!!)
                .setCancelable(true)
                .setTitle("choosetheme")
                .setSingleChoiceItems(R.array.theme_array, selectIndex, { dialog1, which ->
                    dialog1.dismiss()
                    utils.set(utils.THEME, valueList[which])
                    recreateMain()
                })
                .show()
    }

    private fun recreateMain() {
        callBack!!.onRecreate()
    }
}
