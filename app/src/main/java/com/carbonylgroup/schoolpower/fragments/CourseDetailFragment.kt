/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView

import java.util.ArrayList

import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.MainActivity
import com.carbonylgroup.schoolpower.classes.Adapter.CourseDetailAdapter
import com.carbonylgroup.schoolpower.classes.ListItems.MainListItem
import com.carbonylgroup.schoolpower.classes.Transition.TransitionHelper
import com.carbonylgroup.schoolpower.classes.Utils.Utils


class CourseDetailFragment : TransitionHelper.BaseFragment() {

    private var view_private: View? = null
    private var utils: Utils? = null
    private var itemToPresent: MainListItem? = null
    private var offset_up_from_bottom: Animation? = null
    private var course_detail_recycler: RecyclerView? = null
    private var dataList: ArrayList<MainListItem>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle): View? {

        view_private = inflater.inflate(R.layout.course_detail_view_content, container, false)

        initValue()
        initAnim()

        return view_private
    }

    private fun initValue() {

        utils = Utils(activity)

        MainActivity.of(activity).presentFragment = 1
        MainActivity.of(activity).setToolBarTitle("")
        MainActivity.of(activity).expandToolBar(true, true)

        val bundle = this.arguments
        val transformedPosition = bundle.getInt("transformedPosition", -1)

        if (transformedPosition != -1) {

            itemToPresent = MainActivity.of(activity).mainListItemTransporter
            view_private!!.findViewById(R.id.detail_view_header).setBackgroundColor(utils!!.getColorByLetterGrade(activity, itemToPresent!!.letterGrade))
            (view_private!!.findViewById(R.id.detail_subject_title_tv) as TextView).text = itemToPresent!!.subjectTitle
            MainActivity.of(activity).setToolBarColor(utils!!.getColorByLetterGrade(activity, itemToPresent!!.letterGrade), true)

            course_detail_recycler = view_private!!.findViewById(R.id.course_detail_recycler) as RecyclerView
            dataList = MainActivity.of(activity).dataList
            val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            val adapter = CourseDetailAdapter(activity, dataList!![transformedPosition])
            course_detail_recycler!!.layoutManager = layoutManager
            course_detail_recycler!!.adapter = adapter
        }
    }

    private fun initAnim() {

        offset_up_from_bottom = AnimationUtils.loadAnimation(activity, R.anim.offset_up_from_bottom)
    }
}
