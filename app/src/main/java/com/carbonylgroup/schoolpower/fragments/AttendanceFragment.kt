package com.carbonylgroup.schoolpower.fragments

import android.app.Fragment
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.MainActivity
import com.carbonylgroup.schoolpower.adapter.AttendanceAdapter
import com.carbonylgroup.schoolpower.data.Attendance
import com.carbonylgroup.schoolpower.utils.Utils


class AttendanceFragment : Fragment() {

    private lateinit var utils: Utils
    private var view_private: View? = null
    private var adapter: AttendanceAdapter? = null
    private var attendance: List<Attendance>? = null
    private lateinit var attendanceRecyclerView: RecyclerView
    private var attendance_swipe_refresh_layout: SwipeRefreshLayout? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        utils = Utils(activity)
        MainActivity.of(activity).expandToolBar(true, true)
        view_private = inflater!!.inflate(R.layout.fragment_attendance, container, false)

        initValue()

        return view_private
    }

    override fun onPause() {

        super.onPause()
        //To prevent freezing during fragment transaction
        if (attendance_swipe_refresh_layout != null) {
            attendance_swipe_refresh_layout!!.isRefreshing = false
            attendance_swipe_refresh_layout!!.destroyDrawingCache()
            attendance_swipe_refresh_layout!!.clearAnimation()
        }
    }

    private fun initValue() {

        utils = Utils(activity)
        attendance = MainActivity.of(activity).attendances!!
        attendanceRecyclerView = view_private!!.findViewById(R.id.attendance_recycler)
        attendance_swipe_refresh_layout = view_private!!.findViewById(R.id.attendance_swipe_refresh_layout)
        attendance_swipe_refresh_layout!!.setColorSchemeResources(R.color.accent, R.color.A_score_green, R.color.B_score_green,
                R.color.Cp_score_yellow, R.color.C_score_orange, R.color.Cm_score_red, R.color.primary)
        attendance_swipe_refresh_layout!!.setOnRefreshListener { MainActivity.of(activity).fetchStudentDataFromServer() }

        if (attendance == null || attendance!!.count() == 0) refreshAdapterToEmpty()
        else initAdapter()
    }

    private fun initAdapter() {

        if (attendance != null && attendance!!.count() != 0) attendanceRecyclerView.visibility = View.VISIBLE
        attendanceRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        attendanceRecyclerView.adapter = AttendanceAdapter(activity, attendance)
    }

    fun invisiblizeRecyclerView() {
        attendanceRecyclerView.visibility = View.GONE
    }

    fun setRefreshing(isRefreshing: Boolean) {
        attendance_swipe_refresh_layout?.isRefreshing = isRefreshing
    }

    fun refreshAdapterToEmpty() {

        attendance = arrayListOf()
        setRefreshing(false)
        invisiblizeRecyclerView()
    }

    fun refreshAdapter(newAttendance: List<Attendance>) {

        attendance = newAttendance
        if (adapter == null) initValue()
        else {
            adapter!!.setAttendanceItems(newAttendance)
            adapter!!.notifyDataSetChanged()
        }
        setRefreshing(false)
    }

    fun notifyAdapter() {
        adapter!!.notifyDataSetChanged()
    }
}