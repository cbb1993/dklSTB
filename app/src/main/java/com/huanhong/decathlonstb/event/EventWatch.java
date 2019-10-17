package com.huanhong.decathlonstb.event;

import android.util.Log;

import com.huanhong.decathlonstb.db.DBManager;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by done on 2018/3/11.
 * 数据库里手表的数据只保留最新的20条。
 * 有待发送数据的情况下，每分钟发送一次。
 */

public class EventWatch implements IEvent, DBManager.OnDoneListener {

    private String TAG = getClass().getSimpleName();
    private Config config;
    private boolean isSending;
    private boolean isEmpty;
    private String watchUrl;

    private static EventWatch watchEvent;

    private EventWatch(Config config) {
        this.config = config;
        watchUrl = "http://" + config.db.getIp() + ":5665/?";
    }

    public static EventWatch getWatchEvent(Config config) {
        if (watchEvent == null) {
            watchEvent = new EventWatch(config);
        }
        return watchEvent;
    }

    @Override
    public boolean isOk() {
        return !isEmpty;
    }

    @Override
    public void ok() {
        if (isSending)
            return;
        isSending = true;
        config.db.queryWatchs(new DBManager.OnDoneListener<JSONArray>() {
            @Override
            public void done(JSONArray array) {
                final int size = array.length();
                isEmpty = size == 0;
                Log.d(TAG, "send: " + size);
                if (isEmpty) {
                    isSending = false;
                    return;
                }
                JSONObject js = new JSONObject();
                try {
                    js.put("data", array);
                    js.put("store", config.store);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                OkGo.<String>get(watchUrl + js.toString()).execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if (response != null && "1".equals(response.body())) {
                            Log.d(TAG, "send: success");
                            config.db.clearWatchs(size);
                            ok();
                        } else {
                            Log.e(TAG, "send: 'store' does not match");
                        }
                    }

                    @Override
                    public void onFinish() {
                        isSending = false;
                    }

                    @Override
                    public void onError(Response<String> response) {
                        Log.e(TAG, "send: " + response.getException().getMessage() + " :" + watchUrl);
                    }
                });
            }
        });
    }

    @Override
    public void end() {
        watchEvent = null;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public void done(Object o) {
        ok();
    }
}