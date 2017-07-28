/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.activities

import android.animation.ValueAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.AppBarLayout
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.graphics.drawable.DrawerArrowDrawable
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import co.ceryle.segmentedbutton.SegmentedButtonGroup
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.classes.ListItems.Subject
import com.carbonylgroup.schoolpower.classes.Transition.DetailsTransition
import com.carbonylgroup.schoolpower.classes.Transition.TransitionHelper
import com.carbonylgroup.schoolpower.classes.Utils.ContextWrapper
import com.carbonylgroup.schoolpower.classes.Utils.Utils
import com.carbonylgroup.schoolpower.classes.Utils.WaveHelper
import com.carbonylgroup.schoolpower.classes.Utils.postData
import com.carbonylgroup.schoolpower.fragments.ChartFragment
import com.carbonylgroup.schoolpower.fragments.HomeFragment
import com.carbonylgroup.schoolpower.fragments.SettingsFragment
import com.gelitenight.waveview.library.WaveView
import com.github.premnirmal.textcounter.CounterView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.mikepenz.aboutlibraries.LibsBuilder
import kotterknife.bindView
import java.util.*

class MainActivity : TransitionHelper.MainActivity(), NavigationView.OnNavigationItemSelectedListener {

    var presentFragment: Int = 0
    var dataList: ArrayList<Subject>? = null
    var subjectTransporter: Subject? = null

    private var noConnection = false
    private var menuOpenDrawer = true
    private var utils: Utils = Utils(this)
    private var hideToolBarItemFlag = false
    private val mainToolBar: Toolbar by bindView(R.id.main_toolbar)
    private val drawer: DrawerLayout by bindView(R.id.drawer_layout)
    private val mainAppBar: AppBarLayout by bindView(R.id.main_app_bar)
    private val localeSet = arrayListOf(Resources.getSystem().configuration.locale, Locale.ENGLISH, Locale.TRADITIONAL_CHINESE, Locale.SIMPLIFIED_CHINESE)

    private lateinit var mAdView: AdView
    private lateinit var waveView: WaveView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    private lateinit var toggleIcon: DrawerArrowDrawable
    private lateinit var gpa_dialog_percentage_back: CounterView
    private lateinit var gpa_dialog_percentage_front: CounterView

    /* Fragments */
    private var homeFragment: HomeFragment? = null
    private var chartFragment: ChartFragment? = null
    private var settingsFragment: SettingsFragment? = null

    override fun attachBaseContext(newBase: Context) {

        utils = Utils(newBase)
        var newLocale = utils.readLangPref()
        if (newLocale == null) newLocale = 0
        val context = ContextWrapper.wrap(newBase, localeSet[newLocale])
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        utils = Utils(this)
        initBase()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nav_drawer)
        initValue()
        initUI()
        initOnClick()
        utils.checkUpdate()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu_main, menu)
        menu.findItem(R.id.action_gpa).isVisible = !hideToolBarItemFlag
        menu.findItem(R.id.action_refresh).isVisible = !hideToolBarItemFlag
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_refresh -> {
                initDataJson()
                homeFragment!!.setRefreshing(true)
            }
            R.id.action_gpa -> {

                var sum_gpa = 0.0
                var gpa_except_hr = 0.0
                var gpa_except_hr_me = 0.0
                var num = 0
                for (i in dataList!!.indices) {

                    val periods = dataList!![i]
                    val grade = utils.getLatestItem(periods)!!.termPercentageGrade.toDouble()
                    sum_gpa += grade
                    num += 1
                    if (periods.subjectTitle.contains("Homeroom")) continue
                    gpa_except_hr += grade
                    if (periods.subjectTitle.contains("Moral Education")) continue
                    gpa_except_hr_me += grade
                }

                val gpaDialog = layoutInflater.inflate(R.layout.gpa_dialog, null)
                val gpaDialogView = gpaDialog.findViewById(R.id.gpa_dialog_rootView)
                val gpaDialogBuilder = AlertDialog.Builder(this)
                val gpa_dialog_segmented = gpaDialogView.findViewById(R.id.gpa_dialog_segmented) as SegmentedButtonGroup
                val percentage_all = (sum_gpa / num).toFloat()
                val percentage_exhr = (gpa_except_hr / (num - 1)).toFloat()
                val percentage_exhrme = (gpa_except_hr_me / (num - 2)).toFloat()
                val waveLightColor = utils.getColorByLetterGrade(this, utils.getLetterGradeByPercentageGrade(percentage_all))
                val waveDarkColor = utils.getDarkColorByPrimary(waveLightColor)

                gpa_dialog_percentage_front = gpaDialogView.findViewById(R.id.gpa_dialog_percentage_front) as CounterView
                gpa_dialog_percentage_back = gpaDialogView.findViewById(R.id.gpa_dialog_percentage_back) as CounterView
                gpa_dialog_percentage_front.setFormatter({ prefix, suffix, value -> String.format("%.3f", value) + suffix })
                gpa_dialog_percentage_back.setFormatter({ prefix, suffix, value -> String.format("%.3f", value) + suffix })
                gpa_dialog_percentage_front.setAutoStart(false)
                gpa_dialog_percentage_front.setPrefix("")
                gpa_dialog_percentage_front.setSuffix("%")
                gpa_dialog_percentage_back.setAutoStart(false)
                gpa_dialog_percentage_back.setPrefix("")
                gpa_dialog_percentage_back.setSuffix("%")
                waveView = gpaDialogView.findViewById(R.id.gpa_Dialog_wave_view) as WaveView
                waveView.setShapeType(WaveView.ShapeType.CIRCLE)

                gpa_dialog_segmented.rippleColor = waveDarkColor
                gpa_dialog_segmented.selectorColor = waveDarkColor
                gpa_dialog_segmented.setPosition(0, 0)
                gpa_dialog_segmented.setOnClickedButtonPosition({
                    position: Int ->
                    when (position) {
                        0 -> animateWaveAndText(waveView.waterLevelRatio, percentage_all)
                        1 -> animateWaveAndText(waveView.waterLevelRatio, percentage_exhr)
                        2 -> animateWaveAndText(waveView.waterLevelRatio, percentage_exhrme)
                    }
                })

                gpaDialogBuilder.setView(gpaDialogView)
                gpaDialogBuilder.setTitle("GPA")
                gpaDialogBuilder.setMessage(String.format(getString(R.string.your_gpa), utils.getLatestItem(dataList!![0])!!.termIndicator))
                gpaDialogBuilder.setPositiveButton(getString(R.string.sweet), null)
                gpaDialogBuilder.create().setCanceledOnTouchOutside(true)
                gpaDialogBuilder.create().show()

                animateWaveAndText(0f, percentage_all)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {

        super.onRestoreInstanceState(savedInstanceState)
        presentFragment = savedInstanceState.getInt("presentFragment")
        val fragments = intArrayOf(R.id.nav_dashboard, R.id.course_detail_background)
        gotoFragmentWithMenuItemId(fragments[presentFragment])
    }

    override fun onSaveInstanceState(outState: Bundle) {

        super.onSaveInstanceState(outState)
        outState.putInt("presentFragment", presentFragment)
    }

    override fun onBackPressed() {

        when (presentFragment) {
            1 -> returnFromDetail()
            2 -> returnFromPrimaryFragments(1)
            3 -> returnFromSecondaryFragments()
            4 -> returnFromSecondaryFragments()
            else -> super.onBackPressed()
        }
    }

    /* Initializer */
    private fun initBase() {

        setTheme(R.style.Design)
    }

    private fun initValue() {

        MobileAds.initialize(this, getString(R.string.adMob_app_id))
        mAdView = findViewById(R.id.adView) as AdView
        val adRequest = AdRequest.Builder().build()
        mAdView?.loadAd(adRequest)

        setSupportActionBar(mainToolBar)
        toggleIcon = DrawerArrowDrawable(this)
        toggle = ActionBarDrawerToggle(this, drawer, mainToolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        val input = utils.readDataArrayList()
        if (input != null) dataList = input
        initDataJson()

        //Start refreshing animation on startup refreshing data
        //Don't when there is no connection
        mainAppBar.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                mainAppBar.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (!noConnection) homeFragment!!.setRefreshing(true)
                else noConnection = false
            }
        })
    }

    private fun initUI() {

        initDrawer()
        setDefaultFragment()
    }

    private fun initOnClick() {

        toggle.toolbarNavigationClickListener = View.OnClickListener {
            if (menuOpenDrawer) drawer.openDrawer(GravityCompat.START)
            else if (presentFragment == 1) returnFromDetail()
            else if (presentFragment == 2) returnFromPrimaryFragments(1)
            else returnFromSecondaryFragments()
        }
    }

    private fun initDrawer() {

        navigationView = findViewById(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        toggle.isDrawerIndicatorEnabled = false
        toggle.setHomeAsUpIndicator(toggleIcon)
        toggle.syncState()

        (navigationView.getHeaderView(0).findViewById(R.id.nav_header_username) as TextView).text = getUsername()
        (navigationView.getHeaderView(0).findViewById(R.id.nav_header_id) as TextView).text = getUserID()

    }

    /* Fragments Handler */
    fun gotoFragmentWithMenuItemId(id: Int) {

        val fm = fragmentManager
        val transaction = fm.beginTransaction()

        when (id) {
            R.id.nav_dashboard -> {
                homeFragment = HomeFragment()
                transaction.replace(R.id.content_view, homeFragment)
                hideToolBarItems(false)
                presentFragment = 0
            }
            R.id.nav_charts -> {
                chartFragment = ChartFragment()
                transaction.replace(R.id.content_view, chartFragment)
                setToolBarTitle(getString(R.string.charts))
                hideToolBarItems(true)
                presentFragment = 2
            }
            R.id.nav_settings -> {
                settingsFragment = SettingsFragment()
                transaction.setCustomAnimations(R.animator.slide_from_right_in, R.animator.slide_to_left_out)
                        .replace(R.id.content_view, settingsFragment)
                setToolBarTitle(getString(R.string.settings))
                animateDrawerToggle(true)
                hideToolBarItems(true)
                presentFragment = 3
            }
            R.id.nav_about -> {
                val aboutFragment = LibsBuilder()
                        .withAboutIconShown(true)
                        .withAboutVersionShown(true)
                        .withAboutDescription(getString(R.string.i_love_open_source))
                        .fragment()
                transaction.setCustomAnimations(R.animator.slide_from_right_in, R.animator.slide_to_left_out)
                        .replace(R.id.content_view, aboutFragment)
                setToolBarTitle(getString(R.string.about))
                animateDrawerToggle(true)
                hideToolBarItems(true)
                presentFragment = 4
            }
            R.id.nav_sign_out -> confirmSignOut()

            else -> {
            }
        }

        transaction.commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        val id = item.itemId
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        gotoFragmentWithMenuItemId(id)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    fun returnFromDetail() {

        expandToolBar(true, true)

        if (homeFragment == null) homeFragment = HomeFragment()
        homeFragment!!.sharedElementEnterTransition = DetailsTransition()
        homeFragment!!.sharedElementReturnTransition = DetailsTransition()

        fragmentManager
                .beginTransaction()
                .addSharedElement(findViewById(R.id.detail_view_header), getString(R.string.shared_element_course_header))
                .addSharedElement(findViewById(R.id.detail_subject_title_tv), getString(R.string.shared_element_course_subject_title))
                .setCustomAnimations(R.animator.do_nothing, R.animator.fade_out)
                .replace(R.id.content_view, homeFragment)
                .addToBackStack(null)
                .commit()

        MainActivity.of(this).setToolBarColor(ContextCompat.getColor(this, R.color.primary), true)
        animateDrawerToggle(false)
        hideToolBarItems(false)
        setToolBarElevation(0)
    }

    fun returnFromSecondaryFragments() {

        expandToolBar(true, true)

        if (homeFragment == null) homeFragment = HomeFragment()

        fragmentManager
                .beginTransaction()
                .setCustomAnimations(R.animator.slide_to_right_in, R.animator.slide_from_left_out)
                .replace(R.id.content_view, homeFragment)
                .addToBackStack(null)
                .commit()

        animateDrawerToggle(false)
        hideToolBarItems(false)
        homeFragment!!.notifyAdapter()
    }

    /**
     * @param   index:   The position of the primary fragment, Dashboard is 0, so starts from 1
     */
    fun returnFromPrimaryFragments(index: Int) {

        expandToolBar(true, true)

        if (homeFragment == null) homeFragment = HomeFragment()

        fragmentManager
                .beginTransaction()
                .replace(R.id.content_view, homeFragment)
                .addToBackStack(null)
                .commit()

        hideToolBarItems(false)
        homeFragment!!.notifyAdapter()
        navigationView.menu.getItem(index).isChecked = false
        navigationView.menu.getItem(0).isChecked = true
    }

    private fun setDefaultFragment() {

        val fm = fragmentManager
        val transaction = fm.beginTransaction()
        gotoFragmentWithMenuItemId(R.id.nav_dashboard)
        transaction.commit()
    }

    /* Other Method */
    fun initDataJson() {

        val oldMainItemList = ArrayList<Subject>()
        val username = getSharedPreferences("accountData", Activity.MODE_PRIVATE).getString(getString(R.string.usernameKEY), "")
        val password = getSharedPreferences("accountData", Activity.MODE_PRIVATE).getString(getString(R.string.passwordKEY), "")
        if (dataList != null) oldMainItemList.addAll(dataList!!)

        Thread(postData(
                getString(R.string.postURL),
                getString(R.string.username_equals) + username + "&" + getString(R.string.password_equals) + password,
                object : Handler() {
                    override fun handleMessage(msg: Message) {
                        val messages = msg.obj.toString().split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                        if (msg.obj.toString().contains(getString(R.string.error_wrong_password))) {
                            utils.showSnackBar(this@MainActivity, findViewById(R.id.main_coordinate_layout), getString(R.string.wrong_password), true)
                            signOut()
                        } else if (msg.obj.toString().contains(getString(R.string.json_begin))) {

                            val spEditor = getSharedPreferences(getString(R.string.accountData), Activity.MODE_PRIVATE).edit()
                            spEditor.putString(getString(R.string.student_name), messages[1])
                            spEditor.apply()

                            if (messages.size == 3 && !messages[2].isEmpty()) {
                                val jsonStr = messages[2]
                                utils.saveDataJson(jsonStr)
                                dataList = utils.parseJsonResult(jsonStr)
                                utils.saveHistoryGrade(dataList!!)

                                //Diff
                                if (dataList!!.size == oldMainItemList.size) {
                                    for (i in dataList!!.indices) {
                                        val periods = dataList!![i].periodArrayList
                                        val oldPeriods = oldMainItemList[i].periodArrayList
                                        if (periods.size != oldPeriods.size) continue
                                        for (j in periods.indices) {
                                            val newAssignmentListCollection = periods[j].assignmentItemArrayList
                                            val oldAssignmentListCollection = oldPeriods[j].assignmentItemArrayList
                                            for (item in newAssignmentListCollection) {
                                                val found = oldAssignmentListCollection.any { it.assignmentTitle == item.assignmentTitle && it.assignmentDividedScore == item.assignmentDividedScore && it.assignmentDate == item.assignmentDate && !it.isNew }
                                                if (!found)
                                                    item.isNew = true
                                            }
                                        }
                                    }
                                }
                                homeFragment!!.refreshAdapter(dataList!!)
                                utils.showSnackBar(this@MainActivity, findViewById(R.id.main_coordinate_layout), getString(R.string.data_updated), false)
                            }
                        } else {
                            utils.showSnackBar(this@MainActivity, findViewById(R.id.main_coordinate_layout), getString(R.string.no_connection), true)
                            homeFragment!!.setRefreshing(false)
                            noConnection = true
                        }
                    }
                })).start()
    }

    private fun getUsername(): String {

        val sharedPreferences = getSharedPreferences(getString(R.string.accountData), Activity.MODE_PRIVATE)
        val name = sharedPreferences.getString(getString(R.string.student_name), "")
        if (name != "") return name.split(" ")[1] + " " + name.split(" ")[2]
        return getString(R.string.no_username)
    }

    private fun getUserID(): String {

        val sharedPreferences = getSharedPreferences(getString(R.string.accountData), Activity.MODE_PRIVATE)
        return getString(R.string.user_id_indicator) + " " + sharedPreferences.getString(getString(R.string.user_id), "")
    }

    private fun confirmSignOut() {

        val builder = AlertDialog.Builder(this)
        builder.setIcon(getDrawable(R.drawable.ic_exit_accent))
        builder.setTitle(getString(R.string.signing_out_dialog_title))
        builder.setMessage(getString(R.string.signing_out_dialog_message))
        builder.setPositiveButton(getString(R.string.signing_out_dialog_positive)) { _, _ -> signOut() }
        builder.setNegativeButton(getString(R.string.signing_out_dialog_negative), null)
        builder.show()
    }

    private fun signOut() {

        val spEditor = getSharedPreferences(getString(R.string.accountData), Activity.MODE_PRIVATE).edit()
        spEditor.putString(getString(R.string.usernameKEY), "")
        spEditor.putString(getString(R.string.passwordKEY), "")
        spEditor.putBoolean(getString(R.string.loggedIn), false)
        spEditor.apply()
        utils.saveDataJson("")
        startLoginActivity()
    }

    private fun startLoginActivity() {

        startActivity(Intent(application, LoginActivity::class.java))
        this@MainActivity.finish()
    }

    fun animateWaveAndText(startValue: Float, endValue: Float) {

        val waveLightColor = utils.getColorByLetterGrade(this, utils.getLetterGradeByPercentageGrade(endValue))
        val waveDarkColor = utils.getDarkColorByPrimary(waveLightColor)

        gpa_dialog_percentage_front.setStartValue(startValue * 100)
        gpa_dialog_percentage_front.setEndValue(endValue)
        gpa_dialog_percentage_front.setIncrement((endValue - startValue * 100) / 25f) // the amount the number increments at each time interval
        gpa_dialog_percentage_front.setTimeInterval(2) // the time interval (ms) at which the text changes
        gpa_dialog_percentage_back.setStartValue(startValue * 100)
        gpa_dialog_percentage_back.setEndValue(endValue)
        gpa_dialog_percentage_back.setIncrement((endValue - startValue * 100) / 25f) // the amount the number increments at each time interval
        gpa_dialog_percentage_back.setTimeInterval(2) // the time interval (ms) at which the text changes
        gpa_dialog_percentage_front.start()
        gpa_dialog_percentage_back.start()

        waveView.setWaveColor(waveLightColor, waveDarkColor)
        waveView.waterLevelRatio = (endValue / 100)
        WaveHelper(waveView, startValue, waveView.waveShiftRatio).start()
    }

    fun animateDrawerToggle(toArrow: Boolean) {

        val anim: ValueAnimator
        menuOpenDrawer = !toArrow
        enableDrawer(!toArrow)
        if (toArrow) {
            anim = ValueAnimator.ofFloat(0.0f, 1.0f)
            toggle.isDrawerIndicatorEnabled = false
        } else anim = ValueAnimator.ofFloat(1.0f, 0.0f)
        anim.addUpdateListener { valueAnimator ->
            val slideOffset = valueAnimator.animatedValue as Float
            toggleIcon.progress = slideOffset
            if (!toArrow && slideOffset == 0f) toggle.isDrawerIndicatorEnabled = true
        }
        anim.interpolator = DecelerateInterpolator()
        anim.duration = 500
        anim.start()
    }

    fun enableDrawer(enable: Boolean) {

        if (enable) drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        else drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    fun expandToolBar(expand: Boolean, animated: Boolean) {
        mainAppBar.setExpanded(expand, animated)
    }

    fun hideToolBarItems(hide: Boolean) {

        hideToolBarItemFlag = hide
        invalidateOptionsMenu()
    }

    fun setToolBarTitle(barTitle: String) {
        supportActionBar!!.title = barTitle
    }

    fun setToolBarElevation(toolBarElevation: Int) {
        mainAppBar.elevation = toolBarElevation.toFloat()
    }

    fun setToolBarColor(_actionBarToColor: Int, _animation: Boolean) {

        val window = this.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        if (_animation) {

            val anim1 = ValueAnimator.ofArgb((mainToolBar.background as ColorDrawable).color, _actionBarToColor)
            val anim2 = ValueAnimator.ofArgb(utils.getDarkColorByPrimary((mainToolBar.background as ColorDrawable).color), utils.getDarkColorByPrimary(_actionBarToColor))
            anim1.addUpdateListener { valueAnimator ->
                mainToolBar.setBackgroundColor(valueAnimator.animatedValue as Int)
                getWindow().navigationBarColor = valueAnimator.animatedValue as Int
            }
            anim2.addUpdateListener { valueAnimator -> window.statusBarColor = valueAnimator.animatedValue as Int }
            anim1.duration = 300
            anim2.duration = 300
            anim1.start()
            anim2.start()

        } else {
            mainToolBar.setBackgroundColor(_actionBarToColor)
            getWindow().navigationBarColor = _actionBarToColor
            window.statusBarColor = utils.getDarkColorByPrimary(_actionBarToColor)
        }
    }

    companion object {
        fun of(activity: Activity): MainActivity {
            return activity as MainActivity
        }
    }
}
