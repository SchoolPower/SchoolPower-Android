package com.carbonylgroup.schoolpower.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.data.AssignmentItem
import com.carbonylgroup.schoolpower.utils.Utils
import kotterknife.bindView

/**
 * Created by carbonyl on 21/01/2018.
 */

class DetailAssignmentFlagAdapter(private val context: Context, private val assignmentItem: AssignmentItem) :
        RecyclerView.Adapter<DetailAssignmentFlagAdapter.DetailAssignmentFlagViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val utils: Utils = Utils(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailAssignmentFlagViewHolder {

        val view = inflater.inflate(R.layout.detail_assignment_flag_item, parent, false)
        return DetailAssignmentFlagViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: DetailAssignmentFlagViewHolder, position: Int) {

        val (icon, _) = utils.getAssignmentFlag(assignmentItem.trueFlags[position].first)
        viewHolder.assignment_flag_image.setImageResource(icon)
    }

    override fun getItemCount(): Int {
        return assignmentItem.trueFlags.count()
    }

    class DetailAssignmentFlagViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        val assignment_flag_image: ImageView by bindView(R.id.detail_assignment_flag_image)
    }
}
