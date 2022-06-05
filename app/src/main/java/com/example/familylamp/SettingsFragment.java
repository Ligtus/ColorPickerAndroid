package com.example.familylamp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import org.w3c.dom.Text;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

public class SettingsFragment extends PreferenceFragmentCompat {

    SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        Preference delete_all = findPreference("delete_all");



        delete_all.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ConfirmManager cm = new ConfirmManager(getContext());
                cm.confirmDialog(
                        getResources().getString(R.string.delete_all_dialog_title),
                        getResources().getString(R.string.delete_all_dialog_message),
                        getResources().getString(R.string.confirm_delete),
                        new Runnable() {
                            @Override
                            public void run() {
                                deleteAll();
                            }
                        }
                );
                return true;
            }
        });
    }

    private void deleteAll() {
        for (int i = 0; i < MainFragment.recientes.size(); i++) {
            sharedPreferences.edit().remove("color" + i).apply();
        }
        sharedPreferences.edit().putInt("index", 0).apply();
        MainFragment.recientes.clear();
        Toast.makeText(getContext(), R.string.all_colors_deleted, Toast.LENGTH_SHORT).show();
    }
}