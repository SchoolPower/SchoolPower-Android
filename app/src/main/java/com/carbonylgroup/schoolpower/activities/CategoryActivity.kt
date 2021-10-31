package com.carbonylgroup.schoolpower.activities

import android.app.AlertDialog
import android.graphics.Color
import android.support.v7.widget.LinearLayoutManager
import android.widget.ArrayAdapter
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.adapter.CourseDetailAdapter
import com.carbonylgroup.schoolpower.data.AssignmentItem
import com.carbonylgroup.schoolpower.data.CategoryWeightData
import com.carbonylgroup.schoolpower.data.Subject
import com.carbonylgroup.schoolpower.utils.CategorySettingsDialog
import com.carbonylgroup.schoolpower.utils.Utils
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.add_assignment_dialog.view.*
import kotlinx.android.synthetic.main.content_category.*
import org.json.JSONObject
import java.util.*

class CategoryActivity : BaseActivity() {

    private lateinit var categoriesWeights: CategoryWeightData

    override fun initActivity() {
        super.initActivity()
        setContentView(R.layout.activity_category)
        setSupportActionBar(toolbar)

        categoriesWeights = CategoryWeightData(utils)
        val subject = intent.getSerializableExtra("subject") as Subject
        try {
            subject.recalculateGrades(categoriesWeights)
            initChart(subject)
            initRecycler(subject)
        } catch (e: Exception) {
            utils.errorHandler(e)
        }

        fab.setOnClickListener {
            CategorySettingsDialog(this, categoriesWeights, subject,
                    subject.grades[subject.getLatestTermName(utils)]!!) {
                categoriesWeights = CategoryWeightData(utils)
                subject.recalculateGrades(categoriesWeights)
                initChart(subject)
            }.show()
        }

        if(utils.isDeveloperMode()) {
            fab.setOnLongClickListener {
                val categories = subject.grades[subject.getLatestTermName(utils)]!!.calculatedGrade.categories.keys.toList()

                // construct view
                val dialog = this@CategoryActivity.layoutInflater.inflate(R.layout.add_assignment_dialog, null)
                dialog.categorySpinner.adapter = ArrayAdapter(this@CategoryActivity, R.layout.term_selection_spinner, categories)

                // construct dialog
                val gpaDialogBuilder = AlertDialog.Builder(this@CategoryActivity)
                gpaDialogBuilder.setView(dialog)
                gpaDialogBuilder.setTitle("Add Assignment")
                gpaDialogBuilder.setPositiveButton(getString(R.string.sweet)) { _, _ ->
                    val json = JSONObject(
                            """
{
"description": "",
"name": "dummy",
"percentage": "86.96",
"letterGrade": "A",
"date": "2017-09-11T16:00:00.000Z",
"weight": "1",
"includeInFinalGrade": "1",
"terms": ["T1","T2","S1","S2","Y1"]
}
""")
                    json.put("category", categories[dialog.categorySpinner.selectedItemId.toInt()])
                    json.put("score",  dialog.mark.text.toString().toDoubleOrNull() ?: 100)
                    json.put("pointsPossible", dialog.markPossible.text.toString().toDoubleOrNull()?: 100)
                    json.put("weight", dialog.weight.text.toString().toDoubleOrNull() ?: 1.0)
                    subject.assignments.add(AssignmentItem(json))
                    subject.recalculateGrades(categoriesWeights)
                    initChart(subject)
                }
                gpaDialogBuilder.create().show()
                true
            }
        }
    }

    private fun initRecycler(subject: Subject) {

        assignmentsRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val name = subject.getLatestTermName(utils) ?: return
        val grades = subject.grades[name] ?: return
        val categories = grades.calculatedGrade.categories.keys.toList()

        assignmentsRecycler.adapter = CourseDetailAdapter(this, subject, false, categories,
                fun(assignments:List<AssignmentItem>, filter:String):List<AssignmentItem> {
                    return assignments.filter { it.category == filter }
                }
        )
    }

    private fun initChart(subject: Subject) {

        val name = subject.getLatestTermName(utils) ?: return
        val grade = subject.grades[name] ?: return

        toolbar_layout.title = "$name %.2f%%".format(
                grade.calculatedGrade.getEstimatedPercentageGrade()*100)

        /*
        var text = ""
        for ((cname, cate) in grade.calculatedGrade.categories){
            text += ("\n$cname ${cate.score}/${cate.maxScore} (%.2f%%) weight ${cate.weight} " +
                    "Contributing a loss of %.2f%%")
                    .format(cate.getPercentage()*100, cate.weight*(1-cate.getPercentage()))
        }
        cates.text = text
        */

        val entries = ArrayList<PieEntry>()
        val percentages = ArrayList<android.util.Pair<Float, Float>>()
        val colors = ArrayList<Int>()
        var count = 0
        for ((cname, cate) in grade.calculatedGrade.categories){
            entries.add(PieEntry(cate.weight.toFloat(), cname))
            percentages.add(android.util.Pair(cate.score.toFloat(), cate.maxScore.toFloat()))
            colors.add(Color.parseColor(Utils.chartColorList[count++ % Utils.chartColorList.size]))
        }
        setRadarPieChartData(entries, percentages, colors)

        val entries2 = ArrayList<PieEntry>()
        for ((cname, cate) in grade.calculatedGrade.categories){
            if(cate.getPercentage().isNaN()) continue
            entries2.add(PieEntry((cate.weight * (1.0f - cate.getPercentage())).toFloat(), cname))
        }
        setPieChartData(entries2, colors)
    }

    private fun setRadarPieChartData(entries: ArrayList<PieEntry>,
                        percentages: ArrayList<android.util.Pair<Float, Float>>,
                        colors: ArrayList<Int>) {

        val primaryColor = utils.getPrimaryTextColor()
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
        pieRadarChart.setExtraOffsets(15f, 0f, 15f, 0f)

        val dataSet = PieDataSet(entries, "Categories")
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
        pieRadarChart.animateY(1200, Easing.EasingOption.EaseInOutCubic)
    }


    private fun setPieChartData(entries: ArrayList<PieEntry>,
                                     colors: ArrayList<Int>) {

        val primaryColor = utils.getPrimaryTextColor()
        pieChart.rotationAngle = 0f
        pieChart.isRotationEnabled = true
        pieChart.isDrawHoleEnabled = false
        pieChart.description.isEnabled = false
        pieChart.transparentCircleRadius = 61f
        pieChart.isHighlightPerTapEnabled = false
        pieChart.dragDecelerationFrictionCoef = 0.95f
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelTextSize(11f)
        pieChart.setTransparentCircleAlpha(110)
        pieChart.setEntryLabelColor(primaryColor)
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setExtraOffsets(5f, 5f, 5f, 5f)

        val dataSet = PieDataSet(entries, "Categories")
        dataSet.colors = colors
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        dataSet.valueLinePart1Length = .4f
        dataSet.valueLinePart2Length = .7f
        dataSet.valueLineColor = primaryColor
        dataSet.valueTextColor = primaryColor
        dataSet.valueLinePart1OffsetPercentage = 80f
        dataSet.xValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
        dataSet.yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(11f)
        pieChart.data = data
        pieChart.legend.isEnabled = false
        pieChart.invalidate()
        pieChart.highlightValues(null)
        pieChart.animateY(1200, Easing.EasingOption.EaseInOutCubic)
    }
}
