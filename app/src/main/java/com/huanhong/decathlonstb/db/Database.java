package com.huanhong.decathlonstb.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.huanhong.decathlonstb.util.ChannelMannager;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Database extends SQLiteOpenHelper {
    private final String TABLE_SETTING = "decathlongs_set";
    private final String TABLE_DATA = "decathlongs_data";
    private final String TABLE_CACHES = "decathlongs_caches";
    private final String TABLE_SEND = "decathlongs_watchs";

    private static String DB_NAME = "decathlongs_data";
    private static int version = 3;

    public Database(Context context) {
        super(context, DB_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "
                + TABLE_SETTING
                + " (id INTEGER,year VARCHAR,date VARCHAR,store VARCHAR,http VARCHAR,ip VARCHAR)");
        db.execSQL("create table "
                + TABLE_DATA
                + " (id INTEGER,lastid INTEGER,ygc INTEGER,dgc INTEGER,yc INTEGER,dc INTEGER)");
        db.execSQL("create table "
                + TABLE_CACHES
                + " (id INTEGER PRIMARY KEY AUTOINCREMENT,number VARCHAR,time VARCHAR,mac VARCHAR,type INTEGER,tid " +
                "VARCHAR,member_id VARCHAR,terminal_type VARCHAR)");
        db.execSQL("create table "
                + TABLE_SEND
                + " (id INTEGER PRIMARY KEY AUTOINCREMENT,number VARCHAR,time VARCHAR,type INTEGER)");

        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        String date = new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
        ContentValues cv = new ContentValues();
        cv.put("id", "1");
        cv.put("year", year);
        cv.put("date", date);
        cv.put("http", ChannelMannager.getUrl());
        db.insert(TABLE_SETTING, null, cv);

        ContentValues cv2 = new ContentValues();
        cv2.put("id", "1");
        cv2.put("lastid", 0);
        cv2.put("ygc", 0);
        cv2.put("dgc", 0);
        cv2.put("yc", 0);
        cv2.put("dc", 0);
        db.insert(TABLE_DATA, null, cv2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            db.execSQL("ALTER TABLE " + TABLE_CACHES
                    + " ADD COLUMN tid VARCHAR");
            db.execSQL("ALTER TABLE " + TABLE_CACHES
                    + " ADD COLUMN member_id VARCHAR");
        }else  if (oldVersion == 2) {
            db.execSQL("ALTER TABLE " + TABLE_CACHES
                    + " ADD COLUMN terminal_type VARCHAR");
        }
    }
}