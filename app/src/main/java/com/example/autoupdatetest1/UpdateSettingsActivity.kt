package com.example.autoupdatetest1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class UpdateSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, PreferenceFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class PreferenceFragment : PreferenceFragmentCompat() {

        private val DIALOG_FRAGMENT_TAG = "TimePickerDialog"

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        override fun onDisplayPreferenceDialog(preference: Preference?) {
            if(preference is TimepickerPreference) {
                val timePickerDialog = TimePickerPreferenceDialog.newInstance(preference.key)
                timePickerDialog.setTargetFragment(this, 0)
                timePickerDialog.show(parentFragmentManager, DIALOG_FRAGMENT_TAG)
            }
            else {
                super.onDisplayPreferenceDialog(preference)
            }
        }
    }
}