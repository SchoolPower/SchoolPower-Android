/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.fragments

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.ScaleAnimation
import android.widget.AdapterView
import android.widget.ListView
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.MainActivity
import com.carbonylgroup.schoolpower.classes.Adapter.FoldingCellListAdapter
import com.carbonylgroup.schoolpower.classes.Data.Subject
import com.carbonylgroup.schoolpower.classes.Transition.DetailsTransition
import com.carbonylgroup.schoolpower.classes.Transition.TransitionHelper
import com.carbonylgroup.schoolpower.classes.Utils.Utils
import com.ramotion.foldingcell.FoldingCell
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
    private lateinit var theListView: ListView

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

        utils = Utils(activity)
        subjects = MainActivity.of(activity).subjects
        MainActivity.of(activity).presentFragment = 0
        MainActivity.of(activity).setToolBarElevation(utils!!.dpToPx(10))
        MainActivity.of(activity).setToolBarTitle(getString(R.string.dashboard))
        theListView = view_private!!.findViewById(R.id.mainListView)
        home_swipe_refresh_layout = view_private!!.findViewById(R.id.home_swipe_refresh_layout)
        home_swipe_refresh_layout!!.setColorSchemeResources(R.color.accent, R.color.A_score_green, R.color.B_score_green,
                R.color.Cp_score_yellow, R.color.C_score_orange, R.color.Cm_score_red, R.color.primary)
        home_swipe_refresh_layout!!.setOnRefreshListener { MainActivity.of(activity).initDataJson() }
        if (subjects == null || subjects!!.count() == 0) refreshAdapterToEmpty()
        else initAdapter()
    }

    private fun getFilteredSubjects(subjects: List<Subject>) : List<Subject>{
        val filteredSubjects : List<Subject>
        if (PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("list_preference_dashboard_hide_inactive", false)) {

            filteredSubjects = ArrayList<Subject>()
            subjects
                    .filter { it.assignments.size != 0 || it.grades.size != 0 }
                    .forEach { filteredSubjects.add(it) }

        }else{
            filteredSubjects = subjects
        }
        return filteredSubjects
    }
    private fun initAdapter() {

        if (subjects != null && subjects!!.count() != 0) theListView.visibility = View.VISIBLE

        adapter = FoldingCellListAdapter(activity, getFilteredSubjects(subjects!!), unfoldedIndexesBackUp, transformedPosition)
        adapter!!.setDefaultRequestBtnClickListener(View.OnClickListener { v ->
            MainActivity.of(activity).subjectTransporter = subjects!![theListView.getPositionForView(v)]
            if (transformedPosition != -1) {
                val itemView = getItemViewByPosition(transformedPosition, theListView)
                itemView.findViewById<View>(R.id.unfold_header_view).transitionName = ""
                itemView.findViewById<View>(R.id.detail_subject_title_tv).transitionName = ""
            }
            transformedPosition = theListView.getPositionForView(v)
            val itemView = getItemViewByPosition(theListView.getPositionForView(v), theListView)
            itemView.findViewById<View>(R.id.floating_action_button).startAnimation(fab_out)
            itemView.findViewById<View>(R.id.floating_action_button).visibility = View.GONE
            gotoCourseDetail(itemView.findViewById(R.id.unfold_header_view), itemView.findViewById(R.id.detail_subject_title_tv), transformedPosition)
        })
        theListView.onItemClickListener = AdapterView.OnItemClickListener { _, view, pos, _ ->
            adapter!!.registerToggle(pos)
            (view as FoldingCell).toggle(false)
            adapter!!.refreshPeriodRecycler(view, pos)
            unfoldedIndexesBackUp = adapter!!.unfoldedIndexes
        }
        theListView.adapter = adapter
    }

    fun invisiblizeListView() {
        theListView.visibility = View.GONE
    }

    fun setRefreshing(isRefreshing: Boolean) {
        home_swipe_refresh_layout!!.isRefreshing = isRefreshing
    }

    fun refreshAdapterToEmpty() {

        subjects = arrayListOf()
        setRefreshing(false)
        invisiblizeListView()
    }

    fun refreshAdapter(newSubjects: List<Subject>) {
        subjects = newSubjects
        if (adapter == null) initValue()
        adapter!!.setMainListItems(getFilteredSubjects(newSubjects))
        adapter!!.notifyDataSetChanged()
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

        MainActivity.of(activity).presentFragment = 1
        MainActivity.of(activity).animateDrawerToggle(true)
        MainActivity.of(activity).setToolBarElevation(0)
    }

    private fun preRenderUnfoldCells() {

        for (i in 0..9) (getItemViewByPosition(transformedPosition, theListView) as FoldingCell).toggle(false)
    }

    override fun onAfterEnter() {

        super.onAfterEnter()
        preRenderUnfoldCells()
    }
}