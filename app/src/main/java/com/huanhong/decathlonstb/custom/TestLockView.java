package com.huanhong.decathlonstb.custom;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.huanhong.decathlonstb.MainActivity;
import com.huanhong.decathlonstb.R;
import com.huanhong.decathlonstb.db.DBManager;
import com.huanhong.decathlonstb.http.callback.HttpCallback;
import com.huanhong.decathlonstb.util.DateFormatUitl;
import com.huanhong.decathlonstb.util.NetworkUtil;
import com.huanhong.decathlonstb.util.SystemUi;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.callback.Callback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;

import java.io.IOException;

public class TestLockView implements LockerView {

    private String TAG = getClass().getSimpleName();
    private WindowManager mWindowManager;
    private View rootView;
    private WindowManager.LayoutParams mLockViewLayoutParams;
    private boolean isLocked;
    private Context mContext;
    private StringBuffer testInfo = new StringBuffer();
    private TextView tvLog;
    private static OnLog onLog;

    public TestLockView(Context context) {
        mContext = context;
        init();
    }

    public static OnLog getOnLog() {
        return onLog;
    }

    private void init() {
        isLocked = false;
        rootView = View.inflate(mContext, R.layout.layout_test, null);
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mLockViewLayoutParams = new WindowManager.LayoutParams();
        mLockViewLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mLockViewLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        mLockViewLayoutParams.alpha = 0.8f;
//        mLockViewLayoutParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
//        mLockViewLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG;
        mLockViewLayoutParams.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
        tvLog = (TextView) rootView.findViewById(R.id.tv_log);
        rootView.findViewById(R.id.tv_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unlock();
            }
        });
        rootView.findViewById(R.id.tv_help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String help = "帮助：\n一、pad无法上传数据至sever:" +
                        "\n1、确保sever与pad在同一路由下、同一网段。" +
                        "\n2、路由未限制局域网内相互访问。" +
                        "\n3、sever点击测试未出现Error。" +
                        "\n4、pad的ip输入框输入的是sever的ip。" +
                        "\n5、pad与sever选择相同门店。" +
                        "\n二、设备通断测试：" +
                        "\n保证设备与sever同一路由网段下，满足以上第3点条件的情况下，打开设备的浏览器，输入[http://"
                        + NetworkUtil.getIp(mContext) +
                        ":8887?test]（注：不同sever测试地址可能不同，请按照显示的测试地址输入）进行访问。" +
                        "sever测试模式下显示日志[dealData: test ok]即表示设备与sever通信正常，设备可以是pad、手机、电脑等。";
                addLog(help);
            }
        });
        rootView.findViewById(R.id.tv_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testInfo = new StringBuffer();
                tvLog.setText(null);
            }
        });
        rootView.findViewById(R.id.tv_query).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ((MainActivity) mContext).getCacheAmount(new DBManager.OnDoneListener<Integer>() {
                        @Override
                        public void done(Integer integer) {
                            final int amount = integer;
                            rootView.post(new Runnable() {
                                @Override
                                public void run() {
                                    addLog("未上传的评价数量: " + amount);
                                }
                            });
                        }
                    });
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
        rootView.findViewById(R.id.tv_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkUtil.isNetAvailable(mContext)) {
                    addLog("Error: 网络未连接");
                } else if (!((MainActivity) mContext).isSeverAlive()) {
                    addLog("Error: Sever服务未启动");
                } else {
                    addLog("本机Ip：" + NetworkUtil.getIp(mContext));
                    OkGo.<String>get("http://tillreview.decathlon.com.cn:8091/decathlon-store/updateData.?shop_no=000000").execute(new AbsCallback<String>() {
                        @Override
                        public String convertResponse(okhttp3.Response response) {
                            String result = null;
                            try {
                                result = response.body().string();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            response.close();
                            Log.d(TAG, "convertResponse: " + result);
                            return result;
                        }

                        @Override
                        public void onSuccess(Response<String> response) {
                            addLog("访问服务器ok");
                        }

                        @Override
                        public void onError(Response<String> response) {
                            addLog("Error: 访问服务器: " + response.getException().getMessage());
                        }
                    });
                    addLog("自测start");
                    OkGo.<String>get("http://" + NetworkUtil.getIp(mContext) + ":8887?test").execute(new AbsCallback<String>() {
                        @Override
                        public String convertResponse(okhttp3.Response response) {
                            String result = null;
                            try {
                                result = response.body().string();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            response.close();
                            Log.d(TAG, "convertResponse: " + result);
                            return result;
                        }

                        @Override
                        public void onSuccess(Response<String> response) {
                            addLog("自测ok");
                        }

                        @Override
                        public void onError(Response<String> response) {
                            addLog("Error: 自测: " + response.getException().getMessage());
                        }
                    });
                }
            }
        });
    }

    private void addLog(String log) {
        if (testInfo.length() > 0)
            testInfo.append("\n");
        testInfo.append(DateFormatUitl.yyyyMMddmmss(System.currentTimeMillis()) + ": ");
        testInfo.append(log);
        tvLog.setText(testInfo);
    }

    /**
     * 锁住
     */
    public synchronized void lock() {
        if (rootView != null && !isLocked) {
            mWindowManager.addView(rootView, mLockViewLayoutParams);
        }
        isLocked = true;
        SystemUi.fullScreen(rootView);
        onLog = new OnLog() {
            @Override
            public void throwError(String type, Throwable throwable) {
                if (isLocked) {
                    final Throwable error = throwable;
                    final String str = type;
                    rootView.post(new Runnable() {
                        @Override
                        public void run() {
                            addLog("Error: " + str + ": " + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void log(String log) {
                final String l = log;
                rootView.post(new Runnable() {
                    @Override
                    public void run() {
                        addLog(l);
                    }
                });
            }
        };
    }

    /**
     * 解锁
     */
    public synchronized void unlock() {
        if (mWindowManager != null && isLocked) {
            mWindowManager.removeView(rootView);
        }
        isLocked = false;
        onLog = null;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void httpState(boolean b) {

    }

    @Override
    public void notifyUpdate(DBManager db) {

    }

    public interface OnLog {
        void throwError(String type, Throwable throwable);

        void log(String log);
    }
}