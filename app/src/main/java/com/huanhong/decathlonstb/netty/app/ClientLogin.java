package com.huanhong.decathlonstb.netty.app;

import android.util.Log;

import com.google.gson.JsonObject;
import com.huanhong.decathlonstb.util.ThreadUtils;

public class ClientLogin implements ApiClient.ApiClientListener, Runnable {

    private String shop_no, pad_no;

    private String TAG = "ClientLogin";

    public ClientLogin(String shop_no, String pad_no) {
        this.shop_no = shop_no;
        this.pad_no = pad_no;
    }

    @Override
    public void onInited() {

    }

    @Override
    public void onConnected() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", "login");
        jsonObject.addProperty("shop_no", shop_no);
        jsonObject.addProperty("pad_no", pad_no);
        ApiClient.getInstance().send(jsonObject.toString());
    }

    @Override
    public void onConnectFailed(Exception e) {

    }

    @Override
    public void onSent(String message) {
        ApiProtocol apiProtocol = ApiProtocol.parseJson(message);
        if (apiProtocol == null) {
            return;
        }
//        if (apiProtocol.getAction().equals(ApiProtocol.TYPE_LOGIN)) {
//            waitInit(20);
//        }
    }

    @Override
    public void onSendFailed(String message, Exception e) {

    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onDisconnected() {
        initEnd();
    }

    public void waitInit(int timeS) {
//        Log.d(TAG, "initWait: ");
//        ThreadUtils.removeWorkCallbacks(this);
//        ThreadUtils.runOnWorkThread(this, timeS * 1000);
    }

    public void initEnd() {
//        Log.d(TAG, "initEnd: ");
//        ThreadUtils.removeWorkCallbacks(this);
    }

    @Override
    public void run() {
//        Log.d(TAG, "initFaile: retry");
//        ApiClient.getInstance().disConnectRetry();
    }
}