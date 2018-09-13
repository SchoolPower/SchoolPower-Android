package com.carbonylgroup.schoolpower.activities

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

    class CategoryItem(val weight: Double) {
        var score: Double = 0.0
        var maxScore: Double = 0.0

        fun getPercentage() = score / maxScore
        fun getWeightedScore() = score * weight
        fun getWeightedMaxScore() = maxScore * weight
    }

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

        val categoriesMap = HashMap<String, CategoryItem>()
        for (assignment in subject.assignments) {
            if (!categoriesMap.contains(assignment.category))
                categoriesMap[assignment.category] =
                        CategoryItem(categoriesWeights.getWeight(assignment.category, subject)
                                ?: Double.NaN)
            val assignmentWeight = assignment.weight.toDouble()
            val assignmentScore = assignment.score.toDoubleOrNull() ?: continue
            val assignmentMaxScore = assignment.maximumScore.toDoubleOrNull() ?: continue
            categoriesMap[assignment.category]!!.score += assignmentScore * assignmentWeight
            categoriesMap[assignment.category]!!.maxScore += assignmentMaxScore * assignmentWeight
        }

        var sumScore = 0.0
        var sumMaxScore = 0.0
        for ((name, category) in categoriesMap) {
            cates.text = cates.text.toString() + "\n\n" + "name: " + name + " " + category.score +
                    "/" + category.maxScore + " (" + category.getPercentage() * 100 + "%) contributing " +
                    category.getWeightedScore() + "/" + category.getWeightedMaxScore()
            sumScore += category.getWeightedScore()
            sumMaxScore += category.getWeightedMaxScore()
        }

        cates.text = cates.text.toString() + "\n" + "total " + sumScore + "/" + sumMaxScore +
                "(" + (sumScore / sumMaxScore * 100) + "%)"

        catset.setOnClickListener {
            categoriesWeights.setWeight(catname.text.toString(), subject, catweight.text.toString().toDouble())
            categoriesWeights.flush()
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
