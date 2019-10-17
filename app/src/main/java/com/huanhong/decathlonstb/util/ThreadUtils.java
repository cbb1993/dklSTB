package com.huanhong.decathlonstb.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public class ThreadUtils {
    private static HandlerThread mWorkThread;
    private static Handler mWorkHandler;
    private static Handler mMainHandler;

    private static void init() {
        if (mWorkThread == null) {
            mWorkThread = new HandlerThread(ThreadUtils.class.getSimpleName());
            mWorkThread.start();
        }
        if (mWorkHandler == null) {
            mWorkHandler = new Handler(mWorkThread.getLooper());
        }
        if (mMainHandler == null) {
            mMainHandler = new Handler(Looper.getMainLooper());
        }
    }

    public static void runOnMainThread(Runnable runnable, long delayedMs) {
        init();
        mMainHandler.postDelayed(runnable, delayedMs);
    }

    public static void runOnWorkThread(Runnable runnable, long delayedMs) {
        init();
        mWorkHandler.postDelayed(runnable, delayedMs);
    }

    public static void removeWorkCallbacks(Runnable runnable) {
        init();
        mWorkHandler.removeCallbacks(runnable);
    }

    public static void removeWorkCallbacks() {
        init();
        mWorkHandler.removeCallbacksAndMessages(null);
    }
}