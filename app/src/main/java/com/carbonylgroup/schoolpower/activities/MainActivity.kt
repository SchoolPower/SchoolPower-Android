/**
 * Copyright (C) 2017 SchoolPower Studio
 */

package com.carbonylgroup.schoolpower.activities

import android.animation.ValueAnimator
import android.app.Activity
import android.app.AlertDialog
import android.app.job.JobInfo
import android.app.job.JobInfo.NETWORK_TYPE_ANY
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.preference.PreferenceManager
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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.data.Attendance
import com.carbonylgroup.schoolpower.data.StudentData
import com.carbonylgroup.schoolpower.data.StudentInformation
import com.carbonylgroup.schoolpower.data.Subject
import com.carbonylgroup.schoolpower.fragments.AboutFragment
import com.carbonylgroup.schoolpower.fragments.AttendanceFragment
import com.carbonylgroup.schoolpower.fragments.ChartFragment
import com.carbonylgroup.schoolpower.fragments.HomeFragment
import com.carbonylgroup.schoolpower.service.PullDataJob
import com.carbonylgroup.schoolpower.transition.DetailsTransition
import com.carbonylgroup.schoolpower.transition.TransitionHelper
import com.carbonylgroup.schoolpower.utils.ContextWrapper
import com.carbonylgroup.schoolpower.utils.GPADialog
import com.carbonylgroup.schoolpower.utils.ThemeHelper
import com.carbonylgroup.schoolpower.utils.Utils
import com.carbonylgroup.schoolpower.utils.Utils.Companion.AccountData
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotterknife.bindView
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URLConnection
import java.util.*


class MainActivity : TransitionHelper.MainActivity(), NavigationView.OnNavigationItemSelectedListener {

    var presentFragment: Int = 0
    var studentInformation: StudentInformation? = null
    var subjects: List<Subject>? = null
    var attendances: List<Attendance>? = null
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
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    private lateinit var toggleIcon: DrawerArrowDrawable

    /* Fragments */
    private var homeFragment: HomeFragment? = null
    private var chartFragment: ChartFragment? = null
    private var attendanceFragment: AttendanceFragment? = null
    private var aboutFragment: AboutFragment? = null

    private val SETTINGS_REQUEST_CODE = 233

    override fun attachBaseContext(newBase: Context) {

        utils = Utils(newBase)
        val newLocale = utils.getSharedPreference(Utils.SettingsPreference).getString("lang", "0").toInt()
        val context = ContextWrapper.wrap(newBase, localeSet[newLocale])
        super.attachBaseContext(context)

    }

    override fun initActivity() {

        utils = Utils(this)
        super.initActivity()
        setContentView(R.layout.nav_drawer)
        initValue()
        initUI()
        initOnClick()
        initScheduler()
        utils.checkApplicationUpdate()
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
                fetchStudentDataFromServer()
                when (presentFragment) {
                    0 -> homeFragment!!.setRefreshing(true)
                    3 -> attendanceFragment!!.setRefreshing(true)
                }
            }
            R.id.action_gpa -> {

                if (subjects == null || subjects!!.count() == 0) {

                    val builder = AlertDialog.Builder(this)
                    builder.setMessage(getString(R.string.gpa_not_available_because))
                    builder.setTitle(getString(R.string.gpa_not_available))
                    builder.setPositiveButton(getString(R.string.alright), null)
                    builder.create().show()

                } else GPADialog(this, subjects!!, studentInformation!!.GPA).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {

        super.onRestoreInstanceState(savedInstanceState)
        presentFragment = savedInstanceState.getInt("presentFragment")
        val fragments = intArrayOf(R.id.nav_dashboard, R.id.course_detail_background, R.id.nav_charts,
                R.id.nav_attendance, R.id.nav_settings, R.id.nav_about)
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
            3 -> returnFromPrimaryFragments(2)
            4 -> returnFromSecondaryFragments()
            5 -> returnFromSecondaryFragments()
            else -> super.onBackPressed()
        }
    }

    /* Initializer */
    private fun initValue() {

        MobileAds.initialize(this, getString(R.string.adMob_app_id))
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        setSupportActionBar(mainToolBar)
        toggleIcon = DrawerArrowDrawable(this)
        toggle = ActionBarDrawerToggle(this, drawer, mainToolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        try {
            val input = utils.readDataArrayList()
            studentInformation = input.studentInfo
            attendances = input.attendances
            subjects = input.subjects
        } catch (e: Exception) {
            e.printStackTrace()
        }
        fetchStudentDataFromServer()

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
            when {
                menuOpenDrawer -> drawer.openDrawer(GravityCompat.START)
                presentFragment == 1 -> returnFromDetail()
                presentFragment == 2 -> returnFromPrimaryFragments(1)
                presentFragment == 3 -> returnFromPrimaryFragments(2)
                else -> returnFromSecondaryFragments()
            }
        }
    }

    private fun modifyAvatar() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.avatar_agreement_title))
        builder.setMessage(getString(R.string.avatar_agreement_message))
        builder.setPositiveButton(getString(R.string.accept)) { _, _ ->
            CropImage.activity()
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setAspectRatio(1, 1)
                    .setFixAspectRatio(true)
                    .start(this)
        }
        builder.setNegativeButton(getString(R.string.decline), null)
        builder.show()
    }

    private fun removeAvatar() {
        val username = utils.getSharedPreference(AccountData).getString(getString(R.string.usernameKEY), "")
        val password = utils.getSharedPreference(AccountData).getString(getString(R.string.passwordKEY), "")

        utils.buildNetworkRequest(getString(R.string.avatarURL), "POST",
                MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("username", username)
                        .addFormDataPart("password", password)
                        .addFormDataPart("new_avatar", "")
                        .addFormDataPart("remove_code", "")
                        .build())
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val res = response.body()?.string()
                        if (res?.contains("error") != false) {
                            utils.showSnackBar(this@MainActivity, findViewById(R.id.main_coordinate_layout),
                                    JSONObject(res)["error"].toString(), true)
                        } else {
                            utils.setSharedPreference(AccountData, "user_avatar", "")
                            val header = navigationView.getHeaderView(0)
                            header.findViewById<ImageView>(R.id.user_avatar).post {
                                header.findViewById<ImageView>(R.id.user_avatar).setImageDrawable(getDrawable(R.drawable.icon))
                            }
                        }
                    }
                })
    }

    private fun setAvatar() {

        if (utils.getSharedPreference(AccountData).getString("user_avatar", "") != "") {
            val alertDialog =
                    AlertDialog.Builder(this)
                            .setAdapter(ArrayAdapter<String>(this, R.layout.simple_list_item,
                                    arrayOf(getString(R.string.change_avatar), getString(R.string.remove_avatar))), null)
                            .create()

            alertDialog.listView.setOnItemClickListener { _: AdapterView<*>, _: View, position: Int, _: Long ->
                if (position == 0) modifyAvatar()
                else removeAvatar()
                alertDialog.dismiss()
            }
            alertDialog.show()
        } else {
            modifyAvatar()
        }
    }

    private fun updateAvatar() {
        val header = navigationView.getHeaderView(0)
        val avatarUrl = utils.getSharedPreference(AccountData).getString("user_avatar", "")
        if (avatarUrl != "")
            Picasso.get().load(avatarUrl).placeholder(R.drawable.icon).into(header.findViewById<ImageView>(R.id.user_avatar))

    }

    private fun initDrawer() {

        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        toggle.isDrawerIndicatorEnabled = false
        toggle.setHomeAsUpIndicator(toggleIcon)
        toggle.syncState()

        val header = navigationView.getHeaderView(0)
        header.findViewById<TextView>(R.id.nav_header_username).text = getUsername()
        header.findViewById<TextView>(R.id.nav_header_id).text = getUserID()
        header.findViewById<ImageView>(R.id.user_avatar).setOnClickListener(
                { _ -> setAvatar();true }
        )
        updateAvatar()
    }

    private fun initScheduler() {

        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if (!preferences.getBoolean("preference_enable_notification", true)) {
            Log.d("PullDataJob", "Job Gets Cancelled")
            jobScheduler.cancelAll()
            return
        }
        if (jobScheduler.allPendingJobs.size != 0) return
        Log.d("PullDataJob", "Job Registered")

        val builder = JobInfo.Builder(1, ComponentName(this, PullDataJob::class.java))
                .setPeriodic(1000L * 60 * 60) // one hour
                .setRequiredNetworkType(NETWORK_TYPE_ANY)
                .setPersisted(true)
        jobScheduler.schedule(builder.build())
    }

    /* Fragments Handler */
    private fun gotoFragmentWithMenuItemId(id: Int) {

        val fm = fragmentManager
        val transaction = fm.beginTransaction()

        when (id) {
            R.id.nav_dashboard -> {
                homeFragment = HomeFragment()
                transaction.replace(R.id.content_view, homeFragment)
                setToolBarTitle(getString(R.string.dashboard))
                setToolBarElevation()
                expandToolBar(true, true)
                hideToolBarItems(false)
                presentFragment = 0
            }
            R.id.nav_charts -> {
                chartFragment = ChartFragment()
                transaction.replace(R.id.content_view, chartFragment)
                setToolBarTitle(getString(R.string.charts))
                setToolBarElevation(0)
                expandToolBar(true, true)
                hideToolBarItems(true)
                presentFragment = 2
            }
            R.id.nav_attendance -> {
                attendanceFragment = AttendanceFragment()
                transaction.replace(R.id.content_view, attendanceFragment)
                setToolBarTitle(getString(R.string.attendance))
                setToolBarElevation()
                expandToolBar(true, true)
                hideToolBarItems(true)
                presentFragment = 3
            }
            R.id.nav_settings -> startSettingsActivity()

            R.id.nav_about -> {
                aboutFragment = AboutFragment()
                transaction.setCustomAnimations(R.animator.slide_from_right_in, R.animator.slide_to_left_out)
                        .replace(R.id.content_view, aboutFragment)
                setToolBarTitle(getString(R.string.about))
                setToolBarElevation()
                expandToolBar(true, true)
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

    private fun returnFromDetail() {

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


        MainActivity.of(this).setToolBarColor(
                utils.getPrimaryColor(), true)

        animateDrawerToggle(false)
        hideToolBarItems(false)
        setToolBarElevation(0)
    }

    private fun returnFromSecondaryFragments() {

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
//        if (subjects != null && subjects!!.count() != 0) homeFragment!!.notifyAdapter()

        //TODO Bugs might occur when adding new menu items QAQ
        navigationView.menu.getItem(1).isChecked = false
        navigationView.menu.getItem(0).isChecked = true
    }

    /**
     * @param   index:   The position of the primary fragment, Dashboard is 0, so starts from 1
     */
    private fun returnFromPrimaryFragments(index: Int) {

        expandToolBar(true, true)

        if (homeFragment == null) homeFragment = HomeFragment()

        fragmentManager
                .beginTransaction()
                .replace(R.id.content_view, homeFragment)
                .addToBackStack(null)
                .commit()

        hideToolBarItems(false)
//        if (subjects != null && subjects!!.count() != 0) homeFragment!!.notifyAdapter()
        navigationView.menu.getItem(index).isChecked = false
        navigationView.menu.getItem(0).isChecked = true
    }

    private fun setDefaultFragment() {

        val fm = fragmentManager
        val transaction = fm.beginTransaction()
        gotoFragmentWithMenuItemId(R.id.nav_dashboard)
        transaction.commit()
    }

    /* Other Methods */
    fun fetchStudentDataFromServer() {

        val oldSubjects = ArrayList<Subject>()
        val oldAttendances = ArrayList<Attendance>()

        val username = utils.getSharedPreference(AccountData).getString(getString(R.string.usernameKEY), "")
        val password = utils.getSharedPreference(AccountData).getString(getString(R.string.passwordKEY), "")
        if (subjects != null) oldSubjects.addAll(subjects!!)
        if (attendances != null) oldAttendances.addAll(attendances!!)

        val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", username)
                .addFormDataPart("password", password)
                .addFormDataPart("version", utils.getAppVersion())
                .addFormDataPart("action", "manual_get_data")
                .addFormDataPart("os", "android")
                .build()

        var retried = false
        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                val backupServer = utils.getBackupServerUrl("pull_data_2")
                if (!retried && backupServer != null) {
                    retried = true
                    try {
                        val response = utils.buildNetworkRequest(backupServer, "POST", body).execute()
                        onResponse(call, response)
                    } catch (e: IOException) {
                    }
                    return
                }
                utils.showSnackBar(this@MainActivity, findViewById(R.id.main_coordinate_layout), getString(R.string.no_connection), true)
                when (presentFragment) {
                    0 -> homeFragment?.setRefreshing(false)
                    3 -> attendanceFragment?.setRefreshing(false)
                }

                noConnection = true
            }

            override fun onResponse(call: Call, response: Response) {
                val strMessage = response.body()!!.string().replace("\n", "")

                // Error happened. Usually caused by wrong username/password
                if (strMessage.contains("Something went wrong!")) {
                    utils.showSnackBar(this@MainActivity, findViewById(R.id.main_coordinate_layout), getString(R.string.wrong_password), true)
                    signOut()
                    return
                }

                // Get response but not a valid JSON
                if (!strMessage.contains("{")) {
                    utils.showSnackBar(this@MainActivity, findViewById(R.id.main_coordinate_layout), getString(R.string.server_problem) + strMessage, true)
                    when (presentFragment) {
                        0 -> homeFragment!!.setRefreshing(false)
                        3 -> attendanceFragment!!.setRefreshing(false)
                    }
                    noConnection = true
                    return
                }

                utils.saveDataJson(strMessage)
                val data = StudentData(this@MainActivity, strMessage)
                if (data.disabled) {
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setMessage(data.disabledMessage)
                    builder.setTitle(data.disabledTitle)
                    builder.setPositiveButton(getString(R.string.alright), null)
                    builder.create().show()
                }
                studentInformation = data.studentInfo
                subjects = data.subjects
                attendances = data.attendances
                val extraInfo = data.extraInfo

                utils.setSharedPreference(AccountData, "user_avatar", extraInfo.avatar)

                when (presentFragment) {
                    0 -> if (subjects!!.isEmpty()) homeFragment!!.refreshAdapterToEmpty()
                    3 -> if (attendances!!.isEmpty()) attendanceFragment!!.refreshAdapterToEmpty()
                }
                utils.saveHistoryGrade(subjects!!)

                // Mark new or changed assignments
                if (subjects!!.size == oldSubjects.size) {
                    for (i in subjects!!.indices) {
                        subjects!![i].markNewAssignments(oldSubjects[i], this@MainActivity)
                    }
                }
                // Mark new or changed attendances
                for (item in attendances!!) {
                    val found = oldAttendances.any { it -> it.name == item.name && it.date == item.date && it.code == item.code && !it.isNew }
                    if (!found) item.isNew = true
                }
                runOnUiThread {
                    when (presentFragment) {
                        0 -> homeFragment!!.refreshAdapter(subjects!!)
                        3 -> attendanceFragment!!.refreshAdapter(attendances!!)
                    }

                    updateAvatar()
                    utils.showSnackBar(this@MainActivity, findViewById(R.id.main_coordinate_layout), getString(R.string.data_updated), false)
                }
            }
        }

        utils.buildNetworkRequest(getString(R.string.postURL), "POST", body).enqueue(callback)
    }

    private fun getUsername(): String {

        val name = utils.getSharedPreference(AccountData).getString(getString(R.string.student_name), "")
        if (name != "") return name.split(" ")[1] + " " + name.split(" ")[2]
        return getString(R.string.no_username)
    }

    private fun getUserID(): String {

        val id = utils.getSharedPreference(AccountData).getString(getString(R.string.user_id), "")
        return getString(R.string.user_id_indicator) + " " + id
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

        utils.getSharedPreference(AccountData).edit()
                .putString(getString(R.string.usernameKEY), "")
                .putString(getString(R.string.passwordKEY), "")
                .putBoolean(getString(R.string.loggedIn), false)
                .apply()
        utils.saveHistoryGrade(null)
        utils.saveDataJson("")

        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.cancelAll()

        startLoginActivity()
    }

    private fun startSettingsActivity() {
        val intent = Intent(application, SettingsActivity::class.java)
        val subjectList = subjects!!
                .filter { it -> utils.getLatestPeriodGrade(it) != null }
                .mapTo(ArrayList<CharSequence>()) { it -> "${it.name} (${utils.getLatestPeriodGrade(it)!!.percentage}%)" }
        val subjectValueList = subjects!!
                .filter { it -> utils.getLatestPeriodGrade(it) != null }
                .mapTo(ArrayList<CharSequence>()) { it.name }
        intent.putExtra("subjects", subjectList.toTypedArray())
        intent.putExtra("subjects_values", subjectValueList.toTypedArray())
        startActivityForResult(intent, SETTINGS_REQUEST_CODE)
        // Use startActivityForResult to invoke onActivityResult to apply settings
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == SETTINGS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            recreate()
            return
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val file = File(result.uri.path)
                val body = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("smfile", file.name,
                                RequestBody.create(MediaType.parse(URLConnection.guessContentTypeFromName(file.name)), file))
                        .build()

                utils.buildNetworkRequest(getString(R.string.imageUploadURL), "POST", body).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        utils.showSnackBar(this@MainActivity, findViewById(R.id.main_coordinate_layout), getString(R.string.avatar_upload_failed), true)
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val res = response.body()?.string()
                        Log.d("avatar", res)
                        val responseJson = JSONObject(res)
                        if (responseJson["code"] != "success") {
                            utils.showSnackBar(this@MainActivity, findViewById(R.id.main_coordinate_layout), getString(R.string.avatar_upload_failed), true)
                            return
                        }
                        val avatarUrl = responseJson.getJSONObject("data")["url"].toString()
                        val username = utils.getSharedPreference(AccountData).getString(getString(R.string.usernameKEY), "")
                        val password = utils.getSharedPreference(AccountData).getString(getString(R.string.passwordKEY), "")

                        val responseAvatar = utils.buildNetworkRequest(getString(R.string.avatarURL), "POST",
                                MultipartBody.Builder()
                                        .setType(MultipartBody.FORM)
                                        .addFormDataPart("username", username)
                                        .addFormDataPart("password", password)
                                        .addFormDataPart("new_avatar", avatarUrl)
                                        .addFormDataPart("remove_code", responseJson.getJSONObject("data")["hash"].toString())
                                        .build()
                        ).execute().body()?.string()

                        if (responseAvatar?.contains("error") != false) {
                            utils.showSnackBar(this@MainActivity, findViewById(R.id.main_coordinate_layout),
                                    JSONObject(responseAvatar)["error"].toString(), true)
                        }

                        utils.setSharedPreference(AccountData, "user_avatar", avatarUrl)

                        val header = navigationView.getHeaderView(0)
                        header.findViewById<ImageView>(R.id.user_avatar).post {
                            updateAvatar()
                        }
                    }
                })

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //val error = result.error
            }
        }
        if (presentFragment == 0) { // Refresh the home fragment to apply settings

            homeFragment = HomeFragment()

            fragmentManager
                    .beginTransaction()
                    .replace(R.id.content_view, homeFragment)
                    .addToBackStack(null)
                    .commit()
        }
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

    fun setToolBarElevation(toolBarElevation: Int = resources.getDimensionPixelOffset(R.dimen.toolbar_elevation)) {
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
