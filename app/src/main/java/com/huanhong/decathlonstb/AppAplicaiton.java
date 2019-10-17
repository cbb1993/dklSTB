package com.huanhong.decathlonstb;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;

import com.huanhong.decathlonstb.util.AppUtils;
import com.huanhong.decathlonstb.util.ChannelMannager;
import com.huanhong.decathlonstb.util.SharedPreferencesUtil;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.HttpHeaders;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class AppAplicaiton extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppUtils.init(this);
        ChannelMannager.init();
        SharedPreferencesUtil.init(this);
        initOkGo();

        if (!BuildConfig.DEBUG)
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, final Throwable ex) {
                    AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(AppAplicaiton.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("crash", true);
                    PendingIntent restartIntent = PendingIntent.getActivity(
                            AppAplicaiton.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                    System.gc();
                }
            });
    }

    private void initOkGo() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //全局的读取超时时间
        builder.readTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        //全局的写入超时时间
        builder.writeTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        //全局的连接超时时间
        builder.connectTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        OkGo.getInstance().init(this)                           //必须调用初始化
                .setOkHttpClient(builder.build())               //建议设置OkHttpClient，不设置将使用默认的
                .setCacheMode(CacheMode.NO_CACHE)               //全局统一缓存模式，默认不使用缓存，可以不传
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)   //全局统一缓存时间，默认永不过期，可以不传
                .setRetryCount(0)                             //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0
                .addCommonHeaders(new HttpHeaders("Connection", "close"));
    }
}