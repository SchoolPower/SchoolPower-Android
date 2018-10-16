package com.carbonylgroup.schoolpower.utils

import android.app.Activity
import android.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.data.CategoryWeightData
import com.carbonylgroup.schoolpower.data.Grade
import com.carbonylgroup.schoolpower.data.Subject

class CategorySettingsDialog(private val activity: Activity, private val categoryWeights: CategoryWeightData,
                             private val subject: Subject, private val grade: Grade,
                             private val refreshCallback: ()->Unit) {

    fun show() {
        constructView()
    }

    private fun constructView() {

        // construct view
        val dialog = activity.layoutInflater.inflate(R.layout.category_setting_dialog, null)
        val dialogContainer = dialog.findViewById<View>(R.id.categorySettingsContainer) as LinearLayout

        for ((cname, cate) in grade.calculatedGrade.categories){
            val label = TextView(activity)
            val edit = EditText(activity)

            label.text = cname
            edit.setText(cate.weight.toString())
            edit.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    categoryWeights.setWeight(cname, subject, s.toString().toDoubleOrNull()?:return)
                    //subject.recalculateGrades(categoriesWeights)
                }

                override fun beforeTextChanged(s: CharSequence, start: Int,
                                               count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int,
                                           before: Int, count: Int) {}
            })

            dialogContainer.addView(label)
            dialogContainer.addView(edit)

        }

        // construct dialog
        val gpaDialogBuilder = AlertDialog.Builder(activity)
        gpaDialogBuilder.setView(dialog)
        gpaDialogBuilder.setTitle("GPA")
        gpaDialogBuilder.setPositiveButton(activity.getString(R.string.sweet)) { _, _ ->
            categoryWeights.flush()
            refreshCallback()
        }
        gpaDialogBuilder.create().setCanceledOnTouchOutside(true)
        gpaDialogBuilder.create().show()
    }
}
