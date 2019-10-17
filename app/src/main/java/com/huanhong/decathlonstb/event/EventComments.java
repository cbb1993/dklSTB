package com.huanhong.decathlonstb.event;

import android.util.Log;

import com.huanhong.decathlonstb.db.DBManager;
import com.huanhong.decathlonstb.http.callback.HttpCallback;
import com.huanhong.decathlonstb.model.BaseData;
import com.huanhong.decathlonstb.model.ModelComment;
import com.huanhong.decathlonstb.util.MyCountDownTimer;
import com.huanhong.decathlonstb.util.StoreAmout;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;

import org.json.JSONArray;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by done on 2018/3/9.
 */

public class EventComments extends TenMinutesEvent {

    private String TAG = "EventComments";
    private MyCountDownTimer countDownTimer;
    private DBManager db;
    private Config config;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private String httpTagSync = "httpTagSync";
    private String httpTagUploade = "httpTagUploade";

    public EventComments(Config config) {
        super(config);
        db = config.db;
        this.config = config;
        syncData();
    }

    @Override
    public boolean isOk() {
        //每十分钟延时上传一次，除开零点
        return !"00:00".equals(hhmm.format(System.currentTimeMillis())) && super.isOk();
    }

    @Override
    public void ok() {
        startCountdown();
    }

    @Override
    public void end() {
        if (countDownTimer != null) {
            countDownTimer.stop();
        }
        OkGo.getInstance().cancelTag(httpTagSync);
        OkGo.getInstance().cancelTag(httpTagUploade);
    }

    /**
     * 启动倒计时，延迟上传数据
     *
     * @param
     */
    private void startCountdown() {
        String num = getConfig().store.substring(0, 2);
        int delay = Integer.parseInt(num) * 5 * 1000;
        if (countDownTimer == null) {
            countDownTimer = new MyCountDownTimer() {
                @Override
                public void onFinish() {
                    //查询最多200条
                    db.queryCaches(200, new DBManager.OnDoneListener<List<ModelComment>>() {
                        @Override
                        public void done(List<ModelComment> loadData) {
                            int deleteSize = loadData.size();
                            Log.d(TAG, "uploadData: size: " + deleteSize);
                            if (deleteSize > 0) {
                                JSONArray jsonArray = new JSONArray();
                                int lastId = db.getLastId();
                                int i = 1;
                                for (ModelComment modelComment : loadData) {
                                    jsonArray.put(modelComment.toJson(i + lastId));
                                    i++;
                                }
                                uploadData(deleteSize, jsonArray.toString(), lastId);
                            }
                        }
                    });
                }
            };
        }
        Log.d(TAG, "startCountdown: " + dateFormat.format(System.currentTimeMillis() + delay) + "开始上传");
        countDownTimer.reStart(delay, 1000);
    }

    /**
     * 同步数据
     */
    public void syncData() {
        Log.d(TAG, "syncData: start");
        OkGo.<BaseData<StoreAmout>>post(db.getHttpUrl() + "/decathlon-store/updateData.")
                .params("shop_no", config.store)
                .params("date", System.currentTimeMillis() / 1000)
                .tag(httpTagSync)
                .execute(new Callback<BaseData<StoreAmout>>() {
                    @Override
                    public void success(BaseData<StoreAmout> storeAmoutDataResponse) {
                        super.success(storeAmoutDataResponse);
                        StoreAmout storeAmout = storeAmoutDataResponse.data;
                        if (storeAmout != null) {
                            Log.d(TAG, "syncData: success");
                            db.sync(storeAmout);
                        }
                    }
                });
    }

    /**
     * 上传数据
     *
     * @param data
     * @param lastId
     */
    private void uploadData(final int deleteSize, String data, final int lastId) {
        Log.d(TAG, "uploadData: start-->"+ data);
        OkGo.<BaseData<Integer>>post(db.getHttpUrl() + "/decathlon-store/dataCollection.")
                .params("shop_no", config.store)
                .params("json2", data)
                .params("last_no", lastId + "")
                .tag(httpTagUploade)
                .execute(new Callback<BaseData<Integer>>() {
                    @Override
                    public void success(BaseData<Integer> baseData) {
                        super.success(baseData);
                        if (baseData.ok) {
                            Log.d(TAG, "uploadData: success");
                            db.uploded(deleteSize);
                        } else {
                            if (baseData.data != null)
                                uploadError(deleteSize, baseData.message, baseData.data, lastId);
                        }
                    }
                });
    }

    private void uploadError(int deleteSize, String code, int serverId, int lastId) {
        Log.d(TAG, "uploadError: " + code + ": sId: " + serverId + "--lId: " + lastId);
        if (code.equals("2001")) {
            //前端大于后台
            syncData();
        } else if (code.equals("2002")) {
            //后台大于前端
            db.updateLastId(serverId);
            db.clearCaches(serverId - lastId);
            syncData();
        } else if (code.equals("1001")) {
            //数据为空
        } else if (code.equals("1002")) {
            //没有门店NO
        } else if (code.equals("1003")) {
            //上传失败(后台插入数据报错),数据重复
            if (deleteSize != 0) {
                db.updateLastId(lastId + deleteSize);
                db.clearCaches(deleteSize);
            }
            syncData();
        } else if (code.equals("1004")) {
            //上传失败(后台插入数据报错)
        }
    }

    private class Callback<T> extends HttpCallback<T> {
        @Override
        public void success(T t) {
            httpState(true);
        }

        @Override
        public void onError(Response<T> response) {
            super.onError(response);
            Log.e(TAG, "onError: " + response.getException().getMessage());
            if (IOException.class == response.getException().getClass()) {
                //有可能出现并非网络问题的情况：java.io.IOException: unexpected end of stream on Connection
                httpState(true);
            } else
                httpState(false);
        }
    }

    public void httpState(boolean b) {
        //连接服务器异常，请检查网络！
    }
}