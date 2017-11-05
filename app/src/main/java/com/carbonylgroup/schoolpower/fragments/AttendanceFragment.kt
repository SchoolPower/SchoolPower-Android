package com.carbonylgroup.schoolpower.fragments

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.MainActivity
import com.carbonylgroup.schoolpower.adapter.AttendanceAdapter
import com.carbonylgroup.schoolpower.utils.Utils


class AttendanceFragment : Fragment() {

    private lateinit var utils: Utils

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.fragment_attendance, container, false)
        MainActivity.of(activity).expandToolBar(true, true)
        utils = Utils(activity)

        val attendanceRecycler = view.findViewById<RecyclerView>(R.id.attendance_recycler)
        attendanceRecycler.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        attendanceRecycler.adapter = AttendanceAdapter(activity, MainActivity.of(activity).attendances!!)

        return view
    }
}