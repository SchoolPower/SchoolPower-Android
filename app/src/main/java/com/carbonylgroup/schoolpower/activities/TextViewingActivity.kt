/**
 * Copyright (C) 2019 SchoolPower Studio
 */

package com.carbonylgroup.schoolpower.activities

import android.app.AlertDialog
import android.content.Intent
import android.widget.TextView
import com.carbonylgroup.schoolpower.R
import kotlinx.android.synthetic.main.activity_text_viewing.*
import android.content.Intent.ACTION_SEND
import android.os.StrictMode
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.carbonylgroup.schoolpower.utils.Utils
import java.io.File
import android.support.v4.content.FileProvider


class TextViewingActivity : BaseActivity() {

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun initActivity() {
        super.initActivity()
        setContentView(R.layout.activity_text_viewing)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        this.title = intent.getStringExtra("title")
        findViewById<TextView>(R.id.text_viewing_text).text = intent.getStringExtra("text")

        if (!intent.getBooleanExtra("shareEnabled", false)) fab.hide()
        fab.setOnClickListener { _ ->

            val alertDialog =
                    AlertDialog.Builder(this)
                            .setAdapter(ArrayAdapter<String>(this, R.layout.simple_list_item,
                                    arrayOf(getString(R.string.share_as_file),
                                            getString(R.string.share_as_text))), null)
                            .create()

            alertDialog.listView.setOnItemClickListener { _: AdapterView<*>, _: View, position: Int, _: Long ->
                if (position == 0) shareFile(filesDir.toString() + "/" + Utils.StudentDataFileName)
                else sharePlainText(intent.getStringExtra("text"))
                alertDialog.dismiss()
            }
            alertDialog.show()



        }
    }

    private fun shareFile(path: String) {
        val intentShareFile = Intent(Intent.ACTION_SEND)
        val fileWithinMyDir = File(path)

        if (fileWithinMyDir.exists()) {
            val uri = FileProvider.getUriForFile(this,
                    "com.carbonylgroup.schoolpower", fileWithinMyDir)
            val builder = StrictMode.VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
            grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intentShareFile.type = "application/json"
            intentShareFile.putExtra(Intent.EXTRA_STREAM, uri)
            intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(intentShareFile, getString(R.string.share_as_file)))
        }
    }

    private fun sharePlainText(text: String) {
        val sharingIntent = Intent(ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_as_text)))
    }
}
