package com.example.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "audit_log.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE AuditPrompt (SequenceNumber INTEGER PRIMARY KEY AUTOINCREMENT, DateTime TEXT, Prompt TEXT)");
        db.execSQL("CREATE TABLE Responses (SequenceNumber INTEGER PRIMARY KEY AUTOINCREMENT, DateTime TEXT, Response TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS AuditPrompt");
        db.execSQL("DROP TABLE IF EXISTS Responses");
        onCreate(db);
    }
}
