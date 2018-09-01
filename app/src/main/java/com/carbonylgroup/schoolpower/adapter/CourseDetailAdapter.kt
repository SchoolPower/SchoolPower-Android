/**
 * Copyright (C) 2018 SchoolPower Studio
 */

package com.carbonylgroup.schoolpower.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.*
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.data.AssignmentItem
import com.carbonylgroup.schoolpower.data.Subject
import com.carbonylgroup.schoolpower.utils.Utils
import kotterknife.bindView
import java.util.*

@SuppressLint("ClickableViewAccessibility")
class CourseDetailAdapter(private val context: Context, private val subject: Subject) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var presentingTermPos = 0
    private val utils: Utils = Utils(context)
    private var termsList: ArrayList<String> = ArrayList()
    private var list: ArrayList<AssignmentItem>? = null

    init {
        initTermList()
        setAllTerms()
    }

    private fun initTermList() {
        subject.grades.keys.mapTo(termsList) { it }
        termsList = utils.sortTerm(termsList)
        termsList.add(0, context.getString(R.string.all_terms))
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
            normalViewHolder.detail_assignment_flag_rv.adapter = DetailAssignmentFlagAdapter(context, assignmentItem)
            normalViewHolder.detail_assignment_grade_background.setBackgroundColor(utils.getColorByLetterGrade(assignmentItem.letterGrade))
            normalViewHolder.detail_assignment_flag_rv.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            if (assignmentItem.isNew) {
                normalViewHolder.detail_header_background.setBackgroundColor(utils.getAccentColor())
                normalViewHolder.detail_assignment_name_tv.setTextColor(ContextCompat.getColor(context, R.color.white))
                normalViewHolder.detail_assignment_date_tv.setTextColor(ContextCompat.getColor(context, R.color.white_0_20))
            } else {
                normalViewHolder.detail_header_background.setBackgroundColor(utils.getCardBackground())
                normalViewHolder.detail_assignment_name_tv.setTextColor(utils.getPrimaryTextColor())
                normalViewHolder.detail_assignment_date_tv.setTextColor(utils.getSecondaryTextColor())
            }
            normalViewHolder.detail_header_background.setOnClickListener {
                showAssignmentDialog(assignmentItem)
            }
            normalViewHolder.detail_assignment_flag_rv.setOnTouchListener { view, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_UP)
                    showAssignmentDialog(assignmentItem)
                false
            }

        } else if (holder is HeaderViewHolder) {

            val termAdapter = ArrayAdapter(context, R.layout.term_selection_spinner, termsList)
            val period = utils.getLatestPeriodGrade(subject) ?: Subject.Grade("--", "--", "null", "--")

            holder.detail_letter_grade_tv.text = period.letter
            holder.detail_percentage_grade_tv.text = period.percentage
            holder.detail_header_teacher_name_tv.text = subject.teacherName
            holder.detail_header_block_tv.text = context.getString(R.string.block) + " " + subject.blockLetter
            holder.detail_header_room_tv.text = context.getString(R.string.room) + " " + subject.roomNumber
            holder.detail_header_grade_background.setBackgroundColor(utils.getColorByLetterGrade(period.letter))
            holder.detail_term_select_spinner.adapter = termAdapter
            holder.detail_term_select_spinner.setSelection(presentingTermPos)
            holder.detail_term_select_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
            if (subject.teacherEmail == "null") {
                holder.detail_header_email.visibility = GONE
            } else {
                holder.detail_header_email.setOnClickListener {
                    val uri = Uri.parse("mailto:" + subject.teacherEmail)
                    val intent = Intent(Intent.ACTION_SENDTO, uri)
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.choose_email_app)))
                }
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

    fun showAssignmentDialog(assignmentItem: AssignmentItem) {

        val assignmentDialog = LayoutInflater.from(context).inflate(R.layout.assignment_dialog, null)
        val assignmentDialogView = assignmentDialog.findViewById<View>(R.id.assignment_dialog_root_view)
        val assignmentDialogBuilder = AlertDialog.Builder(context)

        assignmentDialogView.findViewById<RelativeLayout>(R.id.assignment_header_view).setBackgroundColor(
                utils.getColorByLetterGrade(assignmentItem.letterGrade))

        assignmentDialogView.findViewById<TextView>(R.id.assignment_percentage_grade_tv).text = assignmentItem.percentage
        assignmentDialogView.findViewById<TextView>(R.id.assignment_name_tv).text = assignmentItem.title
        assignmentDialogView.findViewById<TextView>(R.id.assignment_date_tv).text = assignmentItem.date
        assignmentDialogView.findViewById<TextView>(R.id.assignment_score_body_tv).text = assignmentItem.getDividedScore()
        assignmentDialogView.findViewById<TextView>(R.id.assignment_weight_body_tv).text = assignmentItem.weight

        assignmentDialogView.findViewById<RecyclerView>(R.id.assignment_flag_rv).adapter = AssignmentFlagAdapter(context, assignmentItem)
        assignmentDialogView.findViewById<RecyclerView>(R.id.assignment_flag_rv).layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        assignmentDialogView.findViewById<TextView>(R.id.assignment_score_title_tv).text = context.getString(R.string.score)
        assignmentDialogView.findViewById<TextView>(R.id.assignment_weight_title_tv).text = context.getString(R.string.weight)
        assignmentDialogView.findViewById<TextView>(R.id.assignment_flag_title_tv).text = context.getString(R.string.flags)

        assignmentDialogBuilder.setView(assignmentDialogView)
        assignmentDialogBuilder.setPositiveButton(context.getString(R.string.sweet), null)
        assignmentDialogBuilder.create().setCanceledOnTouchOutside(true)
        assignmentDialogBuilder.create().show()
    }

    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    inner class FooterViewHolder(itemView: View) : ViewHolder(itemView)
    inner class HeaderViewHolder(itemView: View) : ViewHolder(itemView) {
        val detail_letter_grade_tv: TextView by bindView(R.id.detail_letter_grade_tv)
        val detail_percentage_grade_tv: TextView by bindView(R.id.detail_percentage_grade_tv)
        val detail_header_teacher_name_tv: TextView by bindView(R.id.detail_header_teacher_name_tv)
        val detail_header_email: ImageView by bindView(R.id.detail_header_email_image)
        val detail_header_block_tv: TextView by bindView(R.id.detail_header_block_tv)
        val detail_header_room_tv: TextView by bindView(R.id.detail_header_room_tv)
        val detail_header_grade_background: RelativeLayout by bindView(R.id.detail_header_grade_background)
        val detail_term_select_spinner: Spinner by bindView(R.id.detail_term_select_spinner)
    }

    inner class NormalViewHolder(itemView: View) : ViewHolder(itemView) {
        val detail_assignment_name_tv: TextView by bindView(R.id.detail_assignment_name_tv)
        val detail_assignment_date_tv: TextView by bindView(R.id.detail_assignment_date_tv)
        val detail_assignment_flag_rv: RecyclerView by bindView(R.id.detail_assignment_flag_rv)
        val detail_assignment_percentage_tv: TextView by bindView(R.id.detail_assignment_percentage_tv)
        val detail_assignment_dividing_score_tv: TextView by bindView(R.id.detail_assignment_dividing_score_tv)
        val detail_assignment_grade_background: RelativeLayout by bindView(R.id.detail_assignment_grade_background)
        val detail_header_background: RelativeLayout by bindView(R.id.detail_header_background)
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
