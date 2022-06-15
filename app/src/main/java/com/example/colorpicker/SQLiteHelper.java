package com.example.colorpicker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Simple helper class to create and manage the SQLite database used by the main fragment.

public class SQLiteHelper extends SQLiteOpenHelper {

    // Create database constants
    private static final String DATABASE_NAME = "colorpicker.db";
    private static final String TABLE_NAME = "colors";
    private static final String TABLE_CREATION_QUERY = "CREATE TABLE " + TABLE_NAME + " (id INTEGER PRIMARY KEY AUTOINCREMENT, hexcode TEXT)";
    private static final int DATABASE_VERSION = 1;

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the table when the database is created
        db.execSQL(TABLE_CREATION_QUERY);
    }

    // This method is called when the database is upgraded, so that the table can be updated to the latest version.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public String getTableName() {
        return TABLE_NAME;
    }
}
