package com.example.autoupdatetest1

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        val textView: TextView = findViewById(R.id.version)
        textView.text = getCurrentVersion()

        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // TODO if in the settings page change auto check -> close or start service
        if(prefs.getBoolean("settings_update_enable_disable_auto_check_update", true)) {
            val i = Intent(this, ScheduledService::class.java)
            startService(i)
        }
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.updates -> {
                val intent = Intent(this, UpdatesActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.update_settings -> {
                val intent = Intent(this, UpdateSettingsActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}