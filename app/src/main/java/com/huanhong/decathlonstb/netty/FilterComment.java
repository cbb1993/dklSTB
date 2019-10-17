package com.huanhong.decathlonstb.netty;

import android.text.TextUtils;
import android.util.Log;

public class FilterComment {

    private static String TAG = "FilterComment";

    private static String mId;
    private static int mIdCount;

    public static boolean pass(String memberId) {
        if (TextUtils.isEmpty(memberId)) {
            mIdCount = 1;
            return true;
        }
        if (memberId.equals(mId)) {
            mIdCount++;
        } else {
            mIdCount = 1;
        }
        mId = memberId;
        if (mIdCount == 3) {
            Log.e(TAG, "Error: memberId continuous more than 3 times");
            return false;
        }
        return true;
    }
}
