package com.carbonylgroup.schoolpower.classes.Adapter


import android.content.Context
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.ScaleAnimation
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import android.widget.TextView
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.classes.ListItems.MainListItem
import com.carbonylgroup.schoolpower.classes.Utils.Utils
import com.ramotion.foldingcell.FoldingCell
import java.util.*

/**
 * Simple example of ListAdapter for using with Folding Cell
 * Adapter holds indexes of unfolded elements for correct work with default reusable views behavior
 */
class FoldingCellListAdapter(context: Context, private var mainListItems: ArrayList<MainListItem>?, val unfoldedIndexes: HashSet<Int>, private val transformedPosition: Int) : ArrayAdapter<MainListItem>(context, 0, mainListItems) {

    private val utils: Utils = Utils(getContext())
    private var fab_in: Animation? = null
    private var defaultRequestBtnClickListener: View.OnClickListener? = null
    val periodGradeItemClickListener: View.OnClickListener? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val item = mainListItems!![position]
        var cell = convertView as FoldingCell?
        val viewHolder: ViewHolder

        if (cell == null) {

            viewHolder = ViewHolder()
            val vi = LayoutInflater.from(context)
            cell = vi.inflate(R.layout.main_list_item, parent, false) as FoldingCell

            viewHolder.fold_letter_grade_tv = cell.findViewById(R.id.fold_letter_grade_tv) as TextView
            viewHolder.fold_teacher_name_tv = cell.findViewById(R.id.fold_teacher_name_tv) as TextView
            viewHolder.fold_block_letter_tv = cell.findViewById(R.id.fold_block_letter_tv) as TextView
            viewHolder.fold_subject_title_tv = cell.findViewById(R.id.fold_subject_title_tv) as TextView
            viewHolder.unfold_teacher_name_tv = cell.findViewById(R.id.unfold_teacher_name_tv) as TextView
            viewHolder.unfold_subject_title_tv = cell.findViewById(R.id.detail_subject_title_tv) as TextView
            viewHolder.fold_percentage_grade_tv = cell.findViewById(R.id.fold_percentage_grade_tv) as TextView
            viewHolder.unfold_percentage_grade_tv = cell.findViewById(R.id.unfold_percentage_grade_tv) as TextView
            viewHolder.unfolded_grade_recycler_view = cell.findViewById(R.id.unfolded_grade_recycler_view) as RecyclerView
            viewHolder.floating_action_button = cell.findViewById(R.id.floating_action_button) as FloatingActionButton
            viewHolder.fold_grade_background = cell.findViewById(R.id.fold_grade_background) as RelativeLayout
            viewHolder.unfold_header_view = cell.findViewById(R.id.unfold_header_view) as RelativeLayout
            viewHolder.fold_background = cell.findViewById(R.id.fold_background) as RelativeLayout

            if (transformedPosition != -1)
                if (position == transformedPosition) {

                    viewHolder.unfold_header_view!!.transitionName = context.getString(R.string.shared_element_course_header)
                    viewHolder.floating_action_button!!.transitionName = context.getString(R.string.shared_element_course_fab)
                }

            cell.tag = viewHolder

            if (unfoldedIndexes.contains(position)) {

                cell.unfold(true)
                popUpFAB(cell, 300)
            } else
                cell.fold(true)
        } else {

            if (unfoldedIndexes.contains(position)) {

                cell.unfold(true)
                popUpFAB(cell, 300)
            } else
                cell.fold(true)
            viewHolder = cell.tag as ViewHolder
        }

        val items = item.periodGradeItemArrayList
        val adapter = PeriodGradeAdapter(context, items)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        viewHolder.fold_letter_grade_tv!!.text = item.letterGrade
        viewHolder.fold_teacher_name_tv!!.text = item.teacherName
        viewHolder.fold_block_letter_tv!!.text = item.blockLetter
        viewHolder.fold_subject_title_tv!!.text = item.subjectTitle
        viewHolder.unfold_teacher_name_tv!!.text = item.teacherName
        viewHolder.unfold_subject_title_tv!!.text = item.subjectTitle
        viewHolder.fold_percentage_grade_tv!!.text = item.percentageGrade
        viewHolder.unfold_percentage_grade_tv!!.text = item.percentageGrade
        viewHolder.unfolded_grade_recycler_view!!.layoutManager = layoutManager
        viewHolder.unfolded_grade_recycler_view!!.adapter = adapter
        viewHolder.floating_action_button!!.setOnClickListener(defaultRequestBtnClickListener)
        viewHolder.fold_grade_background!!.setBackgroundColor(utils.getColorByLetterGrade(context, item.letterGrade))
        viewHolder.unfold_header_view!!.setBackgroundColor(utils.getColorByLetterGrade(context, item.letterGrade))

        var anyNew = false
        if (item.getAssignmentItemArrayList("T4") != null) {
            for (t4 in item.getAssignmentItemArrayList("T4")!!) {
                if (t4.isNew) {
                    anyNew = true
                    break
                }
            }
        }
        if (anyNew) {
            viewHolder.fold_subject_title_tv!!.setTextColor(ContextCompat.getColor(context, R.color.white))
            viewHolder.fold_teacher_name_tv!!.setTextColor(ContextCompat.getColor(context, R.color.white_0_5))
            viewHolder.fold_block_letter_tv!!.setTextColor(ContextCompat.getColor(context, R.color.white_0_5))
            viewHolder.fold_background!!.setBackgroundColor(ContextCompat.getColor(context, R.color.accent))
        }

        return cell
    }

    private fun initAnim(_delay: Int) {

        fab_in = ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        fab_in!!.duration = 200
        fab_in!!.startOffset = _delay.toLong()
        fab_in!!.interpolator = DecelerateInterpolator()
    }

    fun refreshPeriodRecycler(_cell: FoldingCell, transformedPosition: Int) {

        val items = mainListItems!![transformedPosition].periodGradeItemArrayList
        val adapter = PeriodGradeAdapter(context, items)
        (_cell.findViewById(R.id.unfolded_grade_recycler_view) as RecyclerView).adapter = adapter
    }

    fun setMainListItems(_mainListItems: ArrayList<MainListItem>) {
        mainListItems = _mainListItems
    }

    private fun popUpFAB(_cell: FoldingCell, _delay: Int) {

        initAnim(_delay)
        _cell.findViewById(R.id.floating_action_button).startAnimation(fab_in)
    }

    fun registerToggle(position: Int) {

        if (unfoldedIndexes.contains(position))
            registerFold(position)
        else
            registerUnfold(position)
    }

    private fun registerFold(position: Int) {
        unfoldedIndexes.remove(position)
    }

    private fun registerUnfold(position: Int) {
        unfoldedIndexes.add(position)
    }

    fun setDefaultRequestBtnClickListener(defaultRequestBtnClickListener: View.OnClickListener) {

        this.defaultRequestBtnClickListener = defaultRequestBtnClickListener
    }

    private class ViewHolder {

        internal var fold_letter_grade_tv: TextView? = null
        internal var fold_teacher_name_tv: TextView? = null
        internal var fold_block_letter_tv: TextView? = null
        internal var fold_subject_title_tv: TextView? = null
        internal var unfold_teacher_name_tv: TextView? = null
        internal var unfold_subject_title_tv: TextView? = null
        internal var fold_percentage_grade_tv: TextView? = null
        internal var unfold_percentage_grade_tv: TextView? = null
        internal var floating_action_button: FloatingActionButton? = null
        internal var unfolded_grade_recycler_view: RecyclerView? = null
        internal var fold_grade_background: RelativeLayout? = null
        internal var unfold_header_view: RelativeLayout? = null
        internal var fold_background: RelativeLayout? = null
    }
}