package com.example.autoupdatetest1

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import android.widget.TextView
import androidx.preference.PreferenceManager
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
import org.json.JSONObject

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
                        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@ScheduledService)
                        // val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
                        with (prefs.edit()) {
                            putLong("saved_update_check_timestamp_key", System.currentTimeMillis())
                            apply()
                        }
                        val newVersion = getNewVersion(response)
                        if (newVersion !== null) {

                            // todo
                            if(prefs.getBoolean("settings_update_enable_disable_notification", true)) {
                                scheduleOneTimeNotification(
                                    5000,
                                    newVersion["url"].toString().replace("\"", ""),
                                    newVersion["name"].toString()
                                )
                            }
                        }
                    },
                    {
                        Log.v("ScheduleService", "Error")
                    })

                // Add the request to the RequestQueue.
                queue.add(stringRequest)
            }
        }, 0, PERIOD)
    }

    private fun getNewVersion(response: String): JSONObject? {
        val currentVersion = getCurrentVersion()

        val responseObject = Json.parseToJsonElement(response)
        if (responseObject.jsonArray.size > 0) {
            val latestRelease = responseObject.jsonArray[0]
            val tagName = latestRelease.jsonObject["tag_name"]?.toString()?.replace("\"", "")
            if (!tagName.equals(currentVersion)) {
                val newVersion: JSONObject = JSONObject()
                newVersion.put("url", latestRelease.jsonObject["assets"]?.jsonArray?.get(0)?.jsonObject?.getValue("browser_download_url")
                    .toString().replace("\"", ""))
                newVersion.put("name", tagName)

                return newVersion
            }
        }
        return null
    }

    fun scheduleOneTimeNotification(initialDelay: Long, url: String, version: String) {
        val data = Data.Builder()
        //Add parameter in Data class. just like bundle. You can also add Boolean and Number in parameter.
        data.putString(NEW_VERSION_URL_KEY, url)
        data.putString(NEW_VERSION_NAME_KEY, version)
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
        const val RELEASES_URL = "https://api.github.com/repos/peyman-mohtashami/auto-update-test/releases"
        private val TAG = ScheduledService::class.qualifiedName
        const val NEW_VERSION_URL_KEY = "newVersionUrl"
        const val NEW_VERSION_NAME_KEY = "newVersionName"
        private const val PERIOD: Long = 2 * 60 * 1000 // 2 minutes
    }
}