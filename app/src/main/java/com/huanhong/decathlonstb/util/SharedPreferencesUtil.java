package com.huanhong.decathlonstb.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 坎坎 on 2017/2/28.
 */

public class SharedPreferencesUtil {
    private static SharedPreferences preferences;

    private static SharedPreferences.Editor editor;

    static {

    }

    public static void init(Context context) {
        if (preferences == null) {
            preferences = context.getApplicationContext().getSharedPreferences("user", context.MODE_PRIVATE);
        }
    }

    public static void setStringData(String key, String s) {
        editor = preferences.edit();
        editor.putString(key, s);
        editor.commit();
    }

    public static String getStringData(String key) {
        return preferences.getString(key, "");
    }

    public static void setBooleanData(String key, boolean s) {
        editor = preferences.edit();
        editor.putBoolean(key, s);
        editor.commit();
    }

    public static boolean getBooleanData(String key, boolean df) {
        return preferences.getBoolean(key, df);
    }

    public static void saveObject(String key, Object obj) {
        if (obj == null) {
            editor.remove(key);
        } else {
            editor = preferences.edit();
            String str = new Gson().toJson(obj, obj.getClass());
            editor.putString(key, str);
        }
        editor.commit();
    }

    public static <T> T getObject(String key) {
        try {
            String str = preferences.getString(key, null);
            if (str != null) {
                Type type = new TypeToken<T>() {
                }.getType();
                return (T) new Gson().fromJson(str, type);
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static <T> List<T> getListObject(String key, Class<T> clazz) {
        try {
            String str = preferences.getString(key, null);
            if (str != null) {
                return fromJsonArray(str, clazz);
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static <T> List<T> fromJsonArray(String json, Class<T> clazz) throws Exception {
        List<T> lst = new ArrayList<T>();
        JsonArray array = new JsonParser().parse(json).getAsJsonArray();
        for (final JsonElement elem : array) {
            lst.add(new Gson().fromJson(elem, clazz));
        }
        return lst;
    }

    public static void setStringDataList(String key, List<String> datalist) {
        if (null == datalist || datalist.size() <= 0)
            return;
        Gson gson = new Gson();
        //转换成json数据，再保存
        String strJson = gson.toJson(datalist);
        editor = preferences.edit();
        editor.putString(key, strJson);
        editor.commit();

    }
}
