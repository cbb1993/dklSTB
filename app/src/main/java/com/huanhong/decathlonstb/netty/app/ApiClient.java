package com.huanhong.decathlonstb.netty.app;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.google.gson.Gson;
import com.huanhong.decathlonstb.netty.NettyClient;
import com.huanhong.decathlonstb.util.AppUtils;
import com.huanhong.decathlonstb.util.SharedPreferencesUtil;
import com.huanhong.decathlonstb.util.ThreadUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ApiClient implements NettyClient.NettyClientListener {
    private static ApiClient mInstance;
    private static NettyClient mNettyClient;
    private final String TAG = getClass().getSimpleName();
    private final int DELAY_CONNECT = ApiConstans.DELAY_CONNECT_TIME;
    private Map<String, List<OnResponseListener>> mOnResponseListenerMap = new HashMap<>();
    private Map<String, List<OnRequestListener>> mOnRequestListenerMap = new HashMap<>();
    private List<ApiClientListener> mApiClientListenerList = new ArrayList<>();
    private Gson mGson;
    private boolean close = false;

    private int errorCout = 0;

    private ApiClient() {
        mNettyClient = NettyClient.getInstance();
        mNettyClient.setNettyClientListener(this);
        mNettyClient.setHeartBeat(ApiProtocol.TYPE_HEARTBEAT);
    }

    public synchronized static ApiClient getInstance() {
        if (mInstance == null) {
            mInstance = new ApiClient();
        }
        return mInstance;
    }

    public void connect() {
        close = false;
        mNettyClient.connect("140.207.233.97", 35536);
    }

    public void disconnect() {
        close = true;
        mNettyClient.disconnect();
    }

    public boolean isConnected() {
        return mNettyClient.isConnect();
    }

//    public void send(ApiProtocol apiProtocol) {
//        mNettyClient.sendMessage(mGson.toJson(apiProtocol), 0);
//    }

    public void send(String message) {
        mNettyClient.sendMessage(message, 0);
    }

    public void addOnResponseListener(String type, OnResponseListener listener) {
        List<OnResponseListener> listenerList = mOnResponseListenerMap.get(type);
        if (listenerList == null) {
            listenerList = new ArrayList<>();
        }
        listenerList.add(listener);
        mOnResponseListenerMap.put(type, listenerList);
    }

    public void removeOnResponseListener(String type, OnResponseListener listener) {
        List<OnResponseListener> listenerList = mOnResponseListenerMap.get(type);
        if (listenerList != null) {
            listenerList.remove(listener);
        }
    }

    public void addOnRequestListener(String type, OnRequestListener listener) {
        List<OnRequestListener> listenerList = mOnRequestListenerMap.get(type);
        if (listenerList == null) {
            listenerList = new ArrayList<>();
        }
        listenerList.add(listener);
        mOnRequestListenerMap.put(type, listenerList);
    }

    public void removeOnRequestListener(String type, OnRequestListener listener) {
        List<OnRequestListener> listenerList = mOnRequestListenerMap.get(type);
        if (listenerList != null) {
            listenerList.remove(listener);
        }
    }

    public void addApiClientListener(ApiClientListener listener) {
        mApiClientListenerList.add(listener);
    }

    public void removeApiClientListener(ApiClientListener listener) {
        mApiClientListenerList.remove(listener);
    }

    @Override
    public void onInited() {
        Log.e(TAG, "初始化成功");
        for (ApiClientListener listener : mApiClientListenerList) {
            if (listener != null) {
                listener.onInited();
            }
        }
    }

    @Override
    public void onConnected() {
        errorCout = 0;
        Log.e(TAG, "连接成功");
        for (ApiClientListener listener : mApiClientListenerList) {
            if (listener != null) {
                listener.onConnected();
            }
        }
    }

    @Override
    public void onConnectFailed(final Exception e) {
        errorCout++;
        if (errorCout > 10) {
            errorCout = 0;
            final WifiManager wifiManager = (WifiManager) AppUtils
                    .getBaseApplication()
                    .getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            Log.d(TAG, "wifi: off");
            wifiManager.setWifiEnabled(false);
            ThreadUtils.runOnWorkThread(new Runnable() {
                @Override
                public void run() {
                    wifiManager.setWifiEnabled(true);
                    Log.d(TAG, "wifi: on");
                }
            }, 5000);
        }
        Log.e(TAG, errorCout + "连接失败: " + e.getMessage());
        for (ApiClientListener listener : mApiClientListenerList) {
            if (listener != null) {
                listener.onConnectFailed(e);
            }
        }
        reConnect();
    }

    private void reConnect() {
        Log.e(TAG, "尝试重连: " + DELAY_CONNECT + "ms后");
        mNettyClient.reInit(0);
        mNettyClient.reConnect(DELAY_CONNECT);
    }

    public void disConnectRetry() {
        if (isConnected()) {
            mNettyClient.disconnect();
        }
    }

    @Override
    public void onSent(final String message) {
        Log.e(TAG, "发送成功: " + message);
        for (ApiClientListener listener : mApiClientListenerList) {
            if (listener != null) {
                listener.onSent(message);
            }
        }
    }

    @Override
    public void onSendFailed(final String message, final Exception e) {
        Log.e(TAG, "发送失败: " + message + "," + e.getMessage());
        for (ApiClientListener listener : mApiClientListenerList) {
            if (listener != null) {
                listener.onSendFailed(message, e);
            }
        }
        reConnect();
    }


    @Override
    public void onDataReceive(String data) {
        Log.e(TAG, "接收成功: " + data);
//        if (data.equals(ApiProtocol.TYPE_HEARTBEAT)) {
//            Log.e(TAG, "接收: 心跳");
//            return;
//        }
        final ApiProtocol apiProtocol = ApiProtocol.parseJson(data);
        if (apiProtocol == null) {
            return;
        }
        if( "sales".equals(apiProtocol.getAction())){
            send(data);
            if(severPushListene!=null){
                severPushListene.push(data);
            }
        }
        final List<OnResponseListener> listenerList = mOnResponseListenerMap.get(apiProtocol.getAction());
        if (listenerList != null) {
            for (OnResponseListener listener : listenerList) {
                if (listener != null) {
                    listener.onResponse(apiProtocol);
                }
            }
        }
    }

    @Override
    public void onError(final Exception e) {
        Log.e(TAG, "异常: " + e.getMessage());
        for (ApiClientListener listener : mApiClientListenerList) {
            if (listener != null) {
                listener.onError(e);
            }
        }
        reConnect();
    }

    @Override
    public void onDisconnected() {
        Log.e(TAG, "连接关闭");
        for (ApiClientListener listener : mApiClientListenerList) {
            if (listener != null) {
                listener.onDisconnected();
            }
        }
        if (!close)
            reConnect();
    }


    public interface ApiClientListener {
        void onInited();

        void onConnected();

        void onConnectFailed(Exception e);

        void onSent(String message);

        void onSendFailed(String message, Exception e);

        void onError(Exception e);

        void onDisconnected();
    }

    public interface OnResponseListener {
        void onResponse(ApiProtocol apiProtocol);
    }

    public interface OnRequestListener {
        void onRequest(ApiProtocol apiProtocol);
    }

    private SeverPushListener severPushListene;
    public void setSeverPushListener(SeverPushListener severPushListener) {
        severPushListene = severPushListener;
    }
    public interface SeverPushListener{
        void push(String data);
    }
}