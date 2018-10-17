package com.carbonylgroup.schoolpower.fragments

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.MainActivity
import com.carbonylgroup.schoolpower.utils.Utils
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class LineChartFragment : Fragment() {

    private lateinit var utils: Utils

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_line_chart, container, false)

        utils = Utils(activity!!)

        // Adjust chart's size to leave the space for the action bar and ad. bar.
        val lineChartCardView = view.findViewById<CardView>(R.id.line_chart_card)
        val layoutParams = lineChartCardView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(0, 0, 0, utils.getActionBarSizePx() + utils.dpToPx(58))
        lineChartCardView.requestLayout()

        val lineChart: LineChart = view.findViewById(R.id.line_chart)
        lineChart.setNoDataText(getString(R.string.chart_not_available))
        lineChart.setNoDataTextColor(utils.getSecondaryTextColor())

        val subjects = MainActivity.of(activity).subjects
        if (subjects == null ||
                utils.getGradedSubjects(subjects).isEmpty() ||
                utils.getFilteredSubjects(MainActivity.of(activity).subjects!!).count() == 0)
            return view

        val historyData = utils.readHistoryGrade()
        val organizedData = HashMap<String, ArrayList<Entry>>()
        val lastData = HashMap<String, Entry>()
        val lineData = LineData()

        for (date in historyData.keys()) {
            val floatDate = (SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(date).time / 1000.0f / 60.0f / 60.0f / 24.0f).toInt().toFloat()
            val historySubjects = historyData.getJSONArray(date)

            for (i in 0 until historySubjects.length()) {
                val subjectNow = historySubjects.getJSONObject(i)
                val subjectName = subjectNow.getString("name")
                val subjectGrade = subjectNow.getDouble("grade").toFloat()
                if (subjectGrade == 0.0f) continue
                if (!utils.getPreferences()
                                .getBoolean("list_preference_dashboard_show_inactive", true)) {
                    val currentTime = System.currentTimeMillis()
                    val it = MainActivity.of(activity).subjects!!.find { it.name == subjectName }
                            ?: continue
                    if (currentTime < it.startDate || currentTime > it.endDate) continue
                }

                val entry = Entry(floatDate, subjectGrade)
                if (organizedData[subjectName] == null) organizedData[subjectName] = ArrayList()
                if (lastData[subjectName] == null) lastData[subjectName] = Entry(floatDate, subjectGrade)
                else lastData[subjectName] = entry
                val subjectItem = organizedData[subjectName]!!
                if (subjectItem.size != 0 && abs(subjectGrade - subjectItem.last().y) < 1e-5) continue
                subjectItem.add(entry)
            }
        }
        for ((name, grade) in lastData) {
            organizedData[name]!!.add(grade)
        }

        for ((count, subjectName) in organizedData.keys.withIndex()) {
            val dataSet = LineDataSet(organizedData[subjectName], subjectName)
            val intColor = Color.parseColor(Utils.chartColorList[count])
            dataSet.color = intColor
            dataSet.valueTextColor = utils.getAccentColor()
            dataSet.circleColors = List((organizedData[subjectName] as ArrayList<Entry>).size) { intColor }
            dataSet.lineWidth = 2.0f
            dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            lineData.addDataSet(dataSet)
        }

        lineChart.data = lineData
        lineChart.invalidate()

        val xAxis = lineChart.xAxis
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(true)
        xAxis.enableGridDashedLine(10f, 10f, 0f)
        xAxis.granularity = 1f
        xAxis.textColor = utils.getPrimaryTextColor()
        xAxis.valueFormatter = object : IAxisValueFormatter {

            private val mFormat = SimpleDateFormat("MM/dd", Locale.CHINA)

            override fun getFormattedValue(value: Float, axis: AxisBase): String {
                val millis = TimeUnit.DAYS.toMillis(value.toLong())
                return mFormat.format(Date(millis))
            }
        }

        lineChart.description.isEnabled = false
        lineChart.axisLeft.enableGridDashedLine(10f, 10f, 0f)
        lineChart.axisLeft.textColor = utils.getPrimaryTextColor()
        lineChart.axisRight.enableGridDashedLine(10f, 10f, 0f)
        lineChart.axisRight.textColor = utils.getPrimaryTextColor()
        lineChart.legend.form = Legend.LegendForm.CIRCLE
        lineChart.legend.textColor = utils.getPrimaryTextColor()
        lineChart.legend.formSize = 8.0f
        lineChart.legend.isWordWrapEnabled = true

        lineChart.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                lineChart.parent.requestDisallowInterceptTouchEvent(true);false
            } else false
        }

        return view
    }
}
