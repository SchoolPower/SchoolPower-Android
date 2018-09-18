package com.carbonylgroup.schoolpower.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.graphics.Color
import android.support.v4.content.ContextCompat
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.data.CategoryWeightData
import com.carbonylgroup.schoolpower.data.Subject
import com.carbonylgroup.schoolpower.utils.PieRadarChart.PieRadarChart
import com.carbonylgroup.schoolpower.utils.Utils
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.content_category.*
import java.util.ArrayList

class CategoryActivity : BaseActivity() {

    lateinit var categoriesWeights: CategoryWeightData

    private lateinit var categoriesWeights: CategoryWeightData

    override fun initActivity() {
        super.initActivity()
        setContentView(R.layout.activity_category)
        setSupportActionBar(toolbar)

        initCategories()
        initChart()
    }

    private fun initCategories() {
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

    private fun initChart() {
        val entries = ArrayList<PieEntry>()
        val percentages = ArrayList<android.util.Pair<Float, Float>>()
        val colors = ArrayList<Int>()
        entries.add(PieEntry(95f, "rabbit"))
        entries.add(PieEntry(5f, "rabbit"))
        percentages.add(android.util.Pair(95f, 100f))
        percentages.add(android.util.Pair(20f, 100f))
        colors.add(ContextCompat.getColor(this, R.color.material_purple_500))
        colors.add(ContextCompat.getColor(this, R.color.material_teal_500))
        setData(entries, percentages, colors)
    }

    private fun setData(entries: ArrayList<PieEntry>,
                        percentages: ArrayList<android.util.Pair<Float, Float>>,
                        colors: ArrayList<Int>) {

        val primaryColor = Utils(this).getPrimaryTextColor()
        val pieRadarChart = findViewById<PieRadarChart>(R.id.prc)!!
        pieRadarChart.rotationAngle = 0f
        pieRadarChart.isRotationEnabled = true
        pieRadarChart.isDrawHoleEnabled = false
        pieRadarChart.description.isEnabled = false
        pieRadarChart.transparentCircleRadius = 61f
        pieRadarChart.isHighlightPerTapEnabled = false
        pieRadarChart.dragDecelerationFrictionCoef = 0.95f
        pieRadarChart.setUsePercentValues(true)
        pieRadarChart.setEntryLabelTextSize(11f)
        pieRadarChart.setTransparentCircleAlpha(110)
        pieRadarChart.setEntryLabelColor(primaryColor)
        pieRadarChart.setEntryInnerLabelColor(Color.WHITE)
        pieRadarChart.setTransparentCircleColor(Color.WHITE)
        pieRadarChart.setExtraOffsets(5f, 10f, 5f, 5f)
        pieRadarChart.setExtraOffsets(20f, 0f, 20f, 0f)

        val dataSet = PieDataSet(entries, "Election Results")
        dataSet.colors = colors
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        dataSet.valueLinePart1Length = .4f
        dataSet.valueLinePart2Length = .7f
        dataSet.valueLineColor = primaryColor
        dataSet.valueTextColor = primaryColor
        dataSet.valueLinePart1OffsetPercentage = 80f
        dataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(11f)
        pieRadarChart.data = data
        pieRadarChart.legend.isEnabled = false
        pieRadarChart.invalidate()
        pieRadarChart.highlightValues(null)
        pieRadarChart.setDrawRadiiPairs(percentages)
        pieRadarChart.animateY(1400, Easing.EasingOption.EaseInOutCubic)
    }
}
