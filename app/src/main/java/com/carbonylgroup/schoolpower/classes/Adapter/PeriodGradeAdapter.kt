/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.classes.Adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

import java.util.ArrayList

import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.R2
import com.carbonylgroup.schoolpower.classes.ListItems.PeriodGradeItem
import com.carbonylgroup.schoolpower.classes.Utils.Utils
import kotterknife.bindView

class PeriodGradeAdapter(private val context: Context, objects: ArrayList<PeriodGradeItem>) : RecyclerView.Adapter<PeriodGradeAdapter.PeriodGradeViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val list: List<PeriodGradeItem> = objects
    private val utils: Utils = Utils(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeriodGradeViewHolder {

        val view = inflater.inflate(R.layout.period_grade_list_item, parent, false)
        return PeriodGradeViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: PeriodGradeViewHolder, position: Int) {

        val item = list[position]
        viewHolder.period_indicator.text = item.termIndicator
        viewHolder.period_latter_grade.text = item.termLetterGrade
        viewHolder.period_percentage_grade.text = item.termPercentageGrade
        viewHolder.period_grade_background.setBackgroundColor(utils.getColorByPeriodItem(context, item))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class PeriodGradeViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        val period_indicator: TextView by bindView(R2.id.period_indicator)
        val period_latter_grade: TextView by bindView(R2.id.period_latter_grade)
        val period_percentage_grade: TextView by bindView(R2.id.period_percentage_grade)
        val period_grade_background: LinearLayout by bindView(R2.id.period_grade_background)
    }
}