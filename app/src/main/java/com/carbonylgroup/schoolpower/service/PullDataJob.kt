package com.carbonylgroup.schoolpower.service

import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.MainActivity
import com.carbonylgroup.schoolpower.data.Attendance
import com.carbonylgroup.schoolpower.data.Subject
import com.carbonylgroup.schoolpower.utils.PostData
import com.carbonylgroup.schoolpower.utils.Utils
import java.util.*


class PullDataJob : JobService() {

    private fun diffSubjects(oldSubjects: List<Subject>, newSubject: List<Subject>) {

        val updatedSubjects = ArrayList<String>()
        val updatedGradedSubjects = ArrayList<String>()
        // Mark new or changed assignments
        if (newSubject.size == oldSubjects.size) {

            for (i in newSubject.indices) {

                val newAssignmentListCollection = newSubject[i].assignments
                val oldAssignmentListCollection = oldSubjects[i].assignments
                for (item in newAssignmentListCollection) {
                    var newItem = true
                    var newGrade = true
                    var grade = ""
                    val maxGrade = item.maximumScore

                    for (it in oldAssignmentListCollection) {

                        if (it.title == item.title && it.date == item.date) {
                            newItem = false
                            if (it.score == item.score) newGrade = false
                            else grade = it.score
                        }
                    }
                    if (newItem && item.score != "--") {
                        newGrade = true
                        grade = item.score
                    }

                    val preference = PreferenceManager.getDefaultSharedPreferences(applicationContext)

                    if (newGrade || (newItem && preference.getBoolean("notification_show_no_grade_assignment", true))) {
                        if (newGrade && preference.getBoolean("notification_show_grade", true))
                            updatedGradedSubjects.add(item.title + " ($grade/$maxGrade)")
                        else
                            updatedSubjects.add(item.title)
                    }
                }
            }
        }

        if (updatedSubjects.size != 0 || updatedGradedSubjects.size != 0) {

            val allUpdated = updatedSubjects
            allUpdated.addAll(updatedGradedSubjects)

            val stackBuilder = TaskStackBuilder.create(this@PullDataJob)
            stackBuilder.addParentStack(MainActivity::class.java)
            stackBuilder.addNextIntent(Intent(this@PullDataJob, MainActivity::class.java))
            val nBuilder = NotificationCompat.Builder(this@PullDataJob, "data updated")
                    .setContentTitle(allUpdated.count().toString() + " " + getString(R.string.notification_new))
                    .setContentText(allUpdated.joinToString(", "))
                    .setSmallIcon(R.drawable.icon_light)
                    .setContentIntent(stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT))
                    .setAutoCancel(true)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(1, nBuilder.build())
            Log.d("PullDataJob", "Notification Sent (Subjects)")
        }
    }

    private fun diffAttendances(oldAttendances: List<Attendance>, newAttendances: List<Attendance>) {

        val updatedAttendances = ArrayList<String>()

        for (item in newAttendances) {
            val newItem = oldAttendances.none { it -> it.name == item.name && it.date == item.date && it.code == item.code }

            if (newItem) updatedAttendances.add(item.name + " - " + item.description)
        }

        if (updatedAttendances.size != 0) {
            val stackBuilder = TaskStackBuilder.create(this@PullDataJob)
            stackBuilder.addParentStack(MainActivity::class.java)
            stackBuilder.addNextIntent(Intent(this@PullDataJob, MainActivity::class.java))
            val nBuilder = NotificationCompat.Builder(this@PullDataJob, "data updated")
                    .setContentTitle(getString(R.string.attendance_new))
                    .setContentText(updatedAttendances.joinToString(", "))
                    .setSmallIcon(R.drawable.icon_light)
                    .setContentIntent(stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT))
                    .setAutoCancel(true)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(2, nBuilder.build())
            Log.d("PullDataJob", "Notification Sent (Attendances)")
        }
    }

    override fun onStartJob(params: JobParameters): Boolean {

        val username = getSharedPreferences("accountData", Activity.MODE_PRIVATE).getString(getString(R.string.usernameKEY), "")
        val password = getSharedPreferences("accountData", Activity.MODE_PRIVATE).getString(getString(R.string.passwordKEY), "")
        val utils = Utils(this)

        Log.d("PullDataJob", "onStartJob")

        class HandleData : Handler() {
            override fun handleMessage(msg: Message) {
                val strMessage = msg.obj.toString().replace("\n", "")

                when {
                    strMessage.contains("Something went wrong!") ->
                        // incorrect username or password
                        jobFinished(params, false)
                    strMessage == "" ->
                        // no connection, try to run the job again later.
                        jobFinished(params, true)
                    strMessage.contains(getString(R.string.json_begin)) ->
                    {
                        val newData = utils.parseJsonResult(strMessage)
                        val oldData = utils.readDataArrayList()

                        diffSubjects(oldData.subjects, newData.subjects)
                        diffAttendances(oldData.attendances, newData.attendances)

                        Log.d("PullDataJob", "Job Finished Normally")
                        jobFinished(params, false)
                    }
                    else -> // unknown error
                        jobFinished(params, false)
                }
            }
        }
        val version = packageManager.getPackageInfo("com.carbonylgroup.schoolpower", 0).versionName

        Thread(PostData(
                getString(R.string.postURL),
                "username=$username&password=$password&version=$version&action=pull_data_job&os=android",
                HandleData())).start()

        return true
    }

    override fun onStopJob(params: JobParameters): Boolean = false
}
