/**
 * Copyright (C) 2017 Gustav Wang
 */

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
import com.carbonylgroup.schoolpower.data.Subject
import com.carbonylgroup.schoolpower.utils.Utils
import kotterknife.bindView
import java.util.*

class CourseDetailAdapter(private val context: Context, private val subject: Subject) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var presentingTermPos = 0
    private val utils: Utils = Utils(context)
    private val termsList: ArrayList<String> = ArrayList()
    private var list: ArrayList<AssignmentItem>? = null

    init {
        initTermList()
        setAllTerms()
    }

    private fun initTermList() {

        termsList.add(context.getString(R.string.all_terms))
        subject.grades.keys.mapTo(termsList) { it }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == HEADER_VIEW) return HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.course_detail_header, parent, false))
        if (viewType == FOOTER_VIEW) return FooterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.course_detail_footer, parent, false))
        return NormalViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.course_detail_assignment_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            if (holder is NormalViewHolder) {

                val normalViewHolder = holder

                Collections.sort(list!!) { o1, o2 -> o2.date.compareTo(o1.date) }

                val assignmentItem = list!![position - 1]
                normalViewHolder.detail_assignment_name_tv.text = assignmentItem.title
                normalViewHolder.detail_assignment_date_tv.text = assignmentItem.date
                normalViewHolder.detail_assignment_percentage_tv.text = assignmentItem.percentage
                normalViewHolder.detail_assignment_dividing_score_tv.text = assignmentItem.getDividedScore()
                normalViewHolder.detail_assignment_grade_background.setBackgroundColor(utils.getColorByLetterGrade(context, assignmentItem.letterGrade))
                if (assignmentItem.isNew) {
                    normalViewHolder.detail_header_background.setBackgroundColor(ContextCompat.getColor(context, R.color.accent))
                    normalViewHolder.detail_assignment_name_tv.setTextColor(ContextCompat.getColor(context, R.color.white))
                    normalViewHolder.detail_assignment_date_tv.setTextColor(ContextCompat.getColor(context, R.color.white_0_20))
                }else{
                    normalViewHolder.detail_header_background.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                    normalViewHolder.detail_assignment_name_tv.setTextColor(ContextCompat.getColor(context, R.color.text_primary_black))
                    normalViewHolder.detail_assignment_date_tv.setTextColor(ContextCompat.getColor(context, R.color.text_tertiary_black))
                }
                normalViewHolder.detail_header_background.setOnLongClickListener {
                    if(context.getSharedPreferences("other", Activity.MODE_PRIVATE).getBoolean("developer_mode", false)){
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("Developer Mode")
                        builder.setMessage("Name: ${assignmentItem.title}\nWeight: ${assignmentItem.weight}\nInclude in final grade: ${assignmentItem.includeInFinalGrade}")
                        builder.setPositiveButton("Okay", null)
                        builder.show()
                    }
                    true
                }

            } else if (holder is HeaderViewHolder) {

                val headerViewHolder = holder
                val termAdapter = ArrayAdapter(context, R.layout.term_selection_spinner, termsList)
                val period = utils.getLatestPeriodGrade(subject) ?: Subject.Grade("--", "--")

                headerViewHolder.detail_letter_grade_tv.text = period.letter
                headerViewHolder.detail_percentage_grade_tv.text = period.percentage
                headerViewHolder.detail_header_teacher_name_tv.text = subject.teacherName
                headerViewHolder.detail_header_block_tv.text = context.getString(R.string.block) + " " + subject.blockLetter
                headerViewHolder.detail_header_room_tv.text = context.getString(R.string.room) + " " + subject.roomNumber
                headerViewHolder.detail_header_grade_background.setBackgroundColor(utils.getColorByLetterGrade(context, period.letter))
                headerViewHolder.detail_term_select_spinner.adapter = termAdapter
                headerViewHolder.detail_term_select_spinner.setSelection(presentingTermPos)
                headerViewHolder.detail_term_select_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                        val selectItem = termsList[pos]
                        if (pos != presentingTermPos) {
                            presentingTermPos = pos
                            if (selectItem == context.getString(R.string.all_terms)) setAllTerms()
                            else setTerm(selectItem)
                            refreshAdapter()
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }
    }

    override fun getItemCount(): Int {

        if (list == null || list!!.isEmpty()) return 2
        return list!!.size + 2
    }

    override fun getItemViewType(position: Int): Int {

        if (position == 0) return HEADER_VIEW
        if (position == itemCount - 1) return FOOTER_VIEW
        return super.getItemViewType(position)
    }

    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    inner class FooterViewHolder(itemView: View) : ViewHolder(itemView)
    inner class HeaderViewHolder(itemView: View) : ViewHolder(itemView) {
        val detail_letter_grade_tv: TextView by bindView(R2.id.detail_letter_grade_tv)
        val detail_percentage_grade_tv: TextView by bindView(R2.id.detail_percentage_grade_tv)
        val detail_header_teacher_name_tv: TextView by bindView(R2.id.detail_header_teacher_name_tv)
        val detail_header_block_tv: TextView by bindView(R2.id.detail_header_block_tv)
        val detail_header_room_tv: TextView by bindView(R2.id.detail_header_room_tv)
        val detail_header_grade_background: RelativeLayout by bindView(R2.id.detail_header_grade_background)
        val detail_term_select_spinner: Spinner by bindView(R2.id.detail_term_select_spinner)
    }
    inner class NormalViewHolder(itemView: View) : ViewHolder(itemView) {
        val detail_assignment_name_tv: TextView by bindView(R2.id.detail_assignment_name_tv)
        val detail_assignment_date_tv: TextView by bindView(R2.id.detail_assignment_date_tv)
        val detail_assignment_percentage_tv: TextView by bindView(R2.id.detail_assignment_percentage_tv)
        val detail_assignment_dividing_score_tv: TextView by bindView(R2.id.detail_assignment_dividing_score_tv)
        val detail_assignment_grade_background: RelativeLayout by bindView(R2.id.detail_assignment_grade_background)
        val detail_header_background: RelativeLayout by bindView(R2.id.detail_header_background)
    }

    private fun refreshAdapter() {
        this.notifyDataSetChanged()
    }

    private fun setTerm(term: String) {
        if (term == "ANY") {
            list = subject.assignments
        } else {
            list = ArrayList()
            subject.assignments
                    .filter { it.terms.contains(term) }
                    .forEach { list!!.add(it) }
        }
    }

    private fun setAllTerms() {

        setTerm("ANY")
    }

    companion object {

        private val HEADER_VIEW = 1
        private val FOOTER_VIEW = 2
    }
}