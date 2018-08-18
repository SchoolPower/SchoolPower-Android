package com.carbonylgroup.schoolpower.fragments

import android.app.Fragment
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.MainActivity
import com.carbonylgroup.schoolpower.data.Subject
import com.carbonylgroup.schoolpower.utils.Utils
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import java.util.*

class RadarChartFragment : Fragment() {

    private lateinit var utils: Utils

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.fragment_radar_chart, container, false)

        utils = Utils(activity)

        // Adjust chart's size to leave the space for the action bar and ad. bar.
        val radarChartCardView = view.findViewById<CardView>(R.id.radar_chart_card)
        val layoutParams = radarChartCardView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(0, 0, 0, utils.getActionBarSizePx() + utils.dpToPx(58))
        radarChartCardView.requestLayout()

        val radarChart: RadarChart = view.findViewById(R.id.radar_chart)
        radarChart.setNoDataText(getString(R.string.chart_not_available))
        radarChart.setNoDataTextColor(utils.getSecondaryTextColor())

        val subjects = MainActivity.of(activity).subjects
        if (subjects == null ||
                utils.getGradedSubjects(subjects).isEmpty() ||
                utils.getFilteredSubjects(MainActivity.of(activity).subjects!!).count() == 0)
            return view

        val gradedSubjects = utils.getGradedSubjects(subjects)
        val entries = ArrayList<RadarEntry>()
        radarChart.description.isEnabled = false
        val xAxis = radarChart.xAxis
        xAxis.yOffset = 10f
        xAxis.xOffset = 10f
        xAxis.textColor = utils.getPrimaryTextColor()
        xAxis.valueFormatter = object : IAxisValueFormatter {

            private val mSubjectsName = ArrayList<String>()

            init {
                gradedSubjects.mapTo(mSubjectsName) { it.getShortName() }
            }

            override fun getFormattedValue(value: Float, axis: AxisBase): String {
                return mSubjectsName[value.toInt() % mSubjectsName.size]
            }
        }
        var minGrade = 100.0f
        for (it in gradedSubjects) {
            val periodGrade = (utils.getLatestPeriodGrade(it) ?: continue).percentage.toFloat()
            entries.add(RadarEntry(periodGrade))
            if (periodGrade < minGrade) minGrade = periodGrade
        }

        val yAxis = radarChart.yAxis
        yAxis.textSize = 9f
        yAxis.axisMinimum = minGrade / 3 * 2
        yAxis.axisMaximum = 110.0f - 20.0f
        yAxis.setDrawLabels(false)

        val set = RadarDataSet(entries, "Grades")
        set.color = utils.getAccentColor()
        set.fillColor = utils.getAccentColor()
        set.setDrawFilled(true)
        set.fillAlpha = 180
        set.lineWidth = 2f
        set.isDrawHighlightCircleEnabled = true
        set.setDrawHighlightIndicators(false)

        val sets = ArrayList<IRadarDataSet>()
        sets.add(set)

        val radarData = RadarData(sets)
        radarData.setValueTextSize(8f)
        radarData.setDrawValues(true)
        radarData.setValueTextColor(utils.getAccentColor())

        radarChart.legend.isEnabled = false

        radarChart.data = radarData
        //radarChart.invalidate()

        radarChart.animateX(1000)

        return view
    }
}
