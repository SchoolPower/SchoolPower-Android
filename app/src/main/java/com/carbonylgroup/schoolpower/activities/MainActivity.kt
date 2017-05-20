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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.classes.ListItems.MainListItem
import com.carbonylgroup.schoolpower.classes.Transition.DetailsTransition
import com.carbonylgroup.schoolpower.classes.Transition.TransitionHelper
import com.carbonylgroup.schoolpower.classes.Utils.Utils
import com.carbonylgroup.schoolpower.classes.Utils.postData
import com.carbonylgroup.schoolpower.fragments.CourseDetailFragment
import com.carbonylgroup.schoolpower.fragments.HomeFragment
import kotterknife.bindView
import java.util.*


class MainActivity : TransitionHelper.MainActivity(), NavigationView.OnNavigationItemSelectedListener {

    var presentFragment: Int = 0
    var dataList: ArrayList<MainListItem>? = null
    var mainListItemTransporter: MainListItem? = null
    private var menuOpenDrawer = true
    private var utils: Utils = Utils(this)
    private val mainToolBar: Toolbar by bindView(R.id.main_toolbar)
    private val drawer: DrawerLayout by bindView(R.id.drawer_layout)
    private val mainAppBar: AppBarLayout by bindView(R.id.main_app_bar)
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var toggleIcon: DrawerArrowDrawable

    /* Fragments */
    private var homeFragment: HomeFragment? = null
    private var courseDetailFragment: CourseDetailFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Design)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nav_drawer)

        initValue()
        initUI()
        initOnClick()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_refresh -> {
                initDataJson()
                homeFragment!!.setRefreshing(true)
            }
            R.id.action_new -> {
                //TODO NEW ASSIGNMENTS
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

            1 -> returnToHome()

            else -> super.onBackPressed()
        }
    }

    /* Initializer */
    private fun initValue() {

        setSupportActionBar(mainToolBar)
        toggleIcon = DrawerArrowDrawable(this)
        toggle = ActionBarDrawerToggle(this, drawer, mainToolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        try {
            val input = utils.readDataArrayList()
            if (input != null) dataList = input
        } catch (e: Exception) {
            e.printStackTrace()
        }

        initDataJson()
    }

    private fun initUI() {

        initDrawer()
        setDefaultFragment()
    }

    private fun initOnClick() {

        toggle.toolbarNavigationClickListener = View.OnClickListener {
            if (menuOpenDrawer)
                drawer.openDrawer(GravityCompat.START)
            else
                returnToHome()
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
                presentFragment = 0
            }

            R.id.course_detail_background -> {

                if (courseDetailFragment == null) courseDetailFragment = CourseDetailFragment()
                transaction.replace(R.id.content_view, courseDetailFragment)
                presentFragment = 1
            }

            R.id.nav_settings -> {
                //TODO SETTING
            }

            R.id.nav_sign_out -> confirmSignOut()

            else -> {
            }
        }

        transaction.commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        val id = item.itemId
        gotoFragmentWithMenuItemId(id)

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    fun returnToHome() {

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
        setToolBarElevation(0)
    }

    private fun setDefaultFragment() {

        val fm = fragmentManager
        val transaction = fm.beginTransaction()
        gotoFragmentWithMenuItemId(R.id.nav_dashboard)
        transaction.commit()
    }

    /* Other Method */
    fun initDataJson() {

        val oldMainItemList = ArrayList<MainListItem>()
        if (dataList != null) oldMainItemList.addAll(dataList!!)

        val token = getSharedPreferences("accountData", Activity.MODE_PRIVATE).getString("token", "")

        Thread(postData(
                getString(R.string.postURL), getString(R.string.token_equals) + token,
                object : Handler() {
                    override fun handleMessage(msg: Message) {
                        val messages = msg.obj.toString().split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                        val spEditor = getSharedPreferences(getString(R.string.accountData), Activity.MODE_PRIVATE).edit()
                        spEditor.putString(getString(R.string.student_name), messages[1])
                        spEditor.apply()

                        if (messages.size == 3 && !messages[2].isEmpty()) {
                            val jsonStr = messages[2]
                            try {
                                utils.saveDataJson(jsonStr)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            dataList = utils.parseJsonResult(jsonStr)

                            if (dataList!!.size == oldMainItemList.size) {
                                for (i in dataList!!.indices) {
                                    val periods = dataList!![i].periodGradeItemArrayList
                                    val oldPeriods = oldMainItemList[i].periodGradeItemArrayList

                                    if (periods.size != oldPeriods.size) break

                                    for (j in periods.indices) {
                                        val newAssignmentListCollection = periods[j].assignmentItemArrayList
                                        val oldAssignmentListCollection = oldPeriods[j].assignmentItemArrayList
                                        for (item in newAssignmentListCollection) {
                                            val found = oldAssignmentListCollection.any { it.assignmentTitle == item.assignmentTitle && it.assignmentDividedScore == item.assignmentDividedScore && it.assignmentDate == item.assignmentDate && !it.isNew }
                                            if (!found) item.isNew=true
                                        }
                                    }
                                }
                            }

                            homeFragment!!.refreshAdapter(dataList!!)
                            if (oldMainItemList.size == 0) setDefaultFragment()
                        }

                    }
                })).start()
    }

    private fun getUsername(): String {

        val sharedPreferences = getSharedPreferences(getString(R.string.accountData), Activity.MODE_PRIVATE)
        val name = sharedPreferences.getString(getString(R.string.student_name), "")
        if (name != "") {
            val fullName = name.split(" ")
            return fullName[1] + " " + fullName[2]
        }
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
        spEditor.putString(getString(R.string.token), "")
        spEditor.putBoolean(getString(R.string.loggedIn), false)
        spEditor.apply()
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
        } else
            anim = ValueAnimator.ofFloat(1.0f, 0.0f)

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

        if (enable)
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        else
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    fun expandToolBar(expand: Boolean, animated: Boolean) {
        mainAppBar.setExpanded(expand, animated)
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
