package com.carbonylgroup.schoolpower.fragments

import android.app.Fragment
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.MainActivity
import com.carbonylgroup.schoolpower.data.Subject
import com.carbonylgroup.schoolpower.utils.Utils
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ChartFragment : Fragment() {

    private lateinit var utils: Utils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.fragment_chart, container, false)
        MainActivity.of(activity).expandToolBar(true, true)
        utils = Utils(activity)

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
                    for (i in 0..subjects.length() - 1) {
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
            run {

                val radarChart: RadarChart = view.findViewById(R.id.radar_chart)
                val entries = ArrayList<RadarEntry>()
                radarChart.description.isEnabled = false
                val xAxis = radarChart.xAxis
                xAxis.yOffset = 10f
                xAxis.xOffset = 10f
                xAxis.valueFormatter = object : IAxisValueFormatter {

                    private val mSubjectsName = ArrayList<String>()

                    init {
                        data.mapTo(mSubjectsName) { it.getShortName() }
                    }

                    override fun getFormattedValue(value: Float, axis: AxisBase): String {
                        return mSubjectsName[value.toInt() % mSubjectsName.size]
                    }
                }
                var minGrade = 100.0f
                for (it in data) {
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
                set.color = Color.parseColor("#345995")
                set.fillColor = Color.parseColor("#345995")
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
                radarData.setValueTextColor(ContextCompat.getColor(activity, R.color.primary))

                radarChart.legend.isEnabled = false

                radarChart.data = radarData
                //radarChart.invalidate()

                radarChart.animateX(1000)
            }
        }
        return view
    }
}
