package com.carbonylgroup.schoolpower.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.MainActivity
import com.carbonylgroup.schoolpower.utils.Utils
import com.mikepenz.aboutlibraries.LibsBuilder

class AboutFragment : Fragment() {

    private lateinit var utils: Utils

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.about_content, container, false)
        MainActivity.of(activity).expandToolBar(true, true)
        utils = Utils(activity!!)

        (view.findViewById<View>(R.id.about_version_label) as TextView).text = activity!!.packageManager.getPackageInfo(activity!!.packageName, 0).versionName

        view.findViewById<View>(R.id.about_version_cell).setOnLongClickListener {
            val devMode = !utils.getPreferences(Utils.TmpData)
                    .getBoolean("developer_mode", false)
            utils.setPreference("developer_mode", devMode, Utils.TmpData)
            utils.showSnackBar(view.findViewById(R.id.about_content_parent), "Developer Mode: " + devMode.toString(), false)
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
            //TODO theme the licence activity
            LibsBuilder()
                    .withAboutDescription(getString(R.string.i_love_open_source))
                    .withActivityTitle(getString(R.string.license))
                    .start(activity)
        }

        view.findViewById<View>(R.id.about_bug_cell).setOnClickListener {
            val version = activity!!.packageManager.getPackageInfo("com.carbonylgroup.schoolpower", 0).versionName
            val uri = Uri.parse(getString(R.string.bug_report_email))
            val intent = Intent(Intent.ACTION_SENDTO, uri)
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.bug_report_email_subject))
            intent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.bug_report_email_content), version))
            startActivity(Intent.createChooser(intent, getString(R.string.choose_email_app)))
        }

        view.findViewById<View>(R.id.about_website_cell).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website_address))))
        }
/*
        view.findViewById<View>(R.id.about_qq_cell).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.qq_group_address))))
        }*/

        view.findViewById<View>(R.id.about_code_cell).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.source_code_address))))
        }

        return view
    }
}