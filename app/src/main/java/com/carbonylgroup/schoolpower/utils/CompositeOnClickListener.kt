package com.carbonylgroup.schoolpower.utils

import android.view.View

class CompositeOnClickListener : View.OnClickListener {

    private var listeners = ArrayList<View.OnClickListener>()

    fun addOnClickListener(listener: View.OnClickListener) {
        listeners.add(listener)
    }

    override fun onClick(v: View) {
        for (listener in listeners) {
            listener.onClick(v)
        }
    }
}
