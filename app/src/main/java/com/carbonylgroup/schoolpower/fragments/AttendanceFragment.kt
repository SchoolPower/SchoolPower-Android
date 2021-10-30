package com.carbonylgroup.schoolpower.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.res.ResourcesCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
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
    private lateinit var no_attendance_view: LinearLayout
    private var attendance_swipe_refresh_layout: SwipeRefreshLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        utils = Utils(activity!!)
        MainActivity.of(activity).expandToolBar(true, true)
        view_private = inflater.inflate(R.layout.fragment_attendance, container, false)

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

        utils = Utils(activity!!)
        attendance = MainActivity.of(activity).attendances
        attendanceRecyclerView = view_private!!.findViewById(R.id.attendance_recycler)
        no_attendance_view = view_private!!.findViewById(R.id.no_attendance_view)
        view_private!!.findViewById<ImageView>(R.id.perfect_attendance_image_view).setImageDrawable(
                ResourcesCompat.getDrawable(resources,
                        when (utils.getTheme()) {
                            Utils.LIGHT -> R.drawable.perfect_attendance
                            Utils.DARK -> R.drawable.perfect_attendance_dark
                            else -> R.drawable.perfect_attendance
                        }, null)
        )
        attendance_swipe_refresh_layout = view_private!!.findViewById(R.id.attendance_swipe_refresh_layout)
        attendance_swipe_refresh_layout!!.setColorSchemeColors(utils.getAccentColor())
        attendance_swipe_refresh_layout!!.setOnRefreshListener { MainActivity.of(activity).fetchStudentDataFromServer() }

        if (attendance == null || attendance!!.count() == 0) refreshAdapterToEmpty()
        else initAdapter()
    }

    private fun initAdapter() {

        if (attendance != null && attendance!!.count() != 0) {
            attendanceRecyclerView.visibility = View.VISIBLE
            no_attendance_view.visibility = View.GONE
        }
        attendanceRecyclerView.layoutManager =
            LinearLayoutManager(
                activity,
                LinearLayoutManager.VERTICAL,
                false
            )
//        Handler().postDelayed({
            attendanceRecyclerView.adapter = AttendanceAdapter(activity as MainActivity, attendance)
//        }, 500)
    }

    fun visiblizeNoAttendanceView() {
        attendanceRecyclerView.visibility = View.GONE
        no_attendance_view.visibility = View.VISIBLE
    }

    fun setRefreshing(isRefreshing: Boolean) {
        attendance_swipe_refresh_layout?.isRefreshing = isRefreshing
    }

    fun refreshAdapterToEmpty() {

        attendance = arrayListOf()
        setRefreshing(false)
        visiblizeNoAttendanceView()
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