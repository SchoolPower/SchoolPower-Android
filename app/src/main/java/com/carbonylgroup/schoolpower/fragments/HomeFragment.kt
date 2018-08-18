/**
 * Copyright (C) 2018 SchoolPower Studio
 */

package com.carbonylgroup.schoolpower.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v4.content.ContextCompat
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
import com.carbonylgroup.schoolpower.data.ILDNotification
import com.carbonylgroup.schoolpower.data.Subject
import com.carbonylgroup.schoolpower.transition.DetailsTransition
import com.carbonylgroup.schoolpower.transition.TransitionHelper
import com.carbonylgroup.schoolpower.utils.CompositeOnClickListener
import com.carbonylgroup.schoolpower.utils.Utils
import com.ramotion.foldingcell.FoldingCell
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class HomeFragment : TransitionHelper.BaseFragment() {

    private var utils: Utils? = null
    private var transformedPosition = -1
    private var viewPrivate: View? = null
    private var fabIn: ScaleAnimation? = null
    private var fabOut: ScaleAnimation? = null
    private var subjects: List<Subject>? = null
    private var allILDs: ArrayList<ViewGroup> = ArrayList()
    private var unfoldedIndexesBackUp = HashSet<Int>()
    private var adapter: FoldingCellListAdapter? = null
    private var courseDetailFragment: CourseDetailFragment? = null
    private var homeSwipeRefreshLayout: SwipeRefreshLayout? = null
    private lateinit var dashboardListView: ListView
    private lateinit var noGradeView: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewPrivate = inflater.inflate(R.layout.home_view_content, container, false)
        initAnim()
        initValue()
        return viewPrivate
    }

    override fun onPause() {
        super.onPause()
        //To prevent freezing during fragment transaction
        if (homeSwipeRefreshLayout != null) {
            homeSwipeRefreshLayout!!.isRefreshing = false
            homeSwipeRefreshLayout!!.destroyDrawingCache()
            homeSwipeRefreshLayout!!.clearAnimation()
        }
    }

    private fun initAnim() {
        fabIn = ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        fabIn!!.duration = 200
        fabIn!!.interpolator = DecelerateInterpolator()
        fabOut = ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        fabOut!!.duration = 200
        fabOut!!.interpolator = DecelerateInterpolator()
    }

    private fun initValue() {
        if (activity == null) return
        utils = Utils(MainActivity.of(activity))
        subjects = MainActivity.of(activity).subjects
        allILDs = arrayListOf()
        MainActivity.of(activity).presentFragment = 0
        MainActivity.of(activity).setToolBarElevation()
        MainActivity.of(activity).setToolBarTitle(getString(R.string.dashboard))
        dashboardListView = viewPrivate!!.findViewById(R.id.mainListView)
        noGradeView = viewPrivate!!.findViewById(R.id.no_grade_view)
        viewPrivate!!.findViewById<ImageView>(R.id.no_grade_image_view).setImageDrawable(
                getDrawable(resources,
                        when (utils!!.getTheme()) {
                            utils!!.LIGHT -> R.drawable.no_grades
                            utils!!.DARK -> R.drawable.no_grades_dark
                            else -> R.drawable.no_grades
                        }, null)
        )
        homeSwipeRefreshLayout = viewPrivate!!.findViewById(R.id.home_swipe_refresh_layout)
        homeSwipeRefreshLayout!!.setColorSchemeColors(utils!!.getAccentColor())
        homeSwipeRefreshLayout!!.setOnRefreshListener { MainActivity.of(activity).fetchStudentDataFromServer() }
        if (subjects == null || utils!!.getFilteredSubjects(subjects!!).count() == 0) refreshAdapterToEmpty()
        else try {
            initAdapter()
        } catch (e: Exception) {
            utils!!.errorHandler(e)
        }

        val po = View.OnClickListener {
            MainActivity.of(activity).gotoFragmentWithMenuItemId(R.id.nav_support)
            utils!!.setSharedPreference("Tmp", "ImComingForDonation", true)
            setLastDonateShowedDate(Date())
        }
        val ps = View.OnClickListener {
            MainActivity.of(activity).gotoFragmentWithMenuItemId(R.id.nav_support)
            setLastDonateShowedDate(Date())
        }
        val pd = View.OnClickListener {
            setLastDonateShowedDate(Date())
        }
        if (needToShowDonate())
        // If other ILDs are being displayed, don't show the donation
            if (dashboardListView.headerViewsCount == 0)
                initInListDialog(
                        "",
                        ContextCompat.getDrawable(activity, R.drawable.ic_donation)!!,
                        getString(R.string.donation_title),
                        getString(R.string.donation_message),
                        getString(R.string.donation_ok),
                        getString(R.string.donation_promote),
                        getString(R.string.donation_cancel),
                        false, false, false,
                        po, ps, pd
                )
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
        itemView.findViewById<View>(R.id.floating_action_button).startAnimation(fabOut)
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
            noGradeView.visibility = View.GONE
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
        fetchLocalILD()
    }

    /**
     * Generates an InListDialog with maximum of three buttons
     * as the header of the dashboard listView
     *
     * Primary button is mandatorily shown
     *
     * When each button is clicked, dialog will be automatically dismissed after
     * executing the assigned OnClickListener
     *
     * @param uuid used for identification when used as push notification
     * @param headerImage
     * @param title
     * @param message
     * @param primaryText
     * @param secondaryText
     * @param dismissText
     * @param hideDismiss if true - dismiss button will not be shown
     * @param hideSecondary if true - secondary button will not be shown
     * @param onlyOnce if true - the uuid will be saved in sharedprefs when dismissed and won't show up again
     * @param primaryOnClickListener
     * @param secondaryOnClickListener
     * @param dismissOnClickListener
     */
    fun initInListDialog(
            uuid: String,
            headerImage: Drawable,
            title: String,
            message: String,
            primaryText: String,
            secondaryText: String,
            dismissText: String,
            hideDismiss: Boolean,
            hideSecondary: Boolean,
            onlyOnce: Boolean,
            primaryOnClickListener: View.OnClickListener,
            secondaryOnClickListener: View.OnClickListener,
            dismissOnClickListener: View.OnClickListener
    ) {
        val inflater = activity.layoutInflater
        val self = inflater.inflate(R.layout.in_list_dialog, dashboardListView, false) as ViewGroup

        self.findViewById<ImageView>(R.id.ild_image_view).setImageDrawable(headerImage)

        self.findViewById<TextView>(R.id.ild_title).text = title
        self.findViewById<TextView>(R.id.ild_message).text = message

        self.findViewById<Button>(R.id.ild_primary_button).text = primaryText
        self.findViewById<Button>(R.id.ild_secondary_button).text = secondaryText
        self.findViewById<Button>(R.id.ild_dismiss_button).text = dismissText

        val po = CompositeOnClickListener()
        val ps = CompositeOnClickListener()
        val pd = CompositeOnClickListener()
        val dismiss = View.OnClickListener {
            if (onlyOnce) {
                // mark the ILD as displayed
                val displayedILDs = utils!!.getSharedPreference("Tmp").getStringSet("doNotDisplayTheseILDs", mutableSetOf())
                displayedILDs.add(uuid)
                utils!!.setSharedPreference("Tmp", "doNotDisplayTheseILDs", displayedILDs)
                utils!!.setSharedPreference("Tmp", "ildJson", "")
            }
            removeILD(self)
        }
        po.addOnClickListener(primaryOnClickListener)
        ps.addOnClickListener(secondaryOnClickListener)
        pd.addOnClickListener(dismissOnClickListener)
        po.addOnClickListener(dismiss)
        ps.addOnClickListener(dismiss)
        pd.addOnClickListener(dismiss)
        self.findViewById<Button>(R.id.ild_primary_button).setOnClickListener(po)
        if (!hideSecondary) {
            self.findViewById<Button>(R.id.ild_secondary_button).visibility = View.VISIBLE
            self.findViewById<Button>(R.id.ild_secondary_button).setOnClickListener(ps)
        }
        if (!hideDismiss) {
            self.findViewById<Button>(R.id.ild_dismiss_button).visibility = View.VISIBLE
            self.findViewById<Button>(R.id.ild_dismiss_button).setOnClickListener(pd)
        }
        addILD(self)
    }

    private fun addILD(ild: ViewGroup) {
        TransitionManager.beginDelayedTransition(dashboardListView)
        dashboardListView.addHeaderView(ild, null, false)
        allILDs.add(ild)
    }

    fun fetchLocalILD() {
        val json = utils!!
                .getSharedPreference("Tmp")
                .getString("ildJson", "")
        if (json.contains("{")) {
            val data = ILDNotification(json)
            if (data.show) MainActivity.of(activity).showILD(data)
        }
    }

    private fun removeILD(ild: ViewGroup) {
        TransitionManager.beginDelayedTransition(dashboardListView)
        dashboardListView.removeHeaderView(ild)
        allILDs.remove(ild)
    }

    fun removeAllILD() {
        for (ild in allILDs) removeILD(ild)
    }

    private fun needToShowDonate(): Boolean {
        // Show donate every 30 days, if haven't donated
        return if (isDonated()) false
        else ((Date().time - getLastDonateShowedDate().time) / 1000.0 / 60.0 / 60.0 / 24.0 >= 30.0)
//        return true
    }

    private fun isDonated(): Boolean {
        return Utils(activity).getSharedPreference("Tmp").getBoolean("Donated", false)
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

    private fun visibleNoGradeView() {
        dashboardListView.visibility = View.GONE
        noGradeView.visibility = View.VISIBLE
    }

    fun setRefreshing(isRefreshing: Boolean) {
        homeSwipeRefreshLayout?.isRefreshing = isRefreshing
    }

    fun refreshAdapterToEmpty() {
        subjects = arrayListOf()
        setRefreshing(false)
        visibleNoGradeView()
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
        return if (position < firstItemPos || position > lastItemPos) listView.adapter.getView(position, null, listView)
        else listView.getChildAt(position - firstItemPos)
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
