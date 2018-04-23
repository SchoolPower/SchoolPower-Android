package com.carbonylgroup.schoolpower.fragments

import android.graphics.Color
import android.os.Bundle
import android.app.Fragment
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.MainActivity
import com.carbonylgroup.schoolpower.data.Subject
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
import android.content.res.TypedArray


class LineChartFragment : Fragment() {

    private lateinit var utils: Utils

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.fragment_line_chart, container, false)
        //MainActivity.of(activity).expandToolBar(true, true)
        utils = Utils(activity)

        val styledAttributes = activity.theme.obtainStyledAttributes(
                intArrayOf(android.R.attr.actionBarSize))
        val mActionBarSize = styledAttributes.getDimension(0, 0f).toInt()
        styledAttributes.recycle()

        val lineChart = view.findViewById<CardView>(R.id.line_chart_card)
        val layoutParams = lineChart.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(0, 0, 0, mActionBarSize + utils.dpToPx(58))
        lineChart.requestLayout()


        if (MainActivity.of(activity).subjects == null || MainActivity.of(activity).subjects!!.count() == 0) {
            //TODO: Improve the charts display when there is nothing QVQ
        } else {

            val rawData = MainActivity.of(activity).subjects!!
            val data = ArrayList<Subject>()

            rawData.forEach {
                val grade = utils.getLatestPeriodGrade(it)
                if (grade != null && grade.letter != "--") data.add(it)
            }

            run {
                val historyData = utils.readHistoryGrade()

                val lineChart: LineChart = view.findViewById<LineChart>(R.id.line_chart)
                lineChart.description.isEnabled = false

                // Map<SubjectName, Array<Entry<Date, Grade>>>
                val organizedData = HashMap<String, ArrayList<Entry>>()
                val lastData = HashMap<String, Entry>()

                val lineData = LineData()
                for (date in historyData.keys()) {
                    val floatDate = (SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(date).time / 1000.0f / 60.0f / 60.0f / 24.0f).toInt().toFloat()
                    val subjects = historyData.getJSONArray(date)
                    for (i in 0 until subjects.length()) {
                        val subjectNow = subjects.getJSONObject(i)
                        val subjectName = Utils.getShortName(subjectNow.getString("name"))
                        val subjectGrade = subjectNow.getDouble("grade").toFloat()
                        if (subjectGrade == 0.0f) continue
                        val entry = Entry(floatDate, subjectGrade)
                        if (organizedData[subjectName] == null) organizedData.put(subjectName, ArrayList<Entry>())
                        if (lastData[subjectName] == null) lastData.put(subjectName, Entry(floatDate, subjectGrade))
                        else lastData[subjectName] = entry
                        val subjectItem = organizedData[subjectName]!!
                        if (subjectItem.size != 0 && abs(subjectGrade - subjectItem.last().y) < 1e-5) continue
                        subjectItem.add(entry)
                    }
                }
                for ((name, grade) in lastData) {
                    organizedData[name]!!.add(grade)
                }

                val colorList = intArrayOf(
                        Color.parseColor("#ff1744"),
                        Color.parseColor("#f50057"),
                        Color.parseColor("#d500f9"),
                        Color.parseColor("#651fff"),
                        Color.parseColor("#3d5afe"),
                        Color.parseColor("#00b0ff"),
                        Color.parseColor("#00e5ff"),
                        Color.parseColor("#00e676"),
                        Color.parseColor("#76ff03"),
                        Color.parseColor("#c6ff00"),
                        Color.parseColor("#ffea00"),
                        Color.parseColor("#ffc400"),
                        Color.parseColor("#ff3d00"),
                        Color.parseColor("#ff9100"),
                        Color.parseColor("#3e2723")
                )
                for ((count, subjectName) in organizedData.keys.withIndex()) {
                    val dataSet = LineDataSet(organizedData[subjectName], subjectName)
                    dataSet.color = colorList[count]
                    dataSet.circleColors = List((organizedData[subjectName] as ArrayList<Entry>).size, { colorList[count] })
                    dataSet.valueTextColor = Color.BLACK
                    dataSet.lineWidth = 2.0f
                    lineData.addDataSet(dataSet)
                }

                lineChart.data = lineData
                lineChart.invalidate()

                val xAxis = lineChart.xAxis
                xAxis.setDrawAxisLine(true)
                xAxis.setDrawGridLines(true)
                xAxis.enableGridDashedLine(10f, 10f, 0f)
                xAxis.granularity = 1f
                xAxis.valueFormatter = object : IAxisValueFormatter {

                    private val mFormat = SimpleDateFormat("MM/dd", Locale.CHINA)

                    override fun getFormattedValue(value: Float, axis: AxisBase): String {
                        val millis = TimeUnit.DAYS.toMillis(value.toLong())
                        return mFormat.format(Date(millis))
                    }
                }

                val yAxis = lineChart.axisLeft
                yAxis.enableGridDashedLine(10f, 10f, 0f)
                lineChart.axisRight.enableGridDashedLine(10f, 10f, 0f)
                lineChart.legend.form = Legend.LegendForm.LINE

            }
        }
        return view
    }
}
