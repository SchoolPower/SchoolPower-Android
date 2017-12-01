package com.carbonylgroup.schoolpower.utils

import android.app.Activity
import android.app.AlertDialog
import android.opengl.Visibility
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import co.ceryle.segmentedbutton.SegmentedButtonGroup
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.data.Subject
import com.gelitenight.waveview.library.WaveView
import com.github.premnirmal.textcounter.CounterView

/**
 * Created by mlsstu on 11/25/2017.
 */

class GPADialog(private val activity: Activity, private val subjects: List<Subject>, private val officialGPA: Double?) {

    private val utils = Utils(activity)
    private lateinit var waveView: WaveView
    private lateinit var gpa_dialog_percentage_back: CounterView
    private lateinit var gpa_dialog_percentage_front: CounterView
    private lateinit var gpaDialogSegmented: SegmentedButtonGroup
    private val filter = ArrayList<String>()

    fun show() {
        val latestPeriods = HashMap<String, Subject.Grade>()
        val allPeriods = HashSet<String>()
        subjects.indices.forEach { i ->
            val key = utils.getLatestPeriod(subjects[i].grades)?: return@forEach
            latestPeriods[key] = subjects[i].grades[key]!!
            subjects[i].grades.keys.filterTo(allPeriods) { subjects[i].grades[it]!!.letter!="--" }
        }
        val latestPeriod = utils.getLatestPeriod(latestPeriods) ?: return // overall latest period, usually indicate the current term

        constructView(latestPeriod, allPeriods.toList())
        updateData(calculateGPA(latestPeriod, ArrayList()),
                calculateGPA(latestPeriod, filter),
                officialGPA?.toFloat())

    }
    private fun updateData(GPAAll:Float, GPACustom:Float, GPAOfficial:Float?){
        gpaDialogSegmented.setOnClickedButtonPosition({ position: Int ->
            when (position) {
                0 -> animateWaveAndText(waveView.waterLevelRatio, GPAAll)
                1 -> animateWaveAndText(waveView.waterLevelRatio, GPACustom)
                2 -> animateWaveAndText(waveView.waterLevelRatio, GPAOfficial!! / 4.0f)
            }
        })

        when (gpaDialogSegmented.position) {
            0 -> animateWaveAndText(waveView.waterLevelRatio, GPAAll)
            1 -> animateWaveAndText(waveView.waterLevelRatio, GPACustom)
            2 -> animateWaveAndText(waveView.waterLevelRatio, GPAOfficial!! / 4.0f)
        }
    }

    private fun constructView(latestPeriod:String, allPeriods:List<String>) {

        // construct view
        val gpaDialog = activity.layoutInflater.inflate(R.layout.gpa_dialog, null)
        val gpaDialogView = gpaDialog.findViewById<View>(R.id.gpa_dialog_rootView)
        gpaDialogSegmented = gpaDialogView.findViewById(R.id.gpa_dialog_segmented)
        gpa_dialog_percentage_front = gpaDialogView.findViewById(R.id.gpa_dialog_percentage_front)
        gpa_dialog_percentage_back = gpaDialogView.findViewById(R.id.gpa_dialog_percentage_back)
        gpa_dialog_percentage_front.setFormatter({ _, suffix, value -> String.format("%.3f", value) + suffix })
        gpa_dialog_percentage_back.setFormatter({ _, suffix, value -> String.format("%.3f", value) + suffix })
        gpa_dialog_percentage_front.setAutoStart(false)
        gpa_dialog_percentage_front.setPrefix("")
        gpa_dialog_percentage_front.setSuffix("%")
        gpa_dialog_percentage_back.setAutoStart(false)
        gpa_dialog_percentage_back.setPrefix("")
        gpa_dialog_percentage_back.setSuffix("%")

        waveView = gpaDialogView.findViewById(R.id.gpa_Dialog_wave_view)
        waveView.setShapeType(WaveView.ShapeType.CIRCLE)

        gpaDialogSegmented.setPosition(0, 0)

        // construct dialog
        val gpaDialogBuilder = AlertDialog.Builder(activity)
        gpaDialogBuilder.setView(gpaDialogView)
        gpaDialogBuilder.setTitle("GPA")
        gpaDialogBuilder.setPositiveButton(activity.getString(R.string.sweet), null)
        gpaDialogBuilder.create().setCanceledOnTouchOutside(true)
        gpaDialogBuilder.create().show()

        val spinner = gpaDialog.findViewById<Spinner>(R.id.gpa_dialog_term_spinner)
        spinner.adapter =
                ArrayAdapter(activity, R.layout.term_selection_spinner, allPeriods)
        spinner.setSelection(allPeriods.indexOf(latestPeriod))
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                updateData(calculateGPA(allPeriods[pos], ArrayList()),
                        calculateGPA(allPeriods[pos], filter),
                        officialGPA?.toFloat())
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun calculateGPA(term : String, coursesExclude : List<String>) : Float {
        var sumGPA = 0.0f
        var num = 0
        for (subject in subjects) {

            val periodGrade = subject.grades[term] ?: continue
            if (periodGrade.letter == "--") continue
            if (coursesExclude.contains(subject.name)) continue
            sumGPA += periodGrade.percentage.toFloat()
            num += 1

        }
        return sumGPA/num
    }

    private fun animateWaveAndText(rawStartValue: Float, endValue: Float) {

        val startValue = if(rawStartValue.isNaN()) 0.0f else rawStartValue

        val waveLightColor = utils.getColorByLetterGrade(utils.getLetterGradeByPercentageGrade(endValue))
        val waveDarkColor = utils.getDarkColorByPrimary(waveLightColor)

        gpa_dialog_percentage_front.setStartValue(startValue * 100)
        gpa_dialog_percentage_front.setEndValue(endValue)
        gpa_dialog_percentage_front.setIncrement((endValue - startValue * 100) / 25f) // the amount the number increments at each time interval
        gpa_dialog_percentage_front.setTimeInterval(2) // the time interval (ms) at which the text changes
        gpa_dialog_percentage_back.setStartValue(startValue * 100)
        gpa_dialog_percentage_back.setEndValue(endValue)
        gpa_dialog_percentage_back.setIncrement((endValue - startValue * 100) / 25f) // the amount the number increments at each time interval
        gpa_dialog_percentage_back.setTimeInterval(2) // the time interval (ms) at which the text changes
        gpa_dialog_percentage_front.start()
        gpa_dialog_percentage_back.start()

        waveView.setWaveColor(waveLightColor, waveDarkColor)
        waveView.waterLevelRatio = (endValue / 100)
        WaveHelper(waveView, startValue, waveView.waveShiftRatio).start()
    }
}
