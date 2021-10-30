package com.carbonylgroup.schoolpower.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.utils.Utils

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils(this).checkApplicationUpdate()
        if (checkIfLoggedIn()) startMainActivity()
        else startLoginActivity()
        finish()
    }

    private fun checkIfLoggedIn(): Boolean {
        val sharedPreferences = getSharedPreferences(Utils.AccountData, Activity.MODE_PRIVATE)
        return (sharedPreferences.getBoolean(getString(R.string.loggedIn), false))
    }

    private fun startLoginActivity() {
        startActivity(Intent(application, LoginActivity::class.java))
    }

    private fun startMainActivity() {
        startActivity(Intent(application, MainActivity::class.java))
    }
}
