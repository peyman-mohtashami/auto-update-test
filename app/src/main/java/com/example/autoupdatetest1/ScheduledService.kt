package com.example.autoupdatetest1

import android.app.Service
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.util.*
import java.util.concurrent.TimeUnit
import kotlinx.serialization.*
import kotlinx.serialization.json.*

class ScheduledService : Service() {
    private val timer: Timer = Timer()
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {

                // Instantiate the RequestQueue.
                val queue = Volley.newRequestQueue(this@ScheduledService)
                val url = RELEASES_URL

                // Request a string response from the provided URL.
                val stringRequest = StringRequest(
                    Request.Method.GET, url,
                    { response ->
                        val browserDownloadUrl = getBrowserDownloadUrl(response)
                        if (browserDownloadUrl !== null) {
                            scheduleOneTimeNotification(
                                5000,
                                browserDownloadUrl.toString().replace("\"", "")
                            )
                        }

                    },
                    {
                        Log.v("ScheduleService", "Error");
                    })

                // Add the request to the RequestQueue.
                queue.add(stringRequest)
            }
        }, 0, 2 * 60 * 1000) //5 Minutes
    }

    fun getBrowserDownloadUrl(response: String): String? {
        println(response)
        // check if there is a new version available
        val currentVersion = getCurrentVersion()
        Log.v(TAG, "Current Version: $currentVersion")

        val responseObject = Json.parseToJsonElement(response);
        if (responseObject.jsonArray.size > 0) {
            val latestRelease = responseObject.jsonArray[0]
            val tagName = latestRelease.jsonObject["tag_name"]?.toString()?.replace("\"", "")
            if (tagName !== currentVersion) {
                return latestRelease.jsonObject["assets"]?.jsonArray?.get(0)?.jsonObject?.getValue("browser_download_url")
                    .toString().replace("\"", "")
            }
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun scheduleOneTimeNotification(initialDelay: Long, browserDownloadUrl: String) {
        val data = Data.Builder()
        //Add parameter in Data class. just like bundle. You can also add Boolean and Number in parameter.
        data.putString(DOWNLOAD_URL_KEY, browserDownloadUrl)
        val work =
            OneTimeWorkRequestBuilder<OneTimeScheduleWorker>()
                .setInputData(data.build())
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build()

        WorkManager.getInstance(this@ScheduledService).enqueue(work)
    }

    private fun getCurrentVersion(): String? {
        val pm = this.packageManager
        var pInfo: PackageInfo? = null
        try {
            pInfo = pm.getPackageInfo(this.packageName, 0)
        } catch (e1: PackageManager.NameNotFoundException) {
            e1.printStackTrace()
        }
        return pInfo!!.versionName
    }

    companion object {
        public const val RELEASES_URL = "https://api.github.com/repos/peyman-mohtashami/auto-update-test/releases"
        private val TAG = ScheduledService::class.qualifiedName
        public const val DOWNLOAD_URL_KEY = "browserDownloadUrl"
    }
}