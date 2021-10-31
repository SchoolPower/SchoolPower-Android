/**
 * Copyright (C) 2019 SchoolPower Studio
 */

package com.carbonylgroup.schoolpower.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.data.Grade
import com.carbonylgroup.schoolpower.utils.Utils
import kotterknife.bindView

class PeriodGradeAdapter(private val context: Context, objects: Map<String, Grade>) : RecyclerView.Adapter<PeriodGradeAdapter.PeriodGradeViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val gradeMap: Map<String, Grade> = objects
    private val utils: Utils = Utils(context)
    private val keys = objects.keys.toTypedArray()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeriodGradeViewHolder {

        val view = inflater.inflate(R.layout.period_grade_list_item, parent, false)
        return PeriodGradeViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: PeriodGradeViewHolder, position: Int) {

        val item = gradeMap[keys[position]]!!
        viewHolder.period_indicator.text = keys[position]
        viewHolder.period_latter_grade.text = item.letter
        viewHolder.period_percentage_grade.text = item.getPercentageString()
        viewHolder.period_grade_background.setBackgroundColor(utils.getColorByLetterGrade(item.letter))
    }

    override fun getItemCount(): Int {
        return gradeMap.size
    }

    class PeriodGradeViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        val period_indicator: TextView by bindView(R.id.period_indicator)
        val period_latter_grade: TextView by bindView(R.id.period_latter_grade)
        val period_percentage_grade: TextView by bindView(R.id.period_percentage_grade)
        val period_grade_background: LinearLayout by bindView(R.id.period_grade_background)
    }
}