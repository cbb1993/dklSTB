package com.huanhong.decathlonstb.util;

import android.app.Application;

public class AppUtils {
    private static Application mApplication;

    public static void init(Application application) {
        mApplication = application;
    }

    public static Application getBaseApplication() {
        return mApplication;
    }
}