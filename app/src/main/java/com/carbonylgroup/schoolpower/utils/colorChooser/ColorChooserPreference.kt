package com.carbonylgroup.schoolpower.utils.colorChooser

import android.content.Context
import android.graphics.PorterDuff
import android.support.annotation.ColorInt
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceViewHolder
import android.util.AttributeSet
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.BaseActivity
import com.carbonylgroup.schoolpower.utils.Utils
import java.util.*


/**
 * Created on 2017/8/4.
 *
 * @author ThirtyDegreesRay
 */

class ColorChooserPreference : Preference, ColorChooserDialog.ColorCallback {

    private var colorChooserCallback: ColorChooserCallback? = null
    private var oriColor: Int = 0
    private lateinit var utils: Utils

    private val accentColors: IntArray
        get() = context.resources.getIntArray(R.array.accent_color_array)

    private val selectedColor: Int
        get() = getColorByIndex(utils.getAccentColorIndex())

    private val colorList: List<Int>
        get() {
            val colorsResId = context.resources.getIntArray(R.array.accent_color_array)
            val list = ArrayList<Int>()
            for (i in colorsResId.indices) {
                list.add(colorsResId[i])
            }
            return list
        }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    fun setColorChooserCallback(colorChooserCallback: ColorChooserCallback) {
        this.colorChooserCallback = colorChooserCallback
    }

    private fun init() {
        widgetLayoutResource = R.layout.preference_widget_color
        utils = Utils(context)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val colorView = holder.findViewById(R.id.color_view)
        colorView.setBackgroundResource(R.drawable.shape_circle)
        colorView.background.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN)
    }

    override fun onClick() {
        super.onClick()
        oriColor = selectedColor
        ColorChooserDialog.Builder(BaseActivity.getCurActivity(), this, R.string.choose_accent_color)
                .titleSub(R.string.choose_accent_color)
                .customColors(accentColors, null)
                .preselect(oriColor)
                .customButton(0)
                .doneButton(0)
                .cancelButton(0)
                .accentMode(true)
                .show()
    }

    override fun onColorSelection(dialog: ColorChooserDialog, @ColorInt selectedColor: Int) {
        utils[Utils.ACCENT_COLOR] = getColorIndex(selectedColor)
        //        colorView.getBackground().setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN);
        if (colorChooserCallback != null && oriColor != selectedColor) {
            colorChooserCallback!!.onColorChanged(oriColor, selectedColor)
        }
        dialog.dismiss()
    }

    override fun onColorChooserDismissed(dialog: ColorChooserDialog) {

    }

    private fun getColorByIndex(index: Int): Int {
        return colorList[index]
    }

    private fun getColorIndex(color: Int): Int {
        return colorList.indexOf(color)
    }

    interface ColorChooserCallback {
        fun onColorChanged(@ColorInt oriColor: Int, @ColorInt selectedColor: Int)
    }
}
