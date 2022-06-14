package com.example.familylamp.Fragments;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.familylamp.ConfirmManager;
import com.example.familylamp.R;
import com.example.familylamp.SQLiteHelper;

public class SettingsFragment extends PreferenceFragmentCompat {

    SharedPreferences sharedPreferences;
    Vibrator vibrator;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        // Get delete_all preference
        Preference delete_all = findPreference("delete_all");

        // Set onPreferenceClickListener for delete_all preference
        delete_all.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // Create confirm dialog for deleting all colors
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

        // Get vibration preference
        Preference vibrationSeekBar = findPreference("vibrationTime");

        // Set onPreferenceChangeListener for vibration preference
        vibrationSeekBar.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // On change of the seekbar, vibrate for the new value so the user can tell what the new value is
                vibrator = (Vibrator) getContext().getSystemService(getContext().VIBRATOR_SERVICE);
                vibrator.vibrate((int)newValue);
                return true;
            }
        });
    }

    // Method to delete all colors from SQLite database
    private void deleteAll() {

        SQLiteHelper sqLiteHelper = new SQLiteHelper(getActivity());
        SQLiteDatabase colorsDB = sqLiteHelper.getWritableDatabase();

        // Simple DELETE FROM + table name to delete all rows
        colorsDB.execSQL("DELETE FROM " + sqLiteHelper.getTableName());

        // Also clear the colors ArrayList in the MainActivity
        MainFragment.colors.clear();

        // Notify the user that all colors have been deleted
        Toast.makeText(getContext(), R.string.all_colors_deleted, Toast.LENGTH_SHORT).show();
    }
}