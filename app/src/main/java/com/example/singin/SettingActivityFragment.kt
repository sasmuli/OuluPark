package com.example.singin

import android.os.Bundle
import com.example.singin.R
import androidx.preference.PreferenceFragmentCompat

class SettingActivityFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }
}