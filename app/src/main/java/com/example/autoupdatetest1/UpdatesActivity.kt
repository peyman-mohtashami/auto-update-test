package com.example.autoupdatetest1

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.json.JSONObject
import java.io.File
import java.util.*


class UpdatesActivity : AppCompatActivity(), TaskDelegate {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_updates)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // todo get preferences
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        println(
            "****()" + prefs.getBoolean(
                "settings_update_enable_disable_auto_check_update",
                true
            )
        )
        println("****()" + prefs.getBoolean("settings_update_enable_disable_notification", true))
        println("****()" + prefs.getString("setting_update_frequency", "Not yet"))
        println("****()" + prefs.getInt("setting_update_notification_time", 540))
        println("****()" + prefs.getLong("saved_update_check_timestamp_key", 0).toString())

        val notificationMng =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationMng.cancelAll()

        val checkUpdateLinearLayout: LinearLayout = findViewById(R.id.check_update_linearLayout)
        val updateLinearLayout: LinearLayout = findViewById(R.id.update_linearLayout)
        val downloadLinearLayout: LinearLayout = findViewById(R.id.download_linearLayout)

        val lastCheckStatus: TextView = findViewById(R.id.last_check_status)
        val cal: Calendar = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = prefs.getLong(
            "saved_update_check_timestamp_key",
            0
        )
        val date: String = DateFormat.format("MMM dd, yyyy HH:mm:ss", cal).toString()
        lastCheckStatus.text = getString(R.string.last_check, date)

        if (intent.extras?.getBoolean("fromNotification", false) == true){
            checkUpdateLinearLayout.visibility = View.GONE
            updateLinearLayout.visibility = View.VISIBLE

            val updateStatus: TextView = findViewById(R.id.update_status)
            updateStatus.text = getString(
                R.string.new_version_available,
                getString(R.string.app_name),
                intent.extras?.getString(
                    "versionName",
                    ""
                )
            )

            val dontShowAgainButton: Button = findViewById(R.id.dont_show_again_button)
            dontShowAgainButton.visibility = View.VISIBLE
        }

        val currentVersion: TextView = findViewById(R.id.current_version)
        currentVersion.text = getString(R.string.currentVersion, getCurrentVersion())

        val progressBar: ProgressBar = findViewById(R.id.progressBar)

        val checkForUpdateButton: Button = findViewById(R.id.check_for_updates_button)
        checkForUpdateButton.setOnClickListener {
            // Do something in response to button click

            // loader on & button off
            val checkForUpdatesPBar: ProgressBar = findViewById(R.id.check_for_updates_pbar)
            checkForUpdatesPBar.visibility = View.VISIBLE
            checkForUpdateButton.visibility = View.GONE

            // send request for checking latest version
            // Instantiate the RequestQueue.
            val queue = Volley.newRequestQueue(this)
            val url = ScheduledService.RELEASES_URL

            // Request a string response from the provided URL.
            val stringRequest = StringRequest(
                Request.Method.GET, url,
                { response ->
                    // if get response check with current version
                    // go to next layout
                    // set the status text
                    // enable or disable buttons
                    val newVersion = getNewVersion(response)
//                    println(browserDownloadUrl)
                    if (newVersion !== null) {
                        val updateStatus: TextView = findViewById(R.id.update_status)
                        updateStatus.text = getString(
                            R.string.new_version_available,
                            getString(R.string.app_name),
                            newVersion.get(
                                "version"
                            )
                        )
                    } else {
                        val updateStatus: TextView = findViewById(R.id.update_status)
                        updateStatus.text = getString(
                            R.string.new_version_not_available, getString(
                                R.string.app_name
                            )
                        )
                    }

                    checkUpdateLinearLayout.visibility = View.GONE
                    updateLinearLayout.visibility = View.VISIBLE

                    checkForUpdatesPBar.visibility = View.GONE
                    checkForUpdateButton.visibility = View.VISIBLE
                },
                {
                    // show error
                    Log.v("ScheduleService", "Error")
                })

            // Add the request to the RequestQueue.
            queue.add(stringRequest)
        }

        val startDownloadingButton: Button = findViewById(R.id.start_downloading_button)
        startDownloadingButton.setOnClickListener {
            // Do something in response to button click
            checkUpdateLinearLayout.visibility = View.GONE
            updateLinearLayout.visibility = View.GONE
            downloadLinearLayout.visibility = View.VISIBLE

            progressBar.progress = 0;
            println(8888)
            val downloader = DownloadFileFromUrl(this, progressBar, this)
            downloader.setProgressBar(progressBar)
            downloader.execute("https://github.com/peyman-mohtashami/auto-update-test/releases/download/v1.4/app-release.apk")
            println(88889)
        }

        val installButton: Button = findViewById(R.id.install_button)
        installButton.setOnClickListener {

            // Do something in response to button click



            // Now start the standard installation window
            val fileName = "app-release.apk";
            val fileLocation = File(filesDir, fileName);
            println("---" + applicationContext.packageName.toString() + ".provider")
            println("---" + fileLocation)
            val uri = FileProvider.getUriForFile(
                this,
                applicationContext.packageName.toString() + ".provider",
                fileLocation
            )
//            createImageFile()
//            )
//            val intent = Intent(Intent.ACTION_VIEW)
//            intent.setDataAndType(
//                // Uri.fromFile(fileLocation),
//                photoURI,
//                "application/vnd.android.package-archive"
//            )
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
//            pendingIntent.send()

            val install = Intent(Intent.ACTION_VIEW)
            install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            install.setDataAndType(
                uri,
                "application/vnd.android.package-archive"
            )
            startActivity(install)

//            val notificationIntent = Intent(Intent.ACTION_VIEW, photoURI) // Uri.parse(browserDownloadUrl))
////
//        val contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
//            contentIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            startActivity(notificationIntent)

//            val intent = Intent(Intent.ACTION_VIEW)
//            intent.setDataAndType(
//                // Uri.fromFile(fileLocation),
//                photoURI,
//                "application/vnd.android.package-archive"
//            )
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            startActivity(intent)


        }

        val cancelButton: Button = findViewById(R.id.cancel_button)
        cancelButton.setOnClickListener {
            // go back to main page
        }

    }

    override fun taskCompletionResult(result: String?) {
        println("task delegate: $result")
        val installButton: Button = findViewById(R.id.install_button)
        installButton.visibility = View.VISIBLE
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

    private fun getNewVersion(response: String): JSONObject? {
        // check if there is a new version available
        val currentVersion = getCurrentVersion()
//        Log.v(ScheduledService.TAG, "Current Version: $currentVersion")

        val responseObject = Json.parseToJsonElement(response)
        if (responseObject.jsonArray.size > 0) {
            val latestRelease = responseObject.jsonArray[0]
            val tagName = latestRelease.jsonObject["tag_name"]?.toString()?.replace("\"", "")
            if (!tagName.equals(currentVersion)) {
                val newVersion: JSONObject = JSONObject()
                newVersion.put(
                    "url",
                    latestRelease.jsonObject["assets"]?.jsonArray?.get(0)?.jsonObject?.getValue(
                        "browser_download_url"
                    )
                        .toString().replace("\"", "")
                )
                newVersion.put("version", tagName)

                return newVersion
            }
        }
        return null
    }


}

