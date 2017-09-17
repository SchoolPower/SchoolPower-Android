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
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import co.ceryle.segmentedbutton.SegmentedButtonGroup
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.classes.Data.StudentInformation
import com.carbonylgroup.schoolpower.classes.Data.Subject
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
    var studentInformation: StudentInformation? = null
    var subjects: List<Subject>? = null
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

                if (subjects == null || subjects!!.count() == 0) {

                    val builder = AlertDialog.Builder(this)
                    builder.setMessage(getString(R.string.gpa_not_available_because))
                    builder.setTitle(getString(R.string.gpa_not_available))
                    builder.setPositiveButton(getString(R.string.alright), null)
                    builder.create().show()

                } else {

                    var sum_gpa = 0.0
                    var gpa_except_hr = 0.0
                    var gpa_except_hr_me = 0.0
                    var contain_hr = false
                    var contain_me = false
                    var num = 0
                    for (i in subjects!!.indices) {

                        val subject = subjects!![i]
                        val periodGrade = utils.getLatestPeriodGrade(subject) ?: continue
                        if (periodGrade.percentage == "--" || periodGrade.letter == "--") continue
                        val grade = periodGrade.percentage.toDouble()
                        sum_gpa += grade
                        num += 1
                        if (subject.name.contains("Homeroom")) {
                            contain_hr = true
                            continue
                        }
                        gpa_except_hr += grade
                        if (subject.name.contains("Moral Education")) {
                            contain_me = true
                            continue
                        }
                        gpa_except_hr_me += grade
                    }

                    val gpaDialog = layoutInflater.inflate(R.layout.gpa_dialog, null)
                    val gpaDialogView = gpaDialog.findViewById<View>(R.id.gpa_dialog_rootView)
                    val gpaDialogBuilder = AlertDialog.Builder(this)
                    val gpa_dialog_segmented : SegmentedButtonGroup = gpaDialogView.findViewById(R.id.gpa_dialog_segmented)

                    val percentage_all = (sum_gpa / num).toFloat()
                    var num_to_minus = 0
                    if (contain_hr) num_to_minus++
                    val percentage_exhr = (gpa_except_hr / (num - num_to_minus)).toFloat()
                    if (contain_me) num_to_minus++
                    val percentage_exhrme = (gpa_except_hr_me / (num - num_to_minus)).toFloat()

                    val waveLightColor = utils.getColorByLetterGrade(this, utils.getLetterGradeByPercentageGrade(percentage_all))
                    val waveDarkColor = utils.getDarkColorByPrimary(waveLightColor)

                    gpa_dialog_percentage_front = gpaDialogView.findViewById(R.id.gpa_dialog_percentage_front)
                    gpa_dialog_percentage_back = gpaDialogView.findViewById(R.id.gpa_dialog_percentage_back)
                    gpa_dialog_percentage_front.setFormatter({ _, suffix, value -> String.format("%.3f", value) + suffix })
                    gpa_dialog_percentage_back.setFormatter({ _, suffix, value -> String.format("%.3f", value) + suffix })
                    gpa_dialog_percentage_front.setAutoStart(false)
                    gpa_dialog_percentage_front.setPrefix("")
                    gpa_dialog_percentage_front.setSuffix("%")
                    gpa_dialog_percentage_back.setAutoStart(false)
                    gpa_dialog_percentage_back.setPrefix("")
                    gpa_dialog_percentage_back.setSuffix("%")
                    waveView = gpaDialogView.findViewById(R.id.gpa_Dialog_wave_view)
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
                    gpaDialogBuilder.setMessage(String.format(getString(R.string.your_gpa), utils.getLatestPeriod(subjects!![0].grades)))
                    gpaDialogBuilder.setPositiveButton(getString(R.string.sweet), null)
                    gpaDialogBuilder.create().setCanceledOnTouchOutside(true)
                    gpaDialogBuilder.create().show()

                    animateWaveAndText(0f, percentage_all)
                }
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
        mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        setSupportActionBar(mainToolBar)
        toggleIcon = DrawerArrowDrawable(this)
        toggle = ActionBarDrawerToggle(this, drawer, mainToolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        try {
            val input = utils.readDataArrayList()
            studentInformation = input.first
            subjects = input.second
        } catch(e: Exception) {
            e.printStackTrace()
        }
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

        navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        toggle.isDrawerIndicatorEnabled = false
        toggle.setHomeAsUpIndicator(toggleIcon)
        toggle.syncState()

        navigationView.getHeaderView(0).findViewById<TextView>(R.id.nav_header_username).text = getUsername()
        navigationView.getHeaderView(0).findViewById<TextView>(R.id.nav_header_id).text = getUserID()

    }

    /* Fragments Handler */
    fun gotoFragmentWithMenuItemId(id: Int) {

        val fm = fragmentManager
        val transaction = fm.beginTransaction()

        when (id) {
            R.id.nav_dashboard -> {
                homeFragment = HomeFragment()
                transaction.replace(R.id.content_view, homeFragment)
                setToolBarTitle(getString(R.string.dashboard))
                expandToolBar(true, true)
                hideToolBarItems(false)
                presentFragment = 0
            }
            R.id.nav_charts -> {
                chartFragment = ChartFragment()
                transaction.replace(R.id.content_view, chartFragment)
                setToolBarTitle(getString(R.string.charts))
                expandToolBar(true, true)
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
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
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
        if (subjects != null && subjects!!.count() != 0) homeFragment!!.notifyAdapter()

        //TODO Bugs might occur when adding new menu items QAQ
        navigationView.menu.getItem(1).isChecked = false
        navigationView.menu.getItem(0).isChecked = true
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
        if (subjects != null && subjects!!.count() != 0) homeFragment!!.notifyAdapter()
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

        val oldSubjects = ArrayList<Subject>()
        val username = getSharedPreferences("accountData", Activity.MODE_PRIVATE).getString(getString(R.string.usernameKEY), "")
        val password = getSharedPreferences("accountData", Activity.MODE_PRIVATE).getString(getString(R.string.passwordKEY), "")
        if (subjects != null) oldSubjects.addAll(subjects!!)

        Thread(postData(
                getString(R.string.postURL),
                getString(R.string.username_equals) + username + "&" + getString(R.string.password_equals) + password,
                object : Handler() {
                    override fun handleMessage(msg: Message) {
                        val strMessage = msg.obj.toString().replace("\n", "")

                        if (strMessage.contains("Something went wrong!")) {

                            utils.showSnackBar(this@MainActivity, findViewById(R.id.main_coordinate_layout), getString(R.string.wrong_password), true)
                            signOut()

                        } else if (strMessage.contains(getString(R.string.json_begin))) {

                            utils.saveDataJson(strMessage)
                            val data = utils.parseJsonResult(strMessage)
                            studentInformation = data.first
                            subjects = data.second
                            if (subjects!!.isEmpty()) {
                                homeFragment!!.refreshAdapterToEmpty()
                            }
                            utils.saveHistoryGrade(subjects!!)

                            // Mark new or changed assignments
                            if (subjects!!.size == oldSubjects.size) {
                                for (i in subjects!!.indices) {
                                    val newAssignmentListCollection = subjects!![i].assignments
                                    val oldAssignmentListCollection = oldSubjects[i].assignments
                                    for (item in newAssignmentListCollection) {
                                        // if no item in oldAssignmentListCollection has the same title, score and date as those of the new one, then the assignment should be marked.
                                        val found = oldAssignmentListCollection.any { it.title == item.title && it.score == item.score && it.date == item.date && !it.isNew }
                                        if (!found) item.isNew = true
                                    }
                                }
                            }

                            homeFragment!!.refreshAdapter(subjects!!)
                            utils.showSnackBar(this@MainActivity, findViewById(R.id.main_coordinate_layout), getString(R.string.data_updated), false)


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
        utils.saveHistoryGrade(null)
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
