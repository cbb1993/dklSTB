package com.huanhong.decathlonstb.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.text.TextUtils;

import com.huanhong.decathlonstb.db.DBManager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ChannelMannager {

    private static String localChannel;
    private static String channel;
    private static String url;
    private static boolean isCompatible = false;

    public static String getChannel() {
        if (!TextUtils.isEmpty(channel)) {
            return channel;
        }
        channel = getMeta("VERSION_CHANNEL");
        return channel;
    }

    public static String getLocalChannel() {
        return localChannel;
    }

    public static boolean isCompatible() {
        return isCompatible;
    }

    public static String getUrl() {
        if (!TextUtils.isEmpty(url)) {
            return url;
        }
        url = getMeta("URL");
        return url;
    }

    public static boolean isScroll() {
        return "yes".equals(getMeta("SCROLL"));
    }

    private static String getMeta(String key) {
        Context context = AppUtils.getBaseApplication();
        if (context == null || TextUtils.isEmpty(key)) {
            return null;
        }
        String value = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager != null) {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        value = applicationInfo.metaData.getString(key);
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void init() {
        setLocale();
        channel = getChannel();
        SharedPreferences sp = AppUtils.getBaseApplication().getSharedPreferences("app_channel", Context.MODE_PRIVATE);
        localChannel = sp.getString("channel", null);
        if (TextUtils.isEmpty(localChannel)) {
            sp.edit().putString("channel", channel).commit();
            localChannel = channel;
        }
        List<String> compatibleChannels = new ArrayList<>();
        String meta = getMeta("COMPATIBLE");
        if (!TextUtils.isEmpty(meta)) {
            String[] metaChannels = meta.split("&");
            if (metaChannels != null && metaChannels.length > 0) {
                for (String metaChannel : metaChannels) {
                    compatibleChannels.add(metaChannel);
                }
            }
        }
        compatibleChannels.add(channel);
        isCompatible = compatibleChannels.contains(localChannel);
        //更新下本地兼容的channel
        if (isCompatible && !channel.equals(localChannel)) {
            sp.edit().putString("channel", channel).commit();
            localChannel = channel;
        }
    }

    /**
     * 根据channel设置语言
     */
    public static void setLocale() {
        if ("normal_hk".equals(getChannel())) {
            Context context = AppUtils.getBaseApplication();
            Configuration config = context.getResources().getConfiguration();
            if (config.locale != Locale.TRADITIONAL_CHINESE) {
                config.locale = Locale.TRADITIONAL_CHINESE;
                context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
            }
        }
    }
}