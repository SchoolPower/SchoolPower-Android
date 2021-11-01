package com.carbonylgroup.schoolpower.utils

import android.app.Activity
import android.app.AlertDialog
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import co.ceryle.segmentedbutton.SegmentedButtonGroup
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.data.Subject
import com.gelitenight.waveview.library.WaveView
import com.github.premnirmal.textcounter.CounterView

class GPADialog(private val activity: Activity, private val subjects: List<Subject>, private val officialGPA: Double?) {

    private val utils = Utils(activity)
    private var currentTerm = 0
    private lateinit var waveView: WaveView
    private lateinit var gpa_dialog_percentage_back: CounterView
    private lateinit var gpa_dialog_percentage_front: CounterView
    private lateinit var gpaDialogSegmented: SegmentedButtonGroup

    fun show(): Boolean {
        if (subjects.count() == 0) return false

        val allPeriods = Utils.sortTerm(utils.getAllPeriods(subjects))
        val temp = utils.getLatestTermNameOverall(subjects)
        currentTerm = if (temp != null) allPeriods.indexOf(temp) else -1
        if (currentTerm == -1) {
            return false
        }

        constructView(allPeriods)
        updateData(calculateGPA(allPeriods[currentTerm]).toFloat(),
                calculateCustomGPA(allPeriods[currentTerm]),
                officialGPA?.toFloat())
        return true
    }

    private fun customGPANotAvailable() {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(activity.getString(R.string.custom_gpa_not_available_because))
        builder.setTitle(activity.getString(R.string.custom_gpa_not_available))
        builder.setPositiveButton(activity.getString(R.string.alright), null)
        builder.create().show()
    }

    private fun updateData(GPAAll: Float, GPACustom: Float, GPAOfficial: Float?) {
        gpaDialogSegmented.setOnClickedButtonListener { position: Int ->
            when (position) {
                0 -> animateWaveAndText(waveView.waterLevelRatio, GPAAll, 100.0f)
                1 -> if (GPACustom.isNaN()) customGPANotAvailable() else
                    animateWaveAndText(waveView.waterLevelRatio, GPACustom, 100.0f)
                2 -> {
                    animateWaveAndText(waveView.waterLevelRatio, GPAOfficial!!, 4.0f, "", "%.4f")
                }
            }
        }

        when (gpaDialogSegmented.position) {
            0 -> animateWaveAndText(waveView.waterLevelRatio, GPAAll, 100.0f)
            1 -> if (GPACustom.isNaN()) customGPANotAvailable() else
                animateWaveAndText(waveView.waterLevelRatio, GPACustom, 100.0f)
            2 -> animateWaveAndText(waveView.waterLevelRatio, GPAOfficial!!, 4.0f, "", "%.4f")
        }
    }

    private fun constructView(allPeriods: List<String>) {

        // construct view
        val gpaDialog = activity.layoutInflater.inflate(R.layout.gpa_dialog, null)
        val gpaDialogView = gpaDialog.findViewById<View>(R.id.gpa_dialog_rootView)
        if (officialGPA?.toFloat()?.isNaN() == true) {
            gpaDialogSegmented = gpaDialogView.findViewById(R.id.gpa_dialog_segmented_without_official)
            gpaDialogView.findViewById<View>(R.id.gpa_dialog_segmented).visibility = View.GONE
        } else {
            gpaDialogSegmented = gpaDialogView.findViewById(R.id.gpa_dialog_segmented)
            gpaDialogView.findViewById<View>(R.id.gpa_dialog_segmented_without_official).visibility = View.GONE
        }
        gpa_dialog_percentage_front = gpaDialogView.findViewById(R.id.gpa_dialog_percentage_front)
        gpa_dialog_percentage_back = gpaDialogView.findViewById(R.id.gpa_dialog_percentage_back)
        gpa_dialog_percentage_front.setAutoStart(false)
        gpa_dialog_percentage_front.setPrefix("")
        gpa_dialog_percentage_back.setAutoStart(false)
        gpa_dialog_percentage_back.setPrefix("")

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
        spinner.setSelection(currentTerm)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                updateData(calculateGPA(allPeriods[pos]).toFloat(),
                        calculateCustomGPA(allPeriods[pos]),
                        officialGPA?.toFloat())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun calculateGPA(term: String): Double {
        var sumGPA = 0.0
        var num = 0
        for (subject in subjects) {

            val periodGrade = subject.grades[term] ?: continue
            sumGPA += periodGrade.getGrade() ?: continue
            num += 1

        }
        return sumGPA / num
    }

    private fun calculateCustomGPA(term: String): Float {
        val customRule = utils.getPreferences().getString("list_preference_custom_gpa_calculate", "all")
        val customSubjects = utils.getPreferences().getStringSet("list_preference_customize_gpa", HashSet())
        if (customSubjects!!.isEmpty()) return Float.NaN

        val grades = ArrayList<Int>()
        for (subject in subjects) {

            if (!customSubjects.contains(subject.name)) continue
            grades.add(subject.grades[term]?.getGrade() ?: continue)

        }

        return if (customRule == "all") {
            grades.average().toFloat()
        } else {
            grades.sortedDescending().take(customRule!!.toInt()).average().toFloat()
        }
    }

    private fun animateWaveAndText(rawStartValue: Float,
                                   rawEndValue: Float,
                                   divider: Float,
                                   textSuffix: String = "%",
                                   textFormat: String = "%.3f") {

        val startValue = if (rawStartValue.isNaN()) 0.0f else rawStartValue * 100
        val endValue =
                if (rawEndValue.isNaN()) 0.0f
                else rawEndValue / divider * 100

        val waveLightColor = utils.getColorByLetterGrade(utils.getLetterGradeByPercentageGrade(endValue))
        val waveDarkColor = utils.getDarkColorByPrimary(waveLightColor)

        gpa_dialog_percentage_front.setStartValue(startValue)
        gpa_dialog_percentage_front.setEndValue(rawEndValue)
        gpa_dialog_percentage_front.setIncrement((rawEndValue - startValue) / 25f) // the amount the number increments at each time interval
        gpa_dialog_percentage_front.setTimeInterval(2) // the time interval (ms) at which the text changes
        gpa_dialog_percentage_front.setSuffix(textSuffix)
        gpa_dialog_percentage_front.setFormatter { _, suffix, value -> String.format(textFormat, value) + suffix }
        gpa_dialog_percentage_back.setStartValue(startValue)
        gpa_dialog_percentage_back.setEndValue(rawEndValue)
        gpa_dialog_percentage_back.setIncrement((rawEndValue - startValue) / 25f) // the amount the number increments at each time interval
        gpa_dialog_percentage_back.setTimeInterval(2) // the time interval (ms) at which the text changes
        gpa_dialog_percentage_back.setSuffix(textSuffix)
        gpa_dialog_percentage_back.setFormatter { _, suffix, value -> String.format(textFormat, value) + suffix }
        gpa_dialog_percentage_front.start()
        gpa_dialog_percentage_back.start()

        waveView.setWaveColor(waveLightColor, waveDarkColor)
        waveView.waterLevelRatio = (endValue / 100)
        WaveHelper(waveView, (startValue / 100), waveView.waveShiftRatio).start()
    }
}
