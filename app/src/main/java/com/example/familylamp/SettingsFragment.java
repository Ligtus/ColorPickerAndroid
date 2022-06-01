package com.example.familylamp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        Preference vibration = findPreference("vibration");

        Context context = getActivity();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (prefs.getBoolean("vibration", true)) {
            findPreference("vibrationTime").setEnabled(true);
        } else {
            findPreference("vibrationTime").setEnabled(false);
        }

        vibration.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue.toString().equals("true")) {
                    findPreference("vibrationTime").setEnabled(true);
                } else {
                    findPreference("vibrationTime").setEnabled(false);
                }
                return true;
            }
        });
    }
}