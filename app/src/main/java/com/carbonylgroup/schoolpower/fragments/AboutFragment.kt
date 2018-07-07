package com.carbonylgroup.schoolpower.fragments

import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.MainActivity
import com.carbonylgroup.schoolpower.data.Subject
import com.carbonylgroup.schoolpower.utils.Utils
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import com.mikepenz.aboutlibraries.LibsBuilder
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AboutFragment : Fragment() {

    private lateinit var utils: Utils

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.about_content, container, false)
        MainActivity.of(activity).expandToolBar(true, true)
        utils = Utils(activity)

        (view.findViewById<View>(R.id.about_version_label) as TextView).text = activity.packageManager.getPackageInfo(activity.packageName, 0).versionName

        view.findViewById<View>(R.id.about_version_cell).setOnLongClickListener {
            val pref = activity.getSharedPreferences("other", Activity.MODE_PRIVATE)
            val spEditor = pref.edit()
            spEditor.putBoolean("developer_mode", !pref.getBoolean("developer_mode", false))
            spEditor.apply()
            utils.showSnackBar(activity, view.findViewById(R.id.about_content_parent), "Developer Mode: "+pref.getBoolean("developer_mode", false).toString(), false)
            true
        }

        view.findViewById<View>(R.id.about_changelog_cell).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.changelog_address))))
        }

        view.findViewById<View>(R.id.about_eula_cell).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.eula_address))))
        }

        view.findViewById<View>(R.id.about_carbon_cell).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.carbon_address))))
        }

        view.findViewById<View>(R.id.about_null_cell).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.null_address))))
        }

        view.findViewById<View>(R.id.about_license_cell).setOnClickListener {

            val licenseFragment = LibsBuilder()
                    .withAboutDescription(getString(R.string.i_love_open_source))
                    .fragment()

            activity.fragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.animator.slide_from_right_in, R.animator.slide_to_left_out)
                    .replace(R.id.content_view, licenseFragment)
                    .addToBackStack(null)
                    .commit()

            MainActivity.of(activity).presentFragment = 4
        }

        view.findViewById<View>(R.id.about_bug_cell).setOnClickListener {
            val version = activity.packageManager.getPackageInfo("com.carbonylgroup.schoolpower", 0).versionName
            val uri = Uri.parse(getString(R.string.bug_report_email))
            val intent = Intent(Intent.ACTION_SENDTO, uri)
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.bug_report_email_subject))
            intent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.bug_report_email_content), version))
            startActivity(Intent.createChooser(intent, getString(R.string.choose_email_app)))
        }

        view.findViewById<View>(R.id.about_website_cell).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website_address))))
        }

        view.findViewById<View>(R.id.about_code_cell).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.source_code_address))))
        }

        return view
    }
}