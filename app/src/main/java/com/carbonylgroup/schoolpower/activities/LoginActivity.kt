/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.EditText
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

    private lateinit var utils: Utils

    override fun initActivity() {

        super.initActivity()
        if (checkIfLoggedIn()) return
        setContentView(R.layout.login_content)

        initDialog()
        utils = Utils(this)

        findViewById<View>(R.id.login_fab).setOnClickListener {
            val username = findViewById<EditText>(R.id.input_username).text.toString()
            val password = findViewById<EditText>(R.id.input_password).text.toString()
            utils.getSharedPreference(Utils.AccountData).edit()
                    .putString(getString(R.string.user_id), username)
                    .apply()
            loginAction(username, password)
        }

        utils.checkApplicationUpdate()
    }

    private fun checkIfLoggedIn(): Boolean {

        val sharedPreferences = getSharedPreferences(getString(R.string.accountData), Activity.MODE_PRIVATE)
        if (sharedPreferences.getBoolean(getString(R.string.loggedIn), false)) {
            startMainActivity()
            return true
        }
        return false
    }

    private fun initDialog() {

        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.only_alert))
        builder.setTitle(getString(R.string.notification))
        builder.setPositiveButton(getString(R.string.i_understand), null)
        builder.create().show()
    }

    private fun loginAction(username: String, password: String) {

        val progressDialog = ProgressDialog(this@LoginActivity)
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setMessage(getString(R.string.authenticating))
        progressDialog.show()
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
                        if(!retried && backupServer!=null) {
                            retried = true
                            try {
                                val response = utils.buildNetworkRequest(backupServer, "POST", body).execute()
                                onResponse(call, response)
                            }catch(e:IOException){}
                            return
                        }
                        progressDialog.dismiss()
                        utils.showSnackBar(findViewById(R.id.login_coordinate_layout), getString(R.string.no_connection), true)
                    }
                    override fun onResponse(call: Call, response: Response) {

                        val strMessage = response.body()!!.string().replace("\n", "")

                        // Error happened. Usually caused by wrong username/password
                        if (strMessage.contains("Something went wrong!")) {
                            utils.showSnackBar(findViewById(R.id.login_coordinate_layout), getString(R.string.wrong_password), true)
                            Log.w("Login", strMessage)
                            progressDialog.dismiss()
                            return
                        }
                        if (strMessage.contains("\"alert\"")) {
                            utils.showSnackBar(findViewById(R.id.login_coordinate_layout), JSONObject(strMessage)["alert"].toString(), true)
                            progressDialog.dismiss()
                            return
                        }
                        if (!strMessage.contains("{")) {
                            utils.showSnackBar(findViewById(R.id.login_coordinate_layout), getString(R.string.no_connection), true)
                            progressDialog.dismiss()
                        }

                        val data = StudentData(this@LoginActivity, strMessage)

                        utils.getSharedPreference(Utils.AccountData).edit()
                                .putString(getString(R.string.usernameKEY), username)
                                .putString(getString(R.string.passwordKEY), password)
                                .putBoolean(getString(R.string.loggedIn), true)
                                .putString(getString(R.string.student_name), data.studentInfo.getFullName())
                                .apply()

                        utils.saveDataJson(strMessage)
                        utils.saveHistoryGrade(data.subjects)

                        startMainActivity()
                        progressDialog.dismiss()
                    }
                })
    }

    private fun startMainActivity() {

        startActivity(Intent(application, MainActivity::class.java))
        this@LoginActivity.finish()
    }
}
