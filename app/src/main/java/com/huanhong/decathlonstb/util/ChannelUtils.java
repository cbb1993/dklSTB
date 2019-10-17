package com.huanhong.decathlonstb.util;

import android.content.Context;
import android.content.SharedPreferences;

public class ChannelUtils {

    private static SharedPreferences mSharedPreferences;

    public static void addData(String key, String value) {
        init();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void addData(String key, boolean value) {
        init();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    private static void init() {
        if (mSharedPreferences == null) {
            mSharedPreferences = AppUtils.getBaseApplication().getSharedPreferences("app_version", Context.MODE_PRIVATE);
        }
    }

    public static String getString(String key) {
        return getString(key, "");
    }

    public static boolean readBooleanData(String key, Boolean defaultStr) {
        init();
        return mSharedPreferences.getBoolean(key, defaultStr);
    }

    public static String getString(String key, String defaultStr) {
        init();
        return mSharedPreferences.getString(key, defaultStr);
    }

    public static SharedPreferences getInstance() {
        init();
        return mSharedPreferences;
    }
}