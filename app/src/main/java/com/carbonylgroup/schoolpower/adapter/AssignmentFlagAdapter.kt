package com.carbonylgroup.schoolpower.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.data.AssignmentItem
import com.carbonylgroup.schoolpower.utils.Utils
import kotterknife.bindView

/**
 * Created by carbonyl on 21/01/2018.
 */

class AssignmentFlagAdapter(private val context: Context, private val assignmentItem: AssignmentItem) :
        RecyclerView.Adapter<AssignmentFlagAdapter.AssignmentFlagViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val utils: Utils = Utils(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentFlagViewHolder {

        val view = inflater.inflate(R.layout.assignment_flag_item, parent, false)
        return AssignmentFlagViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: AssignmentFlagViewHolder, position: Int) {

        val (icon, descrip) = utils.getAssignmentFlag(assignmentItem.trueFlags[position].first)
        viewHolder.assignment_flag_image.setImageResource(icon)
        viewHolder.assignment_flag_description.text = descrip
    }

    override fun getItemCount(): Int {
        return assignmentItem.trueFlags.count()
    }

    class AssignmentFlagViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        val assignment_flag_image: ImageView by bindView(R.id.assignment_flag_image)
        val assignment_flag_description: TextView by bindView(R.id.assignment_flag_description)
    }
}
