package com.carbonylgroup.schoolpower.utils

import android.app.Activity
import android.app.AlertDialog
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.carbonylgroup.schoolpower.R
import java.util.*

class BirthdayDialog(private val activity: Activity) {
    fun show() {
        val birthdayDialog = LayoutInflater.from(activity).inflate(R.layout.birthday_dialog, null)
        val birthdayDialogView = birthdayDialog.findViewById<View>(R.id.birthday_dialog_root_view)
        val birthdayDialogBuilder = AlertDialog.Builder(activity)
        val withSuffix: Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            activity.resources.configuration.locales[0] == Locale.ENGLISH
        } else {
            activity.resources.configuration.locale == Locale.ENGLISH
        }
        birthdayDialogView.findViewById<TextView>(R.id.birthday_dialog_title).text =
                String.format(activity.getString(R.string.happy_birth_title_builder),
                        Utils(activity).getAge(withSuffix))
        birthdayDialogBuilder.setView(birthdayDialogView)
        birthdayDialogBuilder.setPositiveButton(activity.getString(R.string.happy_birth_thank), null)
        birthdayDialogBuilder.create().setCanceledOnTouchOutside(true)
        birthdayDialogBuilder.create().show()
    }
}
