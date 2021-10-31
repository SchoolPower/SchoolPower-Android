/**
 * Copyright (C) 2019 SchoolPower Studio
 */

package com.carbonylgroup.schoolpower.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.AppCompatTextView
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.data.StudentData
import com.carbonylgroup.schoolpower.utils.Utils
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MultipartBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class LoginActivity : BaseActivity() {

    override fun initActivity() {
        super.initActivity()
        setContentView(R.layout.login_content)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.primary)
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary)

        hideProgress()
        initDialog()

        findViewById<View>(R.id.login_fab).setOnClickListener {
            val username = findViewById<EditText>(R.id.input_username).text.toString()
            val password = findViewById<EditText>(R.id.input_password).text.toString()
            utils.getPreferences(Utils.AccountData).edit()
                    .putString(getString(R.string.user_id), username)
                    .apply()
            if (username != "")
                loginAction(username, password)
            else
                utils.showSnackBar(findViewById(R.id.login_coordinate_layout), getString(R.string.wrong_password), true)

        }
    }

    private fun initDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.only_alert))
        builder.setTitle(getString(R.string.notification))
        builder.setPositiveButton(getString(R.string.i_understand), null)
        builder.create().show()
    }

    private fun showProgress() {
        runOnUiThread {
            findViewById<ProgressBar>(R.id.login_progress_bar).visibility = View.VISIBLE
            findViewById<LinearLayout>(R.id.login_scroll).visibility = View.INVISIBLE
            findViewById<AppCompatTextView>(R.id.login_copyright).visibility = View.INVISIBLE
        }
    }

    private fun hideProgress() {
        runOnUiThread {
            findViewById<ProgressBar>(R.id.login_progress_bar).visibility = View.GONE
            findViewById<LinearLayout>(R.id.login_scroll).visibility = View.VISIBLE
            findViewById<AppCompatTextView>(R.id.login_copyright).visibility = View.VISIBLE
        }
    }

    private fun hideSoftKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun loginAction(username: String, password: String) {

        hideSoftKeyboard()
        showProgress()

        val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", username)
                .addFormDataPart("password", password)
                .addFormDataPart("version", utils.getAppVersion())
                .addFormDataPart("action", "login")
                .addFormDataPart("os", "android")
                .build()

        var retried = false
        utils.buildNetworkRequest(getString(R.string.postURL), "POST",
                body).enqueue(
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        val backupServer = utils.getBackupServerUrl("pull_data_2")
                        if (!retried && backupServer != null) {
                            retried = true
                            try {
                                val response = utils.buildNetworkRequest(backupServer, "POST", body).execute()
                                onResponse(call, response)
                            } catch (e: IOException) {
                            }
                            return
                        }
                        hideProgress()
                        utils.showSnackBar(findViewById(R.id.login_coordinate_layout), getString(R.string.no_connection), true)
                    }

                    override fun onResponse(call: Call, response: Response) {

                        val strMessage = response.body()!!.string().replace("\n", "")
                        response.close()
                        // Wrong username/password
                        if (strMessage.contains("Invalid Username or password")) {
                            runOnUiThread {
                                utils.showSnackBar(findViewById(R.id.login_coordinate_layout), getString(R.string.wrong_password), true)
                                Log.w("Login", strMessage)
                                hideProgress()
                            }
                            return
                        }
                        if (strMessage.contains("\"alert\"")) {
                            runOnUiThread {
                                utils.showSnackBar(findViewById(R.id.login_coordinate_layout), JSONObject(strMessage)["alert"].toString(), true)
                                hideProgress()
                            }
                            return
                        }
                        if (!strMessage.contains("{")) {
                            runOnUiThread {
                                utils.showSnackBar(findViewById(R.id.login_coordinate_layout), getString(R.string.no_connection), true)
                                hideProgress()
                            }
                            return
                        }

                        val data =
                                try { StudentData(this@LoginActivity, strMessage, utils) }
                                catch (e: IllegalArgumentException){
                                    if(e.message?.contains("ERROR_ACCOUNT_DISABLED") == true) {
                                        runOnUiThread {
                                            AlertDialog.Builder(this@LoginActivity)
                                                    .setMessage(getString(R.string.account_disabled))
                                                    .setTitle(getString(R.string.account_disabled_desc))
                                                    .setPositiveButton(getString(R.string.alright), null)
                                                    .create().show()
                                        }
                                    } else {
                                        utils.errorHandler(e, getString(R.string.fatel_error_server_side),
                                                getString(R.string.fatel_error_server_side_message) + e.message)
                                    }
                                    runOnUiThread {
                                        hideProgress()
                                    }
                                    null
                                } ?: return
                        utils.getPreferences(Utils.AccountData).edit()
                                .putString(getString(R.string.usernameKEY), username)
                                .putString(getString(R.string.passwordKEY), password)
                                .putBoolean(getString(R.string.loggedIn), true)
                                .putString(getString(R.string.student_name), data.studentInfo.getFullName())
                                .apply()

                        utils.saveDataJson(strMessage)
                        utils.saveHistoryGrade(data.subjects)

                        startMainActivity()
                    }
                })
    }

    private fun startMainActivity() {
        startActivity(Intent(application, MainActivity::class.java))
        this@LoginActivity.finish()
    }
}
