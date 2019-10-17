package com.huanhong.decathlonstb.netty.app;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;

public class ApiProtocol {

    private static String TAG = "ApiProtocol";

    //    public static final String TYPE_UPDATE = "completed";
    public static final String TYPE_LOGIN = "login";
    public static final String TYPE_INIT = "init";
    public static final String TYPE_SALES = "sales";
    public static final String TYPE_LOGOUT = "logout";
    public static final String TYPE_HEARTBEAT = "hb";

    private String type;
    private String action;
    public String tid;
    public String vipid;
    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public static ApiProtocol parseJson(String s) {
        ApiProtocol apiProtocol = null;
        try {
            apiProtocol = JSON.parseObject(s, ApiProtocol.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (apiProtocol == null) {
            Log.e(TAG, "protocol转换失败");
            return null;
        }

        String action = apiProtocol.getAction();
        if (TextUtils.isEmpty(action)) {
            Log.e(TAG, "action不可为空");
            return null;
        }
        return apiProtocol;
    }
}
