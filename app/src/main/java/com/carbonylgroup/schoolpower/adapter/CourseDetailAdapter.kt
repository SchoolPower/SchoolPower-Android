/**
 * Copyright (C) 2019 SchoolPower Studio
 */

package com.carbonylgroup.schoolpower.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
class CourseDetailAdapter(private val context: Context, private val subject: Subject, private val showHeader: Boolean,
                          private val filters: List<String>,
                          private val filter: (List<AssignmentItem>, String)->List<AssignmentItem>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var presentingTermPos = 0
    private val utils: Utils = Utils(context)
    private var list: ArrayList<AssignmentItem>? = null

    init{
        if(filters.isNotEmpty()) filterList(filters[0])
    }

    private fun filterList(filterStr: String){
        list = ArrayList(filter(subject.assignments, filterStr))
        list!!.sortWith(Comparator { o1, o2 -> o2.date.compareTo(o1.date) })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == HEADER_VIEW) return HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.course_detail_header, parent, false))
        if (viewType == FOOTER_VIEW) return FooterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.course_detail_footer, parent, false))
        return NormalViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.course_detail_assignment_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is NormalViewHolder) {

            val assignmentItem = list!![position - 1]
            holder.detail_assignment_name_tv.text = assignmentItem.title
            holder.detail_assignment_date_tv.text = assignmentItem.date
            holder.detail_assignment_percentage_tv.text = assignmentItem.getPercentageString()
            holder.detail_assignment_dividing_score_tv.text = assignmentItem.getDividedScore()
            holder.detail_assignment_flag_rv.adapter = DetailAssignmentFlagAdapter(context, assignmentItem)
            holder.detail_assignment_grade_background.setBackgroundColor(utils.getColorByLetterGrade(assignmentItem.letterGrade))
            holder.detail_assignment_flag_rv.layoutManager =
                LinearLayoutManager(
                    context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )

            if (assignmentItem.isNew) {
                holder.detail_header_background.setBackgroundColor(utils.getAccentColor())
                holder.detail_assignment_name_tv.setTextColor(ContextCompat.getColor(context, R.color.white))
                holder.detail_assignment_date_tv.setTextColor(ContextCompat.getColor(context, R.color.white_0_20))
            } else {
                holder.detail_header_background.setBackgroundColor(utils.getCardBackground())
                holder.detail_assignment_name_tv.setTextColor(utils.getPrimaryTextColor())
                holder.detail_assignment_date_tv.setTextColor(utils.getSecondaryTextColor())
            }
            holder.detail_header_background.setOnClickListener {
                showAssignmentDialog(assignmentItem)
            }
            holder.detail_assignment_flag_rv.setOnTouchListener { _, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_UP)
                    showAssignmentDialog(assignmentItem)
                false
            }

        } else if (holder is HeaderViewHolder) {

            val termAdapter = ArrayAdapter(context, R.layout.term_selection_spinner, filters)
            holder.detail_term_select_spinner.adapter = termAdapter
            holder.detail_term_select_spinner.setSelection(presentingTermPos)
            holder.detail_term_select_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                    if (pos != presentingTermPos) {
                        presentingTermPos = pos
                        filterList(filters[pos])
                        refreshAdapter()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            if(showHeader){
                val period = utils.getLatestTermGrade(subject)

                holder.detail_letter_grade_tv.text = period?.letter ?: "--"
                holder.detail_percentage_grade_tv.text = period?.getPercentageString() ?: "--"
                holder.detail_header_teacher_name_tv.text = subject.teacherName
                holder.detail_header_block_tv.text = context.getString(R.string.block) + " " + subject.blockLetter
                holder.detail_header_room_tv.text = context.getString(R.string.room) + " " + subject.roomNumber
                holder.detail_header_grade_background.setBackgroundColor(utils.getColorByLetterGrade(period?.letter ?: "--"))

                if (subject.teacherEmail == "null") {
                    holder.detail_header_email.visibility = GONE
                } else {
                    holder.detail_header_email.setOnClickListener {
                        val uri = Uri.parse("mailto:" + subject.teacherEmail)
                        val intent = Intent(Intent.ACTION_SENDTO, uri)
                        context.startActivity(Intent.createChooser(intent, context.getString(R.string.choose_email_app)))
                    }
                }
            }else{
                holder.detail_header_heading_card.visibility = GONE
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

        assignmentDialogView.findViewById<TextView>(R.id.assignment_percentage_grade_tv).text = assignmentItem.getPercentageString()
        assignmentDialogView.findViewById<TextView>(R.id.assignment_name_tv).text = assignmentItem.title
        assignmentDialogView.findViewById<TextView>(R.id.assignment_date_tv).text = assignmentItem.date
        assignmentDialogView.findViewById<TextView>(R.id.assignment_category_tv).text = assignmentItem.category
        assignmentDialogView.findViewById<TextView>(R.id.assignment_score_body_tv).text = assignmentItem.getDividedScore()
        assignmentDialogView.findViewById<TextView>(R.id.assignment_weight_body_tv).text = assignmentItem.weight.toString()

        assignmentDialogView.findViewById<RecyclerView>(R.id.assignment_flag_rv).adapter = AssignmentFlagAdapter(context, assignmentItem)
        assignmentDialogView.findViewById<RecyclerView>(R.id.assignment_flag_rv).layoutManager =
            LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
            )

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
        val detail_header_heading_card: CardView by bindView(R.id.heading_card)
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

    companion object {
        private val HEADER_VIEW = 1
        private val FOOTER_VIEW = 2
    }
}
