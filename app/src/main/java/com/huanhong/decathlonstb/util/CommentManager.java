package com.huanhong.decathlonstb.util;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.huanhong.decathlonstb.model.Comment;

import java.util.ArrayList;
import java.util.List;

public class CommentManager {

    private static String TAG = "CommentManager";
    private static final String UP_TO_DOWN_POSITION = "UP_TO_DOWN_POSITION";
    private static final String DOWN_TO_UP_POSITION = "DOWN_TO_UP_POSITION";

    public interface NotifyChange {
        void change();
    }

    private static NotifyChange notifyChange = null;

    public static void setNotifyChange(NotifyChange notifyChange) {
        CommentManager.notifyChange = notifyChange;
    }

    public static boolean shouldUpdate() {
        String date = SharedPreferencesUtil.getStringData("CommentsDate");
        if (TextUtils.isEmpty(date))
            return true;
        else {
            String nowDate = DateFormatUitl.yyyyMMdd(System.currentTimeMillis());
            if (!nowDate.equals(date))
                return true;
        }
        return false;
    }

    public static void reset() {
        SharedPreferencesUtil.setStringData("CommentsDate", "");
        SharedPreferencesUtil.saveObject("CommentsData", null);
        SharedPreferencesUtil.setStringData(DOWN_TO_UP_POSITION, "0");
    }

    public static void saveData(List<Comment> comments) {
        if (comments == null)
            comments = new ArrayList<>();
        String dataNew = new Gson().toJson(comments);
        String dataOld = SharedPreferencesUtil.getStringData("CommentsData");
        SharedPreferencesUtil.setStringData("CommentsDate", DateFormatUitl.yyyyMMdd(System.currentTimeMillis()));
        if (!dataNew.equals(dataOld)) {
            Log.d(TAG, "saveData: " + dataNew);
            //更新后将LastScrollPosition归零
            savePosition(0);
            SharedPreferencesUtil.saveObject("CommentsData", comments);
            if (notifyChange != null) {
                notifyChange.change();
            }
        } else {
            Log.d(TAG, "saveData: old == new, no change");
        }
    }

    public static List<Comment> getList() {
        return SharedPreferencesUtil.getListObject("CommentsData", Comment.class);
    }

    public static int getPosition() {
        String s = SharedPreferencesUtil.getStringData(DOWN_TO_UP_POSITION);
        int p = 0;
        if (!TextUtils.isEmpty(s)) {
            p = Integer.valueOf(s);
        }
        if (p < 0)
            p = 0;
        return p;
    }

    public static void savePosition(int p) {
        SharedPreferencesUtil.setStringData(DOWN_TO_UP_POSITION, p + "");
    }
}