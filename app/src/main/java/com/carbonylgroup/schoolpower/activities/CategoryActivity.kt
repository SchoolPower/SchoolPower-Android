package com.carbonylgroup.schoolpower.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.data.CategoryWeightData
import com.carbonylgroup.schoolpower.data.Subject
import com.carbonylgroup.schoolpower.utils.Utils
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.content_category.*

class CategoryActivity : AppCompatActivity() {

    lateinit var categoriesWeights: CategoryWeightData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)
        setSupportActionBar(toolbar)

        val utils = Utils(this)
        categoriesWeights = CategoryWeightData(utils)

        val subject = intent.getSerializableExtra("subject") as Subject

        val name = subject.getLatestTermName(utils)
        val grade = subject.grades[name]!!
        if(name!=null) {
            supportActionBar!!.title = "$name %.2f%%".format(
                    grade.calculatedGrade.getEstimatedPercentageGrade()*100)
        }

        var text = ""
        for ((cname, cate) in grade.calculatedGrade.categories){
            text += "\n$cname ${cate.score}/${cate.maxScore} (%.2f%%) weight ${cate.weight}"
                    .format(cate.getPercentage()*100)
        }
        cates.text = text

        val categories = grade.calculatedGrade.categories
        val weightLabel = arrayOfNulls<TextView>(categories.size)
        val weightEdit = arrayOfNulls<EditText>(categories.size)

        for ((cname, cate) in grade.calculatedGrade.categories){
            val label = TextView(this)
            val edit = EditText(this)

            label.text = cname
            edit.setText(cate.weight.toString())
            edit.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    categoriesWeights.setWeight(cname, subject, s.toString().toDoubleOrNull()?:return)
                    categoriesWeights.flush()
                    subject.recalculateGrades(categoriesWeights)
                }

                override fun beforeTextChanged(s: CharSequence, start: Int,
                                               count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int,
                                           before: Int, count: Int) {}
            })

            categoryContainer.addView(label)
            categoryContainer.addView(edit)

        }
    }
}
