package com.carbonylgroup.schoolpower.fragments

import android.app.Fragment
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.MainActivity
import com.carbonylgroup.schoolpower.data.Subject
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



class BarChartFragment : Fragment() {

    private lateinit var utils: Utils

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.fragment_bar_chart, container, false)

        utils = Utils(activity)

        // Adjust chart's size to leave the space for the action bar and ad. bar.
        val barChartCardView = view.findViewById<CardView>(R.id.bar_chart_card)
        val layoutParams = barChartCardView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(0, 0, 0, utils.getActionBarSizePx() + utils.dpToPx(58))
        barChartCardView.requestLayout()

        //TODO: Improve the charts display when there is nothing QVQ
        if (MainActivity.of(activity).subjects == null)
            return view

        val subjects = MainActivity.of(activity).subjects!!

        if (subjects.isEmpty())
            return view

        val gradedSubjects = ArrayList<Subject>() // Subjects that have grades

        subjects.forEach {
            val grade = utils.getLatestPeriodGrade(it)
            if (grade != null && grade.letter != "--") gradedSubjects.add(it)
        }
        val barChart: BarChart = view.findViewById(R.id.bar_chart)
        barChart.description.isEnabled = false

        val dataSets = ArrayList<IBarDataSet>()
        val subjectStrings = ArrayList<String>()

        // first run -- get all available terms
        /*
        val termStrings = ArrayList<String>()
        for (it in gradedSubjects) {
            for (term in it.grades.keys) {
                if(!termStrings.contains(term)) termStrings.add(term)
            }
        }
        */
        // or let's just do T1,T2,T3,T4

        val termStrings = arrayListOf("T1","T2","T3","T4")

        // second run -- group them in terms
        for (term in termStrings) {

            val group = ArrayList<BarEntry>()

            for (subject in gradedSubjects) {

                subjectStrings.add(subject.name)
                if (subject.grades[term] != null && subject.grades[term]!!.percentage!="0") {
                    group.add(BarEntry((subjectStrings.size - 1).toFloat(),
                            subject.grades[term]!!.percentage.toFloat()))
                }else{
                    group.add(BarEntry((subjectStrings.size - 1).toFloat(), Float.NaN))
                }
            }

            val dataSet = BarDataSet(group, term)
            dataSet.color = Color.parseColor(Utils.chartColorList[termStrings.indexOf(term)])
            dataSets.add(dataSet)
        }

        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.granularity = 4f
        barChart.xAxis.axisMinimum = 0f
        barChart.xAxis.axisMaximum = 4f*gradedSubjects.size
        barChart.xAxis.setCenterAxisLabels(true)
        //barChart.xAxis.labelRotationAngle = 40f
        barChart.xAxis.valueFormatter = IAxisValueFormatter { value, _ -> subjectStrings.getOrElse(value.toInt()/4,{_->""}) }

        val barData = BarData(dataSets)
        barData.setValueTextSize(8f)
        barData.setDrawValues(true)
        barData.setValueTextColor(ContextCompat.getColor(activity, R.color.primary))

        //barChart.legend.isEnabled = false
        barChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        barChart.data = barData
        barChart.groupBars(0.0f, 0.2f, 0.1f)
        barChart.invalidate()
        barChart.setVisibleXRange(0.0f, 12.0f)
        barChart.animateY(1000)
        barChart.setScaleEnabled(true)

        barChart.setOnTouchListener({ _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                barChart.parent.requestDisallowInterceptTouchEvent(true);false
            } else false
        })

        return view
    }
}
