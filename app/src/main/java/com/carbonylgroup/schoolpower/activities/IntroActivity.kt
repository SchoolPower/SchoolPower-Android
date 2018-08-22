package com.carbonylgroup.schoolpower.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.fragments.PromotionFragment
import com.carbonylgroup.schoolpower.utils.ContextWrapper
import com.carbonylgroup.schoolpower.utils.ThemeHelper
import com.carbonylgroup.schoolpower.utils.Utils
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment
import java.util.*

class IntroActivity : AppIntro() {

    override fun attachBaseContext(newBase: Context) {
        val newLocale = Utils(newBase).getSharedPreference(Utils.SettingsPreference).getString("lang", "0")!!.toInt()
        val context = ContextWrapper.wrap(newBase, Utils(newBase).localeSet[newLocale])
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper(this).apply()
        super.onCreate(savedInstanceState)

        addSlide(PromotionFragment())
//        addSlide(AppIntroFragment.newInstance(page2))
//        addSlide(AppIntroFragment.newInstance(page3))
//        addSlide(AppIntroFragment.newInstance(page4))
//        addSlide(AppIntroFragment.newInstance(page5))

        val window = this.window
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.navigationBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        setBarColor(ContextCompat.getColor(this, android.R.color.transparent))
        setSeparatorColor(ContextCompat.getColor(this, android.R.color.transparent))

        showSkipButton(true)
        isProgressButtonEnabled = true
    }

    override fun onSkipPressed(currentFragment: Fragment) {
        super.onSkipPressed(currentFragment)
        setDisplayedIntro()
        startMainActivity()
    }

    override fun onDonePressed(currentFragment: Fragment) {
        super.onDonePressed(currentFragment)
        setDisplayedIntro()
        startMainActivity()
    }

    private fun setDisplayedIntro() {
        Utils(this).setSharedPreference("Tmp", "displayedIntro", true)
    }

    private fun startMainActivity() {
        startActivity(Intent(application, MainActivity::class.java))
        finish()
    }
}
