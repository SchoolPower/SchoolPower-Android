package com.carbonylgroup.schoolpower.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.MainActivity
import com.carbonylgroup.schoolpower.utils.Utils
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import java.util.*
import kotlin.collections.HashSet


class BarChartFragment : Fragment() {

    private lateinit var utils: Utils

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_bar_chart, container, false)

        utils = Utils(activity!!)

        // Adjust chart's size to leave the space for the action bar and ad. bar.
        val barChartCardView = view.findViewById<CardView>(R.id.bar_chart_card)
        val layoutParams = barChartCardView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(0, 0, 0, utils.getActionBarSizePx() + utils.dpToPx(58))
        barChartCardView.requestLayout()

        val barChart: BarChart = view.findViewById(R.id.bar_chart)
        barChart.setNoDataText(getString(R.string.chart_not_available))
        barChart.setNoDataTextColor(utils.getSecondaryTextColor())

        val subjects = MainActivity.of(activity).subjects
        if (subjects == null ||
                utils.getGradedSubjects(subjects).isEmpty() ||
                utils.getFilteredSubjects(MainActivity.of(activity).subjects!!).count() == 0)
            return view

        val gradedSubjects = utils.getGradedSubjects(subjects)
        val dataSets = ArrayList<IBarDataSet>()
        val subjectStrings = HashSet<String>()
        val termStrings = Utils.sortTerm(utils.getAllPeriods(gradedSubjects))

        // or let's just do T1,T2,T3,T4
//        val termStrings = arrayListOf("T1","T2","T3","T4")

        val accent = utils.getAccentColor()
        val hsbVals = FloatArray(3)
        Color.colorToHSV(accent, hsbVals)

        val colorList = arrayListOf<Int>()
        val padding = 20
        val n = termStrings.size
        val even = n % 2 == 0

        if (even) {
            val offset = padding / 2
            for (i in n / 2 - 1 downTo 0) {
                colorList.add(Color.HSVToColor(floatArrayOf(hsbVals[0] - padding * i - offset, hsbVals[1], hsbVals[2])))
            }
            for (i in 0 until n / 2) {
                colorList.add(Color.HSVToColor(floatArrayOf(hsbVals[0] + padding * i + offset, hsbVals[1], hsbVals[2])))
            }
        } else {
            for (i in (n - 1) / 2 downTo 1) {
                colorList.add(Color.HSVToColor(floatArrayOf(hsbVals[0] - padding * i, hsbVals[1], hsbVals[2])))
            }
            colorList.add(accent)
            for (i in 1..(n - 1) / 2) {
                colorList.add(Color.HSVToColor(floatArrayOf(hsbVals[0] + padding * i, hsbVals[1], hsbVals[2])))
            }
        }

        // second run -- group them in terms
        for (term in termStrings) {

            val group = ArrayList<BarEntry>()

            for (subject in gradedSubjects) {
                if (!utils.getPreferences()
                                .getBoolean("list_preference_dashboard_show_inactive", true)) {
                    val currentTime = System.currentTimeMillis()
                    val it = MainActivity.of(activity).subjects!!.find { it.name == subject.name }
                            ?: continue
                    if (currentTime < it.startDate || currentTime > it.endDate) continue
                }
                subjectStrings.add(subject.name)
                group.add(BarEntry((subjectStrings.size - 1).toFloat(),
                        subject.grades[term]?.getGrade()?.toFloat()?:Float.NaN))
            }

            val dataSet = BarDataSet(group, term)
            dataSet.color = colorList[termStrings.indexOf(term)]
            dataSets.add(dataSet)
        }

        barChart.description.isEnabled = false
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.granularity = termStrings.size.toFloat()
        barChart.xAxis.axisMinimum = 0f
        barChart.xAxis.axisMaximum = termStrings.size.toFloat() * gradedSubjects.size.toFloat()
        barChart.xAxis.setCenterAxisLabels(true)
        barChart.xAxis.textColor = utils.getPrimaryTextColor()
        //barChart.xAxis.labelRotationAngle = 40f
        barChart.xAxis.valueFormatter = IAxisValueFormatter { value, _ -> subjectStrings.toList().getOrElse(value.toInt() / termStrings.size) { _ -> "" } }
        barChart.axisLeft.textColor = utils.getPrimaryTextColor()
        barChart.axisRight.textColor = utils.getPrimaryTextColor()
        barChart.legend.textColor = utils.getPrimaryTextColor()

        val barData = BarData(dataSets)
        barData.setValueTextSize(8f)
        barData.setDrawValues(true)
        barData.setValueTextColor(utils.getAccentColor())

        //barChart.legend.isEnabled = false
        barChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        barChart.data = barData
        if(dataSets.size > 1) barChart.groupBars(0.0f, 0.2f, 0.1f)
        barChart.invalidate()
        barChart.setVisibleXRange(0.0f, 12.0f)
        barChart.animateY(1000)
        barChart.setScaleEnabled(true)

        barChart.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                barChart.parent.requestDisallowInterceptTouchEvent(true);false
            } else false
        }

        return view
    }
}
