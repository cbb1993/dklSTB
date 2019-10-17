package com.huanhong.decathlonstb.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.huanhong.decathlonstb.db.DBManager;
import com.huanhong.decathlonstb.model.Comment;
import com.huanhong.decathlonstb.netty.app.ApiClient;
import com.huanhong.decathlonstb.netty.app.ApiProtocol;
import com.huanhong.decathlonstb.netty.app.ClientLogin;
import com.lzy.okgo.db.DBUtils;

public class ClientService extends Service implements ApiClient.OnResponseListener {

    private final String TAG = "ClientService";
    private ClientLogin clientLogin;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        clientLogin = new ClientLogin(SharedPreferencesUtil.getStringData("shopid"), "svr");
        DBManager db  = new DBManager(this);
        Log.e("---","---"+db.getStoreNo());
        clientLogin = new ClientLogin(db.getStoreNo(), "svr");
        ApiClient.getInstance().addApiClientListener(clientLogin);
        ApiClient.getInstance().addOnResponseListener(ApiProtocol.TYPE_INIT, this);
        ApiClient.getInstance().addOnResponseListener(ApiProtocol.TYPE_LOGOUT, this);
        ApiClient.getInstance().connect();
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public void onDestroy() {
        ApiClient.getInstance().removeApiClientListener(clientLogin);
        ApiClient.getInstance().removeOnResponseListener(ApiProtocol.TYPE_INIT, this);
        ApiClient.getInstance().removeOnResponseListener(ApiProtocol.TYPE_LOGOUT, this);
        ApiClient.getInstance().disconnect();
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @Override
    public void onResponse(ApiProtocol apiProtocol) {

    }

}