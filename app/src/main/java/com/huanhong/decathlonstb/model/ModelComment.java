package com.huanhong.decathlonstb.model;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class ModelComment {
    public long time, id;
    public int type;
    public String mac, store, number;
    public String tid;
    public String vipid;
    public int terminalType;
    /*
    {"goodComment":true,"mac":"90:63:3b:6f:db:2c","number":"101","retry":3,"store":"9999","tid":"1571215627922","type":1}
        terminalType
    * */

    public boolean verifyError(String localStore) {
        //TextUtils.isEmpty(tid) ||
        return (type < 1 || type > 5)
                || TextUtils.isEmpty(store)
                || !store.equals(localStore)
                || TextUtils.isEmpty(number);
    }

    public JSONObject toJson(int upload_no) {
        JSONObject jsonObject = new JSONObject();
        if(terminalType == 0){
            terminalType = 1;
        }
        try {
            jsonObject.put("shop_no", store);
            jsonObject.put("pad_no", number);
            jsonObject.put("time", time / 1000 + "");
            jsonObject.put("score", type);
            jsonObject.put("tid", tid);
            jsonObject.put("vipid", vipid);
            jsonObject.put("upload_no", upload_no);
            jsonObject.put("terminalType", terminalType);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public boolean badComment() {
        return type > 2;
    }
}
