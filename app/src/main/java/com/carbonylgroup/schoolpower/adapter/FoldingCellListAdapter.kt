package com.carbonylgroup.schoolpower.adapter


import android.app.AlertDialog
import android.content.Context
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.ScaleAnimation
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.BaseActivity
import com.carbonylgroup.schoolpower.data.Subject
import com.carbonylgroup.schoolpower.utils.Utils
import com.ramotion.foldingcell.FoldingCell
import java.util.*
import android.os.Handler


/**
 * Simple example of ListAdapter for using with Folding Cell
 * Adapter holds indexes of unfolded elements for correct work with default reusable views behavior
 */
// TODO: fix the folding problem when hiding inactive subjects
class FoldingCellListAdapter(context: Context, private var subjects: List<Subject>?, val unfoldedIndexes: HashSet<Int>, private val transformedPosition: Int) : ArrayAdapter<Subject>(context, 0, subjects) {

    private var fab_in: Animation? = null
    private var utils: Utils = Utils(getContext())
    private var fabOnClickListener: View.OnClickListener? = null
    private var termOnClickListener: com.carbonylgroup.schoolpower.adapter.OnItemClickListener? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val item = subjects!![position]
        var cell = convertView as FoldingCell?
        val viewHolder: ViewHolder

        if (cell == null) {

            viewHolder = ViewHolder()
            val vi = LayoutInflater.from(context)
            cell = vi.inflate(R.layout.main_list_item, parent, false) as FoldingCell

            viewHolder.fold_background = cell.findViewById(R.id.fold_background)
            viewHolder.unfold_trend_card = cell.findViewById(R.id.unfold_trend_card)
            viewHolder.unfold_trend_text = cell.findViewById(R.id.unfold_trend_text)
            viewHolder.unfold_trend_image = cell.findViewById(R.id.unfold_trend_image)
            viewHolder.unfold_header_view = cell.findViewById(R.id.unfold_header_view)
            viewHolder.fold_letter_grade_tv = cell.findViewById(R.id.fold_letter_grade_tv)
            viewHolder.fold_teacher_name_tv = cell.findViewById(R.id.fold_teacher_name_tv)
            viewHolder.fold_block_letter_tv = cell.findViewById(R.id.fold_block_letter_tv)
            viewHolder.fold_grade_background = cell.findViewById(R.id.fold_grade_background)
            viewHolder.fold_subject_title_tv = cell.findViewById(R.id.fold_subject_title_tv)
            viewHolder.floating_action_button = cell.findViewById(R.id.floating_action_button)
            viewHolder.unfold_teacher_name_tv = cell.findViewById(R.id.unfold_teacher_name_tv)
            viewHolder.unfold_subject_title_tv = cell.findViewById(R.id.detail_subject_title_tv)
            viewHolder.detail_header_background = cell.findViewById(R.id.detail_header_background)
            viewHolder.fold_percentage_grade_tv = cell.findViewById(R.id.fold_percentage_grade_tv)
            viewHolder.unfold_percentage_grade_tv = cell.findViewById(R.id.unfold_percentage_grade_tv)
            viewHolder.unfolded_grade_recycler_view = cell.findViewById(R.id.unfolded_grade_recycler_view)

            if (transformedPosition != -1)
                if (position == transformedPosition) {

                    viewHolder.unfold_header_view!!.transitionName = context.getString(R.string.shared_element_course_header)
                    viewHolder.floating_action_button!!.transitionName = context.getString(R.string.shared_element_course_fab)
                }

            cell.tag = viewHolder

            if (unfoldedIndexes.contains(position)) {
                cell.unfold(true)
                popUpFAB(cell, 300)
            } else cell.fold(true)

        } else {

            if (unfoldedIndexes.contains(position)) {
                cell.unfold(true)
                popUpFAB(cell, 300)
            } else cell.fold(true)
            viewHolder = cell.tag as ViewHolder
        }

        val adapter = PeriodGradeAdapter(context, item.grades)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val period = utils.getLatestPeriodGrade(item) ?: Subject.Grade("--", "--", "null", "--")

        viewHolder.fold_letter_grade_tv!!.text = period.letter
        viewHolder.fold_teacher_name_tv!!.text = item.teacherName
        viewHolder.fold_block_letter_tv!!.text = item.blockLetter
        viewHolder.unfolded_grade_recycler_view!!.adapter = adapter
        viewHolder.fold_subject_title_tv!!.text = item.name
        viewHolder.unfold_teacher_name_tv!!.text = item.teacherName
        viewHolder.unfold_subject_title_tv!!.text = item.name
        viewHolder.unfolded_grade_recycler_view!!.layoutManager = layoutManager
        viewHolder.fold_percentage_grade_tv!!.text = period.percentage
        viewHolder.floating_action_button!!.setOnClickListener(fabOnClickListener)
        viewHolder.unfold_percentage_grade_tv!!.text = period.percentage
        viewHolder.unfold_header_view!!.setBackgroundColor(utils.getColorByLetterGrade(period.letter))
        viewHolder.fold_grade_background!!.setBackgroundColor(utils.getColorByLetterGrade(period.letter))
        viewHolder.detail_header_background!!.setBackgroundColor(utils.getCardBackground())
        viewHolder.unfold_trend_card!!.visibility = View.GONE

        if (item.assignments.any { it -> it.isNew }) { // if any assignment is marked as new
            viewHolder.fold_subject_title_tv!!.setTextColor(ContextCompat.getColor(context, R.color.white))
            viewHolder.fold_teacher_name_tv!!.setTextColor(ContextCompat.getColor(context, R.color.white_0_10))
            viewHolder.fold_block_letter_tv!!.setTextColor(ContextCompat.getColor(context, R.color.white_0_10))
            viewHolder.fold_background!!.setBackgroundColor(utils.getAccentColor())

            // Show increase/decrease margin badge
            viewHolder.unfold_trend_card!!.visibility = View.VISIBLE
            if (item.margin > 0) {
                // Increased
                viewHolder.unfold_trend_image!!.setImageResource(R.drawable.ic_trending_up_green_24dp)
                viewHolder.unfold_trend_text!!.setTextColor(ContextCompat.getColor(context, R.color.B_score_green_dark))
            } else if (item.margin < 0) {
                // Decreased
                viewHolder.unfold_trend_image!!.setImageResource(R.drawable.ic_trending_down_red_24dp)
                viewHolder.unfold_trend_text!!.setTextColor(ContextCompat.getColor(context, R.color.Cm_score_red))
            } else {
                // Not changed
                viewHolder.unfold_trend_image!!.setImageResource(R.drawable.ic_trending_flat_gray_24dp)
                viewHolder.unfold_trend_text!!.setTextColor(ContextCompat.getColor(context, R.color.gray))
            }
            viewHolder.unfold_trend_text!!.text = Math.abs(item.margin).toString()

        } else {
            viewHolder.fold_subject_title_tv!!.setTextColor(utils.getPrimaryTextColor())
            viewHolder.fold_teacher_name_tv!!.setTextColor(utils.getSecondaryTextColor())
            viewHolder.fold_block_letter_tv!!.setTextColor(utils.getSecondaryTextColor())
            viewHolder.fold_background!!.setBackgroundColor(utils.getCardBackground())
        }

        viewHolder.unfolded_grade_recycler_view!!.addOnItemClickListener(termOnClickListener!!)

        Handler().post { refreshPeriodRecycler(cell, position) }

        return cell
    }

    private fun initAnim(_delay: Int) {

        fab_in = ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        fab_in!!.duration = 200
        fab_in!!.startOffset = _delay.toLong()
        fab_in!!.interpolator = DecelerateInterpolator()
    }

    fun refreshPeriodRecycler(_cell: FoldingCell, transformedPosition: Int) {

        val items = subjects!![transformedPosition].grades
        val adapter = PeriodGradeAdapter(context, items)
        adapter.notifyDataSetChanged()
        (_cell.findViewById<RecyclerView>(R.id.unfolded_grade_recycler_view)).adapter = adapter
    }

    fun setMainListItems(_subjects: List<Subject>) {
        subjects = _subjects
    }

    private fun popUpFAB(_cell: FoldingCell, _delay: Int) {

        initAnim(_delay)
        _cell.findViewById<View>(R.id.floating_action_button).startAnimation(fab_in)
    }

    fun registerToggle(position: Int) {

        if (unfoldedIndexes.contains(position)) registerFold(position)
        else registerUnfold(position)
    }

    private fun registerFold(position: Int) {
        unfoldedIndexes.remove(position)
    }

    private fun registerUnfold(position: Int) {
        unfoldedIndexes.add(position)
    }

    fun setFabOnClickListener(fabOnClickListener: View.OnClickListener) {
        this.fabOnClickListener = fabOnClickListener
    }

    fun setTermOnClickListener(termOnClickListener: com.carbonylgroup.schoolpower.adapter.OnItemClickListener) {
        this.termOnClickListener = termOnClickListener
    }

    fun showTermDialog(subject: Subject, position: Int) {
        val objects = subject.grades
        val gradeMap: Map<String, Subject.Grade> = objects
        val keys = objects.keys.toTypedArray()
        val term = gradeMap[keys[position]]!!

        val letter = term.letter
        val percentage = term.percentage
        val termIndicator = keys[position]
        val subjectTitle = subject.name
        val evaluation = term.evaluation
        val comment = term.comment

        val termDialog = LayoutInflater.from(context).inflate(R.layout.term_dialog, null)
        val termDialogView = termDialog.findViewById<View>(R.id.term_dialog_root_view)
        val termDialogBuilder = AlertDialog.Builder(context)

        termDialogView.findViewById<RelativeLayout>(R.id.term_header_view).setBackgroundColor(
                utils.getColorByLetterGrade(letter))

        termDialogView.findViewById<TextView>(R.id.term_percentage_grade_tv).text = percentage
        termDialogView.findViewById<TextView>(R.id.term_name_tv).text = termIndicator
        termDialogView.findViewById<TextView>(R.id.term_subject_tv).text = subjectTitle
        termDialogView.findViewById<TextView>(R.id.term_eval_body_tv).text =
                if (evaluation.equals("--")) "N/A"
                else String.format("%s (%s)", evaluation, utils.citizenshipCodes[evaluation])
        termDialogView.findViewById<TextView>(R.id.term_comment_body_tv).text =
                if (comment.equals("null")) "N/A" else comment

        termDialogView.findViewById<TextView>(R.id.term_eval_title_tv).text = context.getString(R.string.evaluation)
        termDialogView.findViewById<TextView>(R.id.term_comment_title_tv).text = context.getString(R.string.comment)

        termDialogBuilder.setView(termDialogView)
        termDialogBuilder.setPositiveButton(context.getString(R.string.sweet), null)
        termDialogBuilder.create().setCanceledOnTouchOutside(true)
        termDialogBuilder.create().show()
    }

    private class ViewHolder {
        internal var unfold_trend_card: CardView? = null
        internal var unfold_trend_text: TextView? = null
        internal var unfold_trend_image: ImageView? = null
        internal var fold_letter_grade_tv: TextView? = null
        internal var fold_teacher_name_tv: TextView? = null
        internal var fold_block_letter_tv: TextView? = null
        internal var fold_subject_title_tv: TextView? = null
        internal var fold_background: RelativeLayout? = null
        internal var unfold_teacher_name_tv: TextView? = null
        internal var unfold_subject_title_tv: TextView? = null
        internal var fold_percentage_grade_tv: TextView? = null
        internal var unfold_header_view: RelativeLayout? = null
        internal var unfold_percentage_grade_tv: TextView? = null
        internal var fold_grade_background: RelativeLayout? = null
        internal var detail_header_background: RelativeLayout? = null
        internal var unfolded_grade_recycler_view: RecyclerView? = null
        internal var floating_action_button: FloatingActionButton? = null
    }
}
