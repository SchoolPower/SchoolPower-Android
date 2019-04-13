/**
 * Copyright (C) 2019 SchoolPower Studio
 */

package com.carbonylgroup.schoolpower.activities

import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.NavUtils
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.RelativeLayout
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.fragments.SettingsFragment
import kotterknife.bindView

private var recreated = false

class SettingsActivity : BaseActivity(), SettingsFragment.SettingsCallBack {

    private val settingsToolBar: Toolbar by bindView(R.id.settings_toolbar)
    private val rootLayout: RelativeLayout by bindView(R.id.settings_root_layout)

    override fun onRecreate() {
        recreated = true
        setResult(Activity.RESULT_OK)
        recreate()
    }

    override fun initActivity() {

        super.initActivity()
        setContentView(R.layout.settings_toolbar)
        setSupportActionBar(settingsToolBar)
        supportFragmentManager.beginTransaction().replace(R.id.settings_content, SettingsFragment()).commit()
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)

        setResult(Activity.RESULT_OK)
        if (recreated) {
            rootLayout.post( { startAnimation() })
            // to invoke onActivityResult to apply settings
            recreated = false
        }
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

    fun addFragmentOnTop(fragment: Fragment){
        supportFragmentManager.beginTransaction()
                .add(R.id.settings_content, fragment)
                .addToBackStack("TAG")
                .commit()
    }
}
