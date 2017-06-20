package com.carbonylgroup.schoolpower.fragments

import android.os.Bundle
import android.app.Fragment
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.MainActivity
import com.carbonylgroup.schoolpower.classes.Utils.Utils
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.xml.datatype.DatatypeConstants.HOURS

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
        val data = MainActivity.of(activity).dataList!!

        run {
            val historyData = utils.readHistoryGrade()

            val lineChart = view.findViewById(R.id.line_chart) as LineChart
            lineChart.description.isEnabled = false

            // Map<SubjectName, Array<Entry<Date, Grade>>>
            val organizedData = HashMap<String, ArrayList<Entry>>()

            val lineData = LineData()
            for (date in historyData.keys()){
                val floatDate = (SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(date).time / 1000.0f / 60.0f / 60.0f / 24.0f).toInt().toFloat()
                val subjects = historyData.getJSONArray(date)
                for (i in 0..subjects.length() - 1){
                    val subjectNow = subjects.getJSONObject(i)
                    val subjectName = Utils.getShortName(subjectNow.getString("name"))
                    val subjectGrade = subjectNow.getDouble("grade")
                    if(organizedData[subjectName]==null) organizedData.put(subjectName,ArrayList<Entry>())
                    organizedData[subjectName]!!.add(Entry(floatDate, subjectGrade.toFloat()))
                }
            }

            val colorList = intArrayOf(Color.BLUE,Color.CYAN,Color.RED,Color.GREEN,Color.LTGRAY,Color.MAGENTA,Color.YELLOW,Color.DKGRAY,Color.GRAY)
            for ((count, subjectName) in organizedData.keys.withIndex()){
                val dataSet = LineDataSet(organizedData[subjectName], subjectName)
                dataSet.color = colorList[count]
                dataSet.circleColors = List((organizedData[subjectName] as ArrayList<Entry>).size, {colorList[count]})
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

            val radarChart = view.findViewById(R.id.radar_chart) as RadarChart
            val entries = ArrayList<RadarEntry>()
            radarChart.description.isEnabled = false
            val xAxis = radarChart.xAxis
            xAxis.yOffset = 10f
            xAxis.xOffset = 10f
            xAxis.valueFormatter = object : IAxisValueFormatter {

                private val mSubjectsName = ArrayList<String>()
                init{data.mapTo(mSubjectsName){it.getShortName()}}

                override fun getFormattedValue(value: Float, axis: AxisBase): String {
                    return mSubjectsName[value.toInt() % mSubjectsName.size]
                }
            }
            var minGrade=100.0f
            for(it in data){
                val periodGrade=utils.getLatestItem(it)!!.getIntegerGrade().toFloat()
                entries.add(RadarEntry(periodGrade))
                if(periodGrade<minGrade) minGrade=periodGrade
            }

            val yAxis = radarChart.yAxis
            yAxis.textSize = 9f
            yAxis.axisMinimum = minGrade/3*2
            yAxis.axisMaximum = 110.0f - 20.0f
            yAxis.setDrawLabels(false)

            val set = RadarDataSet(entries, "Grades")
            set.color = ContextCompat.getColor(activity, R.color.A_score_green)
            set.fillColor = ContextCompat.getColor(activity, R.color.A_score_green)
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

            radarChart.data = radarData
            //radarChart.invalidate()

            radarChart.animateX(1000)
        }
        return view
    }
}
