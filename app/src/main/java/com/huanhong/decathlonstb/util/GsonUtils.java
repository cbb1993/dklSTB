package com.huanhong.decathlonstb.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class GsonUtils {
    public static Gson mGson;

    public static <T> List<T> fromJsonArray(String json, Class<T> clazz) {
        Gson gson = getGson();
        List<T> lst = new ArrayList<T>();
        try {
            JsonArray array = new JsonParser()
                    .parse(json)
                    .getAsJsonArray();
            for (final JsonElement elem : array) {
                lst.add(gson.fromJson(elem, clazz));
            }
        } catch (Exception e) {

        }
        return lst;
    }

    public static Gson getGson() {
        if (mGson == null) {
            mGson = new Gson();
        }
        return mGson;
    }
}
