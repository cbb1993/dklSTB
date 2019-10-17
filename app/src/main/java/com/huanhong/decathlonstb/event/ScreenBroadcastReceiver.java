package com.huanhong.decathlonstb.event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.huanhong.decathlonstb.util.ScreenLockUtil;

public class ScreenBroadcastReceiver extends BroadcastReceiver {

    private Context context;

    private static ScreenBroadcastReceiver screenBroadcastReceiver;

    private ScreenBroadcastReceiver(Context context) {
        this.context = context;
    }

    public static void register(Context context) {
        if (screenBroadcastReceiver == null)
            screenBroadcastReceiver = new ScreenBroadcastReceiver(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        context.registerReceiver(screenBroadcastReceiver, intentFilter);
        ScreenLockUtil.wakeUpAndUnlock(context);
    }

    public static void unregister() {
        if (screenBroadcastReceiver != null) {
            try {
                screenBroadcastReceiver.context.unregisterReceiver(screenBroadcastReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            screenBroadcastReceiver = null;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_SCREEN_ON.equals(action)) { // 开屏
        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) { // 锁屏
            ScreenLockUtil.wakeUpAndUnlock(context);
        } else if (Intent.ACTION_USER_PRESENT.equals(action)) { // 解锁
        }
    }
}