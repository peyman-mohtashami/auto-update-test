package com.example.autoupdatetest1

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        val textView: TextView = findViewById(R.id.version)
        textView.text = getCurrentVersion()
        val i = Intent(this, ScheduledService::class.java)
        startService(i)
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
}