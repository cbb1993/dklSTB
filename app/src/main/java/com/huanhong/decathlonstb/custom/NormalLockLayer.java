package com.huanhong.decathlonstb.custom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.huanhong.decathlonstb.R;
import com.huanhong.decathlonstb.SetUpActivity;
import com.huanhong.decathlonstb.db.DBManager;
import com.huanhong.decathlonstb.util.SystemUi;

import java.text.DecimalFormat;

public class NormalLockLayer implements LockerView, InputWindow.InputEvent {

    private String TAG = getClass().getSimpleName();
    private WindowManager mWindowManager;
    private View mLockView;
    private LayoutParams mLockViewLayoutParams;
    private boolean isLocked;
    private Context mContext;
    private TextView textYears, textDays, textYearsPercent, textDaysPercent;
    private DecimalFormat df = new DecimalFormat("00.0");

    public NormalLockLayer(Context context) {
        mContext = context;
        init();
    }

    private void init() {
        isLocked = false;
        mLockView = View.inflate(mContext, R.layout.activity_detail, null);
        textYearsPercent = (TextView) mLockView.findViewById(R.id.years_percent);
        textDaysPercent = (TextView) mLockView.findViewById(R.id.days_percent);
        textYears = (TextView) mLockView.findViewById(R.id.years_count);
        textDays = (TextView) mLockView.findViewById(R.id.days_count);
        mLockView.findViewById(R.id.main_click).setOnLongClickListener(
                new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        InputWindow inputWindow = new InputWindow((ViewGroup) mLockView);
                        inputWindow.setInputEvent(NormalLockLayer.this);
                        return true;
                    }
                });
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mLockViewLayoutParams = new LayoutParams();
        // mLockViewLayoutParams.alpha = (float) 0.5;
        mLockViewLayoutParams.width = LayoutParams.MATCH_PARENT;
        mLockViewLayoutParams.height = LayoutParams.MATCH_PARENT;
        // 实现关键
        mLockViewLayoutParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
        mLockViewLayoutParams.flags = LayoutParams.FLAG_KEEP_SCREEN_ON
                | LayoutParams.FLAG_TURN_SCREEN_ON;
    }

    /**
     * 锁住
     */
    public synchronized void lock() {
        if (mLockView != null && !isLocked) {
            mWindowManager.addView(mLockView, mLockViewLayoutParams);
        }
        isLocked = true;
        SystemUi.fullScreen(mLockView);
    }

    /**
     * 解锁
     */
    public synchronized void unlock() {
        if (mWindowManager != null && isLocked) {
            mWindowManager.removeView(mLockView);
        }
        isLocked = false;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void httpState(boolean b) {
        mLockView.findViewById(R.id.internet_error).setVisibility(b ? View.GONE : View.VISIBLE);
    }


    @Override
    public void pass(String type) {
        unlock();
        Intent it = new Intent(mContext, SetUpActivity.class);
        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mContext.startActivity(it);
        ((Activity) mContext).finish();
    }

    @Override
    public void notifyUpdate(DBManager db) {
        Log.d(TAG, "notifyUpdate: ");
        int yc = db.getYearsCount();
        int dc = db.getDaysCount();
        textYears.setText(yc + "");
        textDays.setText(dc + "");
        textYearsPercent.setText(swcPercent(db.getYearsGoodCount(), yc));
        textDaysPercent.setText(swcPercent(db.getDaysGoodCount(), dc));
    }

    private String swcPercent(int count, int total) {
        if (count == 0 || total == 0) {
            return "00.0";
        } else {
            return df.format((double) count / (double) total * 100d);
        }
    }
}