/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.activities

import android.animation.ValueAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
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
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.classes.ListItems.Subject
import com.carbonylgroup.schoolpower.classes.Transition.DetailsTransition
import com.carbonylgroup.schoolpower.classes.Transition.TransitionHelper
import com.carbonylgroup.schoolpower.classes.Utils.Utils
import com.carbonylgroup.schoolpower.classes.Utils.postData
import com.carbonylgroup.schoolpower.fragments.ChartFragment
import com.carbonylgroup.schoolpower.fragments.HomeFragment
import com.carbonylgroup.schoolpower.fragments.SettingsFragment
import kotterknife.bindView
import java.util.*




class MainActivity : TransitionHelper.MainActivity(), NavigationView.OnNavigationItemSelectedListener {

    var presentFragment: Int = 0
    var dataList: ArrayList<Subject>? = null
    var subjectTransporter: Subject? = null
    private var menuOpenDrawer = true
    private var noConnection = false
    private var hideToolBarItemFlag = false
    private var utils: Utils = Utils(this)
    private val mainToolBar: Toolbar by bindView(R.id.main_toolbar)
    private val drawer: DrawerLayout by bindView(R.id.drawer_layout)
    private val mainAppBar: AppBarLayout by bindView(R.id.main_app_bar)
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var toggleIcon: DrawerArrowDrawable

    /* Fragments */
    private var homeFragment: HomeFragment? = null
    private var settingsFragment: SettingsFragment? = null
    private var chartFragment: ChartFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {

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
        menu.findItem(R.id.action_average).isVisible = !hideToolBarItemFlag
        menu.findItem(R.id.action_refresh).isVisible = !hideToolBarItemFlag
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_refresh -> {
                initDataJson()
                homeFragment!!.setRefreshing(true)
            }
            R.id.action_average -> {
                var sum_gpa=0.0
                var gpa_except_hr=0.0
                var gpa_except_hr_me=0.0
                var num=0
                for (i in dataList!!.indices) {
                    val periods = dataList!![i]
                    val grade = periods.getPercentageGrade(utils.getLatestItem(periods)).replace("%","").toDouble()

                    sum_gpa+=grade
                    num+=1
                    if(periods.subjectTitle.contains("Homeroom")) continue
                    gpa_except_hr+=grade
                    if(periods.subjectTitle.contains("Moral Education")) continue
                    gpa_except_hr_me+=grade
                }
                val builder = AlertDialog.Builder(this)
                builder.setMessage(String.format("Your GPA for %s is %.3f\n%.3f (except HR)\n%.3f (except HR & ME)", utils.getLatestItem(dataList!![0])!!.termIndicator, sum_gpa/num, gpa_except_hr/(num-1), gpa_except_hr_me/(num-2)))
                builder.setTitle("GPA")
                builder.setPositiveButton("OK", null)
                builder.setNegativeButton("Cancel", null)
                builder.create().show()
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
            2 -> returnFromFragments()
            3 -> returnFromFragments()
            else -> super.onBackPressed()
        }
    }

    /* Initializer */
    private fun initBase() {

        setTheme(R.style.Design)
        setLanguage(utils.getLocaleWithIndex(utils.getSettingsPreference(getString(R.string.list_preference_language)).toInt()))
    }

    private fun initValue() {

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
            else if (presentFragment == 2) returnFromFragments()
            else returnFromFragments()
        }
    }

    private fun initDrawer() {

        val navigationView = findViewById(R.id.nav_view) as NavigationView
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

            R.id.nav_settings -> {
                settingsFragment = SettingsFragment()
                transaction.setCustomAnimations(R.animator.slide_from_right_in, R.animator.slide_to_left_out)
                        .replace(R.id.content_view, settingsFragment)
                setToolBarTitle(getString(R.string.settings))
                animateDrawerToggle(true)
                hideToolBarItems(true)
                presentFragment = 2
            }
            R.id.nav_charts -> {
                chartFragment = ChartFragment()
                transaction.replace(R.id.content_view, chartFragment)
                setToolBarTitle(getString(R.string.charts))
                hideToolBarItems(true)
                presentFragment = 3
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

    fun returnFromFragments() {

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

    private fun setDefaultFragment() {

        val fm = fragmentManager
        val transaction = fm.beginTransaction()
        gotoFragmentWithMenuItemId(R.id.nav_dashboard)
        transaction.commit()
    }

    /* Other Method */
    private fun setLanguage(language: Locale) {

        val displayMetrics = resources.displayMetrics
        val configuration = resources.configuration
        configuration.locale = language
        resources.updateConfiguration(configuration, displayMetrics)
    }

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
