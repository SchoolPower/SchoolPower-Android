package com.carbonylgroup.schoolpower.utils

import android.app.Activity
import android.content.Context
import android.support.annotation.StyleRes
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.BaseActivity


/**
 * Created on 2017/10/30 13:26:25
 * Copied from Copyright (C) 2017 Kosh.
 * Modified by Copyright (C) 2017 ThirtyDegreesRay.
 */

class ThemeHelper(private val activity: BaseActivity) {

    val utils = Utils(activity)

    internal fun apply() {
        val theme = utils.getTheme()
//        val accentColor = utils.getAccentColor()
        activity.setTheme(getTheme(theme, utils.getAccentColorIndex()))
    }

//    fun applyForAboutActivity(activity: Activity) {
//        val theme = utils.getTheme()
//        activity.setTheme(getAboutTheme(theme))
//    }

//    @StyleRes
//    fun getAboutTheme(theme: String): Int {
//        when (theme) {
//            utils.LIGHT -> return R.style.ThemeLight_AboutActivity
//            utils.DARK -> return R.style.ThemeDark_AboutActivity
//            else -> return R.style.ThemeLight_AboutActivity
//        }
//    }

    @StyleRes
    fun getTheme(theme: String, accentColor: Int): Int {
        when (theme) {

            utils.LIGHT -> {
                when (accentColor) {
                    utils.LIGHT_BLUE -> return R.style.ThemeLight_LightBlue
                    utils.BLUE -> return R.style.ThemeLight_Blue
                    utils.INDIGO -> return R.style.ThemeLight_Indigo
                    utils.ORANGE -> return R.style.ThemeLight_Orange
                    utils.YELLOW -> return R.style.ThemeLight_Yellow
                    utils.AMBER -> return R.style.ThemeLight_Amber
                    utils.GREY -> return R.style.ThemeLight_Grey
                    utils.BROWN -> return R.style.ThemeLight_Brown
                    utils.CYAN -> return R.style.ThemeLight_Cyan
                    utils.TEAL -> return R.style.ThemeLight_Teal
                    utils.LIME -> return R.style.ThemeLight_Lime
                    utils.GREEN -> return R.style.ThemeLight_Green
                    utils.PINK -> return R.style.ThemeLight_Pink
                    utils.RED -> return R.style.ThemeLight_Red
                    utils.PURPLE -> return R.style.ThemeLight_Purple
                    utils.DEEP_PURPLE -> return R.style.ThemeLight_DeepPurple
                }
                when (accentColor) {
                    utils.LIGHT_BLUE -> return R.style.ThemeDark_LightBlue
                    utils.BLUE -> return R.style.ThemeDark_Blue
                    utils.INDIGO -> return R.style.ThemeDark_Indigo
                    utils.ORANGE -> return R.style.ThemeDark_Orange
                    utils.YELLOW -> return R.style.ThemeDark_Yellow
                    utils.AMBER -> return R.style.ThemeDark_Amber
                    utils.GREY -> return R.style.ThemeDark_Grey
                    utils.BROWN -> return R.style.ThemeDark_Brown
                    utils.CYAN -> return R.style.ThemeDark_Cyan
                    utils.TEAL -> return R.style.ThemeDark_Teal
                    utils.LIME -> return R.style.ThemeDark_Lime
                    utils.GREEN -> return R.style.ThemeDark_Green
                    utils.PINK -> return R.style.ThemeDark_Pink
                    utils.RED -> return R.style.ThemeDark_Red
                    utils.PURPLE -> return R.style.ThemeDark_Purple
                    utils.DEEP_PURPLE -> return R.style.ThemeDark_DeepPurple
                }
            }
            utils.DARK -> when (accentColor) {
                utils.LIGHT_BLUE -> return R.style.ThemeDark_LightBlue
                utils.BLUE -> return R.style.ThemeDark_Blue
                utils.INDIGO -> return R.style.ThemeDark_Indigo
                utils.ORANGE -> return R.style.ThemeDark_Orange
                utils.YELLOW -> return R.style.ThemeDark_Yellow
                utils.AMBER -> return R.style.ThemeDark_Amber
                utils.GREY -> return R.style.ThemeDark_Grey
                utils.BROWN -> return R.style.ThemeDark_Brown
                utils.CYAN -> return R.style.ThemeDark_Cyan
                utils.TEAL -> return R.style.ThemeDark_Teal
                utils.LIME -> return R.style.ThemeDark_Lime
                utils.GREEN -> return R.style.ThemeDark_Green
                utils.PINK -> return R.style.ThemeDark_Pink
                utils.RED -> return R.style.ThemeDark_Red
                utils.PURPLE -> return R.style.ThemeDark_Purple
                utils.DEEP_PURPLE -> return R.style.ThemeDark_DeepPurple
            }
        }
        return R.style.ThemeLight_Cyan
    }
}
