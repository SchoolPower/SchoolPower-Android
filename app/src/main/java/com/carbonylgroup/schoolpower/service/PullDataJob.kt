package com.carbonylgroup.schoolpower.service

import android.app.Activity
import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Handler
import android.os.Message
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.utils.PostData
import com.carbonylgroup.schoolpower.utils.Utils
import java.util.*


class PullDataJob : JobService() {

    override fun onStartJob(params: JobParameters): Boolean {
        val username = getSharedPreferences("accountData", Activity.MODE_PRIVATE).getString(getString(R.string.usernameKEY), "")
        val password = getSharedPreferences("accountData", Activity.MODE_PRIVATE).getString(getString(R.string.passwordKEY), "")
        val utils = Utils(this)

        Log.d("PullDataJob", "onStartJob")

        class HandleData : Handler() {
            override fun handleMessage(msg: Message) {
                val strMessage = msg.obj.toString().replace("\n", "")

                if (strMessage.contains("Something went wrong!")) {

                    jobFinished(params, false)
                    // fail to load

                } else if (strMessage.contains(getString(R.string.json_begin))) {

                    utils.saveDataJson(strMessage)
                    val subjects = utils.parseJsonResult(strMessage).second

                    val updatedSubjects = ArrayList<String>()

                    val oldSubjects = utils.readDataArrayList().second
                    // Mark new or changed assignments
                    if (subjects.size == oldSubjects.size) {
                        for (i in subjects.indices) {
                            val newAssignmentListCollection = subjects[i].assignments
                            val oldAssignmentListCollection = oldSubjects[i].assignments
                            for (item in newAssignmentListCollection) {
                                // if no item in oldAssignmentListCollection has the same title, score and date as those of the new one, then the assignment should be marked.
                                val found = oldAssignmentListCollection.any { it.title == item.title && it.score == item.score && it.date == item.date && !it.isNew }
                                if (!found) updatedSubjects.add(item.title)

                            }
                        }
                    }
                    if (updatedSubjects.size != 0) {

                        val nBuilder = NotificationCompat.Builder(this@PullDataJob, "data updated")
                                .setContentTitle("New assignments are out")
                                .setContentText(updatedSubjects.joinToString(", "))
                        nBuilder.build()
                    }

                    jobFinished(params, false)
                } else if (strMessage == "") {

                    // no connection, try to run the job again later.
                    jobFinished(params, true)
                } else {

                    // unknown error
                    jobFinished(params, false)
                }
            }
        }
        val version = packageManager.getPackageInfo("com.carbonylgroup.schoolpower", 0).versionName

        Thread(PostData(
                getString(R.string.postURL),
                "username=$username&password=$password&version=$version&action=pull_data_job",
                HandleData())).start()
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean = false
}
