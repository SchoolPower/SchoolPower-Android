/**
 * Copyright (C) 2018 SchoolPower Studio
 */

package com.carbonylgroup.schoolpower.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v4.content.res.ResourcesCompat.getDrawable
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.ScaleAnimation
import android.widget.*
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.MainActivity
import com.carbonylgroup.schoolpower.adapter.FoldingCellListAdapter
import com.carbonylgroup.schoolpower.adapter.OnItemClickListener
import com.carbonylgroup.schoolpower.data.Subject
import com.carbonylgroup.schoolpower.transition.DetailsTransition
import com.carbonylgroup.schoolpower.transition.TransitionHelper
import com.carbonylgroup.schoolpower.utils.Utils
import com.ramotion.foldingcell.FoldingCell
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : TransitionHelper.BaseFragment() {

    private var utils: Utils? = null
    private var transformedPosition = -1
    private var view_private: View? = null
    private var fab_in: ScaleAnimation? = null
    private var fab_out: ScaleAnimation? = null
    private var subjects: List<Subject>? = null
    private var unfoldedIndexesBackUp = HashSet<Int>()
    private var adapter: FoldingCellListAdapter? = null
    private var courseDetailFragment: CourseDetailFragment? = null
    private var home_swipe_refresh_layout: SwipeRefreshLayout? = null
    private lateinit var dashboardListView: ListView
    private lateinit var no_grade_view: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view_private = inflater.inflate(R.layout.home_view_content, container, false)
        initAnim()
        initValue()
        return view_private
    }

    override fun onPause() {

        super.onPause()
        //To prevent freezing during fragment transaction
        if (home_swipe_refresh_layout != null) {
            home_swipe_refresh_layout!!.isRefreshing = false
            home_swipe_refresh_layout!!.destroyDrawingCache()
            home_swipe_refresh_layout!!.clearAnimation()
        }
    }

    private fun initAnim() {

        fab_in = ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        fab_in!!.duration = 200
        fab_in!!.interpolator = DecelerateInterpolator()
        fab_out = ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        fab_out!!.duration = 200
        fab_out!!.interpolator = DecelerateInterpolator()
    }

    private fun initValue() {

        utils = Utils(MainActivity.of(activity))
        subjects = MainActivity.of(activity).subjects
        MainActivity.of(activity).presentFragment = 0
        MainActivity.of(activity).setToolBarElevation()
        MainActivity.of(activity).setToolBarTitle(getString(R.string.dashboard))
        dashboardListView = view_private!!.findViewById(R.id.mainListView)
        no_grade_view = view_private!!.findViewById(R.id.no_grade_view)
        view_private!!.findViewById<ImageView>(R.id.no_grade_image_view).setImageDrawable(
                getDrawable(resources,
                        when (utils!!.getTheme()) {
                            utils!!.LIGHT -> R.drawable.no_grades
                            utils!!.DARK -> R.drawable.no_grades_dark
                            else -> R.drawable.no_grades
                        }, null)
        )
        home_swipe_refresh_layout = view_private!!.findViewById(R.id.home_swipe_refresh_layout)
        home_swipe_refresh_layout!!.setColorSchemeResources(R.color.accent, R.color.A_score_green, R.color.B_score_green,
                R.color.Cp_score_yellow, R.color.C_score_orange, R.color.Cm_score_red, R.color.primary)
        home_swipe_refresh_layout!!.setOnRefreshListener { MainActivity.of(activity).fetchStudentDataFromServer() }
        if (subjects == null || utils!!.getFilteredSubjects(subjects!!).count() == 0) refreshAdapterToEmpty()
        else {
            try {
                initAdapter()
            } catch (e: ExceptionInInitializerError) {
                e.printStackTrace()
                refreshAdapterToEmpty()

                val emergencyDialogBuilder = AlertDialog.Builder(activity)
                emergencyDialogBuilder.setTitle("Unknown Fatal Error")
                emergencyDialogBuilder.setMessage("We've just encountered an unknown fatal error while loading your courses.\n" +
                        "Please stay calm and report this to the developers, we will resolve the issue ASAP.\n\n Error message: \n" +
                        e.printStackTrace()
                )
                val sendEmail = DialogInterface.OnClickListener {
                    val version = activity.packageManager.getPackageInfo("com.carbonylgroup.schoolpower", 0).versionName
                    val uri = Uri.parse(getString(R.string.bug_report_email))
                    val intent = Intent(Intent.ACTION_SENDTO, uri)
                    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.bug_report_email_subject))
                    intent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.bug_report_email_content), version) +
                            "\n\nError message: \n" + e.printStackTrace())
                    startActivity(Intent.createChooser(intent, getString(R.string.choose_email_app)))
                }
                emergencyDialogBuilder.setPositiveButton("email",  sendEmail)
                emergencyDialogBuilder.setNegativeButton("cancel", null)
                emergencyDialogBuilder.create().setCanceledOnTouchOutside(false)
                emergencyDialogBuilder.create().show()
            }
        }

        if (needToShowDonate())
            initDonate()
    }

    private fun adapterSetFabOnClickListener(adapter: FoldingCellListAdapter) = adapter.setFabOnClickListener(View.OnClickListener { v ->

        MainActivity.of(activity).subjectTransporter = utils!!.getFilteredSubjects(subjects!!)[dashboardListView.getPositionForView(v) - dashboardListView.headerViewsCount]
        if (transformedPosition != -1) {
            val itemView = getItemViewByPosition(transformedPosition, dashboardListView)
            itemView.findViewById<View>(R.id.unfold_header_view).transitionName = ""
            itemView.findViewById<View>(R.id.detail_subject_title_tv).transitionName = ""
        }
        transformedPosition = dashboardListView.getPositionForView(v) - dashboardListView.headerViewsCount
        val itemView = getItemViewByPosition(dashboardListView.getPositionForView(v), dashboardListView)
        itemView.findViewById<View>(R.id.floating_action_button).startAnimation(fab_out)
        itemView.findViewById<View>(R.id.floating_action_button).visibility = View.GONE
        gotoCourseDetail(itemView.findViewById(R.id.unfold_header_view), itemView.findViewById(R.id.detail_subject_title_tv), transformedPosition)
    })

    private fun adapterSetTermOnClickListener(adapter: FoldingCellListAdapter) =
            adapter.setTermOnClickListener(object : OnItemClickListener {
                override fun onItemClicked(position: Int, view: View) {
                    adapter.showTermDialog(utils!!.getFilteredSubjects(subjects!!)[dashboardListView.getPositionForView(view) - dashboardListView.headerViewsCount], position)
                }
            })

    private fun initAdapter() {

        if (subjects != null && utils!!.getFilteredSubjects(subjects!!).count() != 0) {
            dashboardListView.visibility = View.VISIBLE
            no_grade_view.visibility = View.GONE
        }

        adapter = FoldingCellListAdapter(activity, utils!!.getFilteredSubjects(subjects!!), unfoldedIndexesBackUp, transformedPosition)
        adapterSetFabOnClickListener(adapter!!)
        adapterSetTermOnClickListener(adapter!!)
        dashboardListView.onItemClickListener = AdapterView.OnItemClickListener { _, view, pos, _ ->
            adapter!!.registerToggle(pos - dashboardListView.headerViewsCount)
            (view as FoldingCell).toggle(false)
            adapter!!.refreshPeriodRecycler(view, pos - dashboardListView.headerViewsCount)
            unfoldedIndexesBackUp = adapter!!.unfoldedIndexes
        }
        dashboardListView.adapter = adapter
    }

    private fun initDonate() {
        val inflater = activity.layoutInflater
        val header = inflater.inflate(R.layout.begging_dialog, dashboardListView, false) as ViewGroup
        header.findViewById<Button>(R.id.donate_button).setOnClickListener {
            MainActivity.of(activity).gotoFragmentWithMenuItemId(R.id.nav_support)
            utils!!.setSharedPreference("Tmp", "ImComingForDonation", true)
            removeDonateHeader(header)
        }
        header.findViewById<Button>(R.id.promote_button).setOnClickListener {
            MainActivity.of(activity).gotoFragmentWithMenuItemId(R.id.nav_support)
            removeDonateHeader(header)
        }
        header.findViewById<Button>(R.id.dismiss_donate_button).setOnClickListener {
            removeDonateHeader(header)
        }
        dashboardListView.addHeaderView(header, null, false)
    }

    private fun removeDonateHeader(header: ViewGroup) {
        TransitionManager.beginDelayedTransition(dashboardListView)
        dashboardListView.removeHeaderView(header)
        setLastDonateShowedDate(Date())
    }

    private fun needToShowDonate(): Boolean {
        // Show donate every 30 days
        return ((Date().time - getLastDonateShowedDate().time) / 1000.0 / 60.0 / 60.0 / 24.0 >= 30.0)
    }

    private fun setLastDonateShowedDate(date: Date) {
        Utils(activity).setSharedPreference("Tmp", "LastTimeDonateShowed",
                SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(date))
    }

    private fun getLastDonateShowedDate(): Date {
        val dateStr = Utils(activity)
                .getSharedPreference("Tmp")
                .getString("LastTimeDonateShowed", "")

        return if (dateStr != "") SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(dateStr) else Date(0)
    }

    fun visiblizeNoGradeView() {
        dashboardListView.visibility = View.GONE
        no_grade_view.visibility = View.VISIBLE
    }

    fun setRefreshing(isRefreshing: Boolean) {
        home_swipe_refresh_layout?.isRefreshing = isRefreshing
    }

    fun refreshAdapterToEmpty() {
        subjects = arrayListOf()
        setRefreshing(false)
        visiblizeNoGradeView()
    }

    fun refreshAdapter(newSubjects: List<Subject>) {

        subjects = newSubjects
        if (adapter == null) initValue()
        else {
            adapter!!.setMainListItems(utils!!.getFilteredSubjects(newSubjects))
            adapterSetFabOnClickListener(adapter!!)
            adapter!!.notifyDataSetChanged()
        }
        setRefreshing(false)
    }

    fun notifyAdapter() {
        adapter!!.notifyDataSetChanged()
    }

    private fun getItemViewByPosition(position: Int, listView: ListView): View {

        val firstItemPos = listView.firstVisiblePosition
        val lastItemPos = listView.lastVisiblePosition
        if (position < firstItemPos || position > lastItemPos) return listView.adapter.getView(position, null, listView)
        else return listView.getChildAt(position - firstItemPos)
    }

    private fun gotoCourseDetail(_header: View, _subject_title: View, transformedPosition: Int) {

        courseDetailFragment = CourseDetailFragment()
        courseDetailFragment!!.sharedElementEnterTransition = DetailsTransition()
        courseDetailFragment!!.sharedElementReturnTransition = DetailsTransition()
        _header.transitionName = getString(R.string.shared_element_course_header)
        _subject_title.transitionName = getString(R.string.shared_element_course_subject_title)
        val bundle = Bundle()
        bundle.putInt("transformedPosition", transformedPosition)
        courseDetailFragment!!.arguments = bundle

        activity.fragmentManager
                .beginTransaction()
                .addSharedElement(_header, getString(R.string.shared_element_course_header))
                .addSharedElement(_subject_title, getString(R.string.shared_element_course_subject_title))
                .setCustomAnimations(R.animator.do_nothing, R.animator.fade_out)
                .replace(R.id.content_view, courseDetailFragment)
                .addToBackStack(null)
                .commit()

        MainActivity.of(activity).animateDrawerToggle(true)
        MainActivity.of(activity).setToolBarElevation(0)
    }

    private fun preRenderUnfoldCells() {
        (getItemViewByPosition(transformedPosition, dashboardListView) as FoldingCell).toggle(true)
    }

    override fun onAfterEnter() {

        super.onAfterEnter()
        preRenderUnfoldCells()
    }
}