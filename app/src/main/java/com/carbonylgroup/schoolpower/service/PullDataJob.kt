package com.carbonylgroup.schoolpower.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import androidx.core.app.NotificationCompat
import android.util.Log
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.MainActivity
import com.carbonylgroup.schoolpower.data.Attendance
import com.carbonylgroup.schoolpower.data.StudentData
import com.carbonylgroup.schoolpower.data.Subject
import com.carbonylgroup.schoolpower.utils.Utils
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MultipartBody
import okhttp3.Response
import java.io.IOException
import java.util.*


class PullDataJob : JobService() {

    lateinit var utils: Utils

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
                    var grade = "--"
                    val maxGrade = item.maximumScore

                    for (it in oldAssignmentListCollection) {

                        if (it.title == item.title && it.date == item.date) {
                            newItem = false
                            if (it.score == item.score) newGrade = false
                            else grade = item.getScoreString()
                            break
                        }
                    }
                    if (newItem) {
                        if(item.score != null) {
                            newGrade = true
                            grade = item.getScoreString()
                        } else newGrade = false
                    }

                    val preference = utils.getPreferences()

                    // send a notification when
                    // 1. there is a new grade (either because of a new assignment with a grade or an old assignment with a new grade)
                    // 2. there is a new assignment without grade but the user opts to display it
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
            val nBuilder = NotificationCompat.Builder(this@PullDataJob, "assignments")
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
            val newItem = oldAttendances.none { it.name == item.name && it.date == item.date && it.code == item.code }

            if (newItem) updatedAttendances.add(item.name + " - " + item.description)
        }

        if (updatedAttendances.size != 0) {
            val stackBuilder = TaskStackBuilder.create(this@PullDataJob)
            stackBuilder.addParentStack(MainActivity::class.java)
            stackBuilder.addNextIntent(Intent(this@PullDataJob, MainActivity::class.java))
            val nBuilder = NotificationCompat.Builder(this@PullDataJob, "attendance")
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
        utils = Utils(this)
        val username = utils.getPreferences(Utils.AccountData).getString(getString(R.string.usernameKEY), "")!!
        val password = utils.getPreferences(Utils.AccountData).getString(getString(R.string.passwordKEY), "")!!

        Log.d("PullDataJob", "onStartJob")

        val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", username)
                .addFormDataPart("password", password)
                .addFormDataPart("version", utils.getAppVersion())
                .addFormDataPart("action", "pull_data_job")
                .addFormDataPart("os", "android")
                .build()
        var retried = false
        utils.buildNetworkRequest(getString(R.string.postURL), "POST", body)
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        val backupServer = utils.getBackupServerUrl("pull_data_2")
                        if (!retried && backupServer != null) { // automatically retry when failed
                            retried = true
                            try {
                                val response = utils.buildNetworkRequest(backupServer, "POST", body).execute()
                                onResponse(call, response)
                            } catch (e: IOException) {
                            }
                            return
                        }
                        // no connection, try to run the job again later.
                        jobFinished(params, true)
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        try {
                            val strMessage = try{
                                response.body()!!.string().replace("\n", "")
                            }catch(e: Exception){
                                Log.d("PullDataJob", "Job Finished Early ${e.message}")
                                jobFinished(params, true)
                                return
                            }
                            response.close()
                            if (strMessage.contains("Something went wrong!") // incorrect username or password
                                    || !strMessage.contains("{")) { // unknown error

                                Log.d("PullDataJob", "Job Finished Early $strMessage")
                                jobFinished(params, false)
                                return

                            }
                            val newData = try {
                                StudentData(this@PullDataJob, strMessage, utils)
                            } catch (e: Exception) {
                                Log.d("PullDataJob", "Job Finished Early $strMessage, ${e.message}")
                                jobFinished(params, false)
                                return
                            }
                            try {
                                val oldData = utils.readDataArrayList()

                                diffSubjects(oldData.subjects, newData.subjects)
                                diffAttendances(oldData.attendances, newData.attendances)

                            }catch(e: Exception){
                                e.printStackTrace()
                                Log.d("PullDataJob", "Job Finished Early ${e.message}")
                                jobFinished(params, false)
                                return
                            }
                            Log.d("PullDataJob", "Job Finished Normally")
                            jobFinished(params, false)
                        } catch (e: Exception) {
                            utils.errorHandler(e)
                        }
                    }
                })

        return true
    }

    override fun onStopJob(params: JobParameters): Boolean = false
}
