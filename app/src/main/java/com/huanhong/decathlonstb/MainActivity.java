package com.huanhong.decathlonstb;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import com.huanhong.decathlonstb.custom.LockerView;
import com.huanhong.decathlonstb.custom.NormalLockLayer;
import com.huanhong.decathlonstb.custom.ScrollLockLayer;
import com.huanhong.decathlonstb.custom.TestLockView;
import com.huanhong.decathlonstb.db.DBManager;
import com.huanhong.decathlonstb.http.MyNanoHTTPD;
import com.huanhong.decathlonstb.event.EventScroll;
import com.huanhong.decathlonstb.event.EventNewDay;
import com.huanhong.decathlonstb.event.EventNewYear;
import com.huanhong.decathlonstb.event.EventRegister;
import com.huanhong.decathlonstb.event.EventComments;
import com.huanhong.decathlonstb.event.IEvent;
import com.huanhong.decathlonstb.event.ScreenBroadcastReceiver;
import com.huanhong.decathlonstb.event.EventWatch;
import com.huanhong.decathlonstb.service.ClientService;
import com.huanhong.decathlonstb.util.ChannelMannager;

import java.io.IOException;

public class MainActivity extends Activity {

    private DBManager db;
    private LockerView lock;
    private MyNanoHTTPD httpd;
    private boolean isOk = false;

    public static void start(Context context) {
        Intent starter = new Intent(context, MainActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        db = new DBManager(this);
        if (!check())
            return;
        isOk = true;
        lock = ChannelMannager.isScroll() ? new ScrollLockLayer(this) : new NormalLockLayer(this);
        db.checkDate();
        lock.lock();
        lock.notifyUpdate(db);
        register();
        connectService();
    }

    private boolean check() {
        if (!ChannelMannager.isCompatible()) {
            ErrorActivity.start(this);
            finish();
            return false;
        }
        String cacheStore = db.getStore();
        if (TextUtils.isEmpty(cacheStore)) {
            startActivity(new Intent(this, SetUpActivity.class));
            finish();
            return false;
        }
        return true;
    }

    private void register() {
        ScreenBroadcastReceiver.register(this);
        String store = db.getStore().split("##")[2].split("-")[0];
        IEvent.Config config = new IEvent.Config(db.getHttpUrl(), store, db);
        EventRegister.register(this);
        if (ChannelMannager.isScroll())
            EventRegister.addEvent(new EventScroll(config));
        EventRegister.addEvent(new EventNewYear(db));
        EventRegister.addEvent(new EventNewDay(db));
        EventRegister.addEvent(new EventComments(config) {
            @Override
            public void httpState(boolean b) {
                lock.httpState(b);
            }
        });
        EventRegister.addEvent(EventWatch.getWatchEvent(config));
        initHttpD(config);
        db.addListener(lock);
    }

    private void initHttpD(IEvent.Config config) {
        httpd = new MyNanoHTTPD(config);
        try {
            httpd.start();
        } catch (IOException e) {
            e.printStackTrace();
            if (TestLockView.getOnLog() != null)
                TestLockView.getOnLog().throwError("initHttpD: ", e);
        }
    }

    public boolean isSeverAlive() {
        return httpd != null && httpd.isAlive();
    }

    public void getCacheAmount(DBManager.OnDoneListener<Integer> listener) throws Throwable {
        db.queryCacheAmount(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(serviceConnection!=null){
            try {
                unbindService(serviceConnection);
            }catch (Exception e){
            }
        }
        if (isOk) {
            httpd.stop();
            EventRegister.unregister();
            lock.onDestroy();
            ScreenBroadcastReceiver.unregister();
        }
        db.closeDb();
    }

    private ServiceConnection serviceConnection;
    private void connectService() {
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            }
            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        bindService(new Intent(this, ClientService.class), serviceConnection, BIND_AUTO_CREATE);
    }
}