package com.huanhong.decathlonstb.http;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.huanhong.decathlonstb.custom.TestLockView;
import com.huanhong.decathlonstb.db.DBManager;
import com.huanhong.decathlonstb.model.ModelComment;
import com.huanhong.decathlonstb.event.IEvent;
import com.huanhong.decathlonstb.event.EventWatch;
import com.huanhong.decathlonstb.netty.app.ApiClient;
import com.huanhong.decathlonstb.netty.app.ApiProtocol;
import com.huanhong.decathlonstb.service.ClientService;
import com.huanhong.decathlonstb.util.GsonUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by done on 2018/3/11.
 */

public class MyNanoHTTPD extends NanoHTTPD implements DBManager.OnDoneListener<ModelComment>, ApiClient.SeverPushListener {

    String TAG = getClass().getSimpleName();
    private ExecutorService fixedThreadPool = null;
    private DBManager db;
    private IEvent.Config config;
    private Response response = null;

    public MyNanoHTTPD(IEvent.Config config) {
        super(8887);
        this.config = config;
        db = config.db;
        fixedThreadPool = Executors.newFixedThreadPool(20);
        response = new Response("1");

        ApiClient.getInstance().setSeverPushListener(this);
    }

    public Response serve(IHTTPSession session) {
        String queryParams = session.getQueryParameterString();
        if (!TextUtils.isEmpty(queryParams)) {
            queryParams = decodePercent(queryParams);
            Log.d(TAG, "receive: " + queryParams);
            dealData(queryParams);
        }
        return response;
    }

    private void dealData(String data) {
        final String result = data;
        fixedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                if ("test".equals(result)) {
                    TestLockView.getOnLog().log("dealData: test ok");
                    return;
                }
                try {
                    ModelComment comment = oldComment(result);
                    if (comment == null){
                        comment = GsonUtils.getGson().fromJson(result, ModelComment.class);
                    }
                    if (comment == null || comment.verifyError(config.store)) {
                        TestLockView.getOnLog().throwError("dealData", new Throwable(comment == null ? "空数据" : "数据验证失败: " + JSON.toJSONString(comment)));
                        return;
                    }
                    comment.time = System.currentTimeMillis();
                    db.insertComment(comment, MyNanoHTTPD.this);
                    TestLockView.getOnLog().log("dealData: ok");
                } catch (Exception e) {
                    e.printStackTrace();
                    if (TestLockView.getOnLog() != null) {
                        TestLockView.getOnLog().throwError("dealData", new Throwable("数据格式错误: " + result));
                    }
                }
            }
        });
    }

    private ModelComment oldComment(String result) {
        ModelComment modelComment = null;
        if (countStr(result, "A") == 3) {
            Log.d(TAG, "oldComment");
            try {
                String data[] = result.split("A");
                modelComment = new ModelComment();
                modelComment.number = data[0];
                modelComment.type = Integer.parseInt(data[1]);
                modelComment.mac = data[3];
                modelComment.store = data[2];
            } catch (Exception e) {
                e.printStackTrace();
                if (TestLockView.getOnLog() != null)
                    TestLockView.getOnLog().throwError("oldComment", e);
            }
        }
        return modelComment;
    }

    private int countStr(String str1, String str2) {
        int counter = 0;
        if (str1.indexOf(str2) == -1) {
            return 0;
        }
        while (str1.indexOf(str2) != -1) {
            counter++;
            str1 = str1.substring(str1.indexOf(str2) + str2.length());
        }
        return counter;
    }

    @Override
    public void done(ModelComment modelComment) {
        //存到数据库后，如果是差评则发送至手表
        if (modelComment.badComment())
            EventWatch.getWatchEvent(config).ok();
    }

    @Override
    public void stop() {
        super.stop();
        if (!fixedThreadPool.isShutdown())
            fixedThreadPool.shutdown();
        fixedThreadPool = null;
    }

    @Override
    public void push(String json) {
        dealData(json);
    }
}