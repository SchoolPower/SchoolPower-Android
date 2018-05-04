/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.preference.*
import android.support.design.widget.AppBarLayout
import android.support.v4.app.NavUtils
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.ContentFrameLayout
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import butterknife.ButterKnife
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.fragments.SettingsFragment
import com.carbonylgroup.schoolpower.utils.ContextWrapper
import com.carbonylgroup.schoolpower.utils.ThemeHelper
import com.carbonylgroup.schoolpower.utils.Utils
import kotterknife.bindView
import java.util.*


class SettingsActivity : BaseActivity(), SettingsFragment.SettingsCallBack {

    private var recreated = false
    private val settingsToolBar: Toolbar by bindView(R.id.settings_toolbar)
    private val rootLayout: Toolbar by bindView(R.id.settings_root_layout)
    private val localeSet = arrayListOf(Resources.getSystem().configuration.locale, Locale.ENGLISH, Locale.TRADITIONAL_CHINESE, Locale.SIMPLIFIED_CHINESE)

    companion object {
        const val LANGUAGE_CHANGED = Activity.RESULT_FIRST_USER
    }

    override fun attachBaseContext(newBase: Context) {

        val utils = Utils(newBase)
        val newLocale = utils.getSharedPreference(Utils.SettingsPreference).getString("lang", "0").toInt()
        val context = ContextWrapper.wrap(newBase, localeSet[newLocale])
        super.attachBaseContext(context)
    }

    override fun onRecreate() {
        recreated = true
        recreate()
    }

    override fun initActivity() {

        super.initActivity()
        setContentView(R.layout.settings_toolbar)
        setSupportActionBar(settingsToolBar)
        fragmentManager.beginTransaction().replace(R.id.settings_content, SettingsFragment()).commit()
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)

//        if (recreated) {
//            rootLayout.post( { startAnimation() })
//            setResult(Activity.RESULT_OK)
//            recreated = false
//        }
        setResult(RESULT_OK) // to invoke onActivityResult to apply settings
    }

    private fun startAnimation() {

        val cx = rootLayout.width / 2
        val cy = rootLayout.height / 2
        val finalRadius = Math.hypot(cx.toDouble(), cy.toDouble()).toFloat()
        val anim = ViewAnimationUtils.createCircularReveal(rootLayout, cx, cy, 0f, finalRadius)
        anim.addListener(object : AnimatorListenerAdapter() {})
        rootLayout.visibility = View.VISIBLE
        anim.start()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        //      finish setting activity to avoid  java.io.NotSerializableException
        //      com.thirtydegreesray.openhub.ui.widget.colorChooser.ColorChooserPreference
        //      android.os.Parcel.writeSerializable(Parcel.java:1761)
        if (recreated) {
            super.onSaveInstanceState(outState)
        } else {
            finish()
        }
    }
}
