package com.carbonylgroup.schoolpower.adapter

/**
 * Created by carbonyl on 12/11/2017.
 */
import androidx.recyclerview.widget.RecyclerView
import android.view.View

interface OnItemClickListener {
    fun onItemClicked(position: Int, view: View)
}

fun RecyclerView.addOnItemClickListener(onClickListener: OnItemClickListener) {

    this.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
        override fun onChildViewAttachedToWindow(p0: View) {
            val listener = View.OnClickListener {
                val holder = getChildViewHolder(p0)
                onClickListener.onItemClicked(holder.adapterPosition, p0)
            }
            p0.setOnClickListener(listener)
        }

        override fun onChildViewDetachedFromWindow(p0: View) {
            p0.setOnClickListener(null)
        }
    })
}
