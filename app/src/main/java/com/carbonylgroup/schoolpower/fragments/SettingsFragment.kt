package com.carbonylgroup.schoolpower.fragments

import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.preference.MultiSelectListPreference
import androidx.preference.SwitchPreference
import androidx.appcompat.app.AlertDialog
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.SettingsActivity
import com.carbonylgroup.schoolpower.utils.Utils
import com.carbonylgroup.schoolpower.utils.colorChooser.ColorChooserPreference
import java.util.*

class SettingsFragment : PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        ColorChooserPreference.ColorChooserCallback{

    override fun onColorChanged(@ColorInt oriColor: Int, @ColorInt selectedColor: Int) {
        recreateMain()
    }

    interface SettingsCallBack {
        fun onRecreate()
    }
    private var callBack: SettingsCallBack? = null
    private lateinit var utils: Utils

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
        utils = Utils(activity!!)
        addPreferencesFromResource(R.xml.preferences_content)
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        initPreferences()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (isAdded) refreshPreferences(sharedPreferences, key)
    }

    private fun updatePerferencesStatus(sharedPreferences: SharedPreferences) {

        val dashboardDisplay = (findPreference("list_preference_dashboard_display") as ListPreference)
        dashboardDisplay.summary = activity!!.getString(R.string.dashboard_display_preference_summary_prefix) +
                dashboardDisplay.entry + activity!!.getString(R.string.dashboard_display_preference_summary_suffix)

        val gpaRule = findPreference("list_preference_custom_gpa_calculate") as ListPreference
        gpaRule.summary = getString(R.string.dashboard_gpa_rule_summary_prefix) +
                gpaRule.entry.toString().toLowerCase() + activity!!.getString(R.string.dashboard_gpa_rule_summary_suffix)

        val evenOddSwitch = findPreference("list_preference_is_even_week")
        evenOddSwitch.isEnabled =
                sharedPreferences.getBoolean("list_preference_even_odd_filter", false)

        evenOddSwitch.summary =
                if (sharedPreferences.getBoolean(evenOddSwitch.key, false))
                    getString(R.string.even_odd_filter_even_week)
                else
                    getString(R.string.even_odd_filter_odd_week)

    }

    private fun initPreferences() {

        updatePerferencesStatus(preferenceManager.sharedPreferences)

        (findPreference("list_preference_accent_color") as ColorChooserPreference)
                .setColorChooserCallback(this)

        findPreference("report_bug").onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val uri = Uri.parse(getString(R.string.bug_report_email))
            val intent = Intent(Intent.ACTION_SENDTO, uri)
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.bug_report_email_subject))
            intent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.bug_report_email_content),
                    activity!!.packageManager.getPackageInfo("com.carbonylgroup.schoolpower", 0).versionName))
            startActivity(Intent.createChooser(intent, getString(R.string.choose_email_app)))
            true
        }
        findPreference("status").onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.server_status_address))))
            true
        }
        findPreference("website").onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website_address))))
            true
        }/*
        findPreference("qq_group").onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.qq_group_address))))
            true
        }*/
        findPreference("source_code").onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.source_code_address))))
            true
        }
        val customGPA = findPreference("list_preference_customize_gpa") as MultiSelectListPreference
        customGPA.entries = activity!!.intent.getCharSequenceArrayExtra("subjects")
        customGPA.entryValues = activity!!.intent.getCharSequenceArrayExtra("subjects_values")
    }

    private fun refreshPreferences(sharedPreferences: SharedPreferences?, key: String?) {

        updatePerferencesStatus(sharedPreferences!!)

        when (key) {
            "list_preference_language" -> {
                recreateMain()
            }
            "preference_enable_notification" -> {
                if (!sharedPreferences.getBoolean("preference_enable_notification", true)) {
                    val jobScheduler = activity!!.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                    jobScheduler.cancelAll()
                }
            }
            "switch_preference_theme_dark" -> {
                utils.setPreference(Utils.THEME, if ((findPreference(key) as SwitchPreference).isChecked) Utils.DARK else Utils.LIGHT)
                recreateMain()
            }
            "preference_enable_advertisement" -> {
                if(!utils.isDonated() && !utils.isEarlyDonators()
                        && !sharedPreferences.getBoolean(key, true)){

                    val builder = android.app.AlertDialog.Builder(activity)
                    builder.setTitle(getString(R.string.ad_donation_request))
                    builder.setMessage(getString(R.string.ad_donation_request_content))
                    builder.setPositiveButton(getString(R.string.donation_ok)) { _, _ ->
                        (activity as SettingsActivity).addFragmentOnTop(DonationFragment())
                    }
                    builder.setNegativeButton(getString(R.string.donation_cancel), null)
                    builder.show()

                }
            }
        }
    }

    private fun showThemeChooser() {
        val utils = Utils(activity!!)
        val valueList = Arrays.asList(*resources.getStringArray(R.array.theme_array))
        val theme = utils.getTheme()
        val selectIndex = valueList.indexOf(theme)
        AlertDialog.Builder(activity!!)
                .setCancelable(true)
                .setTitle("choosetheme")
                .setSingleChoiceItems(R.array.theme_array, selectIndex) { dialog1, which ->
                    dialog1.dismiss()
                    utils.setPreference(Utils.THEME, valueList[which])
                    recreateMain()
                }
                .show()
    }

    private fun recreateMain() {
        callBack!!.onRecreate()
    }
}
