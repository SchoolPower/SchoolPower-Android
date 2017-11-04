package com.carbonylgroup.schoolpower.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.R2
import com.carbonylgroup.schoolpower.data.AssignmentItem
import com.carbonylgroup.schoolpower.data.Attendance
import com.carbonylgroup.schoolpower.data.Subject
import com.carbonylgroup.schoolpower.utils.Utils
import kotterknife.bindView
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by carbonyl on 04/11/2017.
 */
class AttendanceAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val utils: Utils = Utils(context)
    private var attendanceList: ArrayList<Attendance> = ArrayList()

    init {
        initAttendanceList()
    }

    private fun initAttendanceList() {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AttendanceHolder(LayoutInflater.from(parent.context).inflate(R.layout.attendance_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is AttendanceHolder) {

            val AttendanceHolder = holder
            Collections.sort(attendanceList) { o1, o2 -> o2.date.compareTo(o1.date) }
            val attendanceItem = attendanceList[position]
            AttendanceHolder.attendance_code_tv.text = attendanceItem.code
            AttendanceHolder.attendance_description_tv.text = attendanceItem.description
            AttendanceHolder.attendance_subject_tv.text = attendanceItem.subject
            AttendanceHolder.attendance_date_tv.text = attendanceItem.date
//            AttendanceHolder.attendance_code_background.setBackgroundColor(utils.getColorByLetterGrade(context, attendanceItem.letterGrade))
        }
    }

    override fun getItemCount(): Int {

        return attendanceList.size
    }

    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class AttendanceHolder(itemView: View) : ViewHolder(itemView) {

        val attendance_code_tv: TextView by bindView(R2.id.attendance_code_tv)
        val attendance_description_tv: TextView by bindView(R2.id.attendance_description_tv)
        val attendance_subject_tv: TextView by bindView(R2.id.attendance_subject_tv)
        val attendance_date_tv: TextView by bindView(R2.id.attendance_date_tv)
        val attendance_code_background: RelativeLayout by bindView(R2.id.attendance_code_background)
    }

    private fun refreshAdapter() {
        this.notifyDataSetChanged()
    }
}