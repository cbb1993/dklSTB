package com.huanhong.decathlonstb.custom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import com.huanhong.decathlonstb.R;
import com.huanhong.decathlonstb.SetUpActivity;
import com.huanhong.decathlonstb.custom.recycle.AutoRecyclerView;
import com.huanhong.decathlonstb.custom.recycle.RecyclerAdapter;
import com.huanhong.decathlonstb.custom.recycle.ScrollSpeedLinearLayoutManger;
import com.huanhong.decathlonstb.db.DBManager;
import com.huanhong.decathlonstb.model.Comment;
import com.huanhong.decathlonstb.util.CommentManager;
import com.huanhong.decathlonstb.util.SystemUi;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ScrollLockLayer implements LockerView, CommentManager.NotifyChange, InputWindow.InputEvent {
    private String TAG = getClass().getSimpleName();
    private WindowManager mWindowManager;
    private View rootView;
    private LayoutParams mLockViewLayoutParams;
    private boolean isLocked;
    private Context mContext;
    private TextView textYears, textDays, textYearsPercent, textDaysPercent;
    private DecimalFormat decimalFormat;
    private int lastPosition;
    private DecimalFormat df = new DecimalFormat("00.0");

    public ScrollLockLayer(Context context) {
        mContext = context;
        init();
    }

    private void init() {
        decimalFormat = new DecimalFormat("###,###");
        CommentManager.setNotifyChange(this);
        isLocked = false;
        rootView = View.inflate(mContext, R.layout.layout_main, null);
        textYearsPercent = (TextView) rootView.findViewById(R.id.years_percent);
        textYearsPercent.setIncludeFontPadding(false);
        textDaysPercent = (TextView) rootView.findViewById(R.id.days_percent);
        textYears = (TextView) rootView.findViewById(R.id.years_count);
        textDays = (TextView) rootView.findViewById(R.id.days_count);
        rootView.findViewById(R.id.main_click).setOnLongClickListener(
                new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        InputWindow inputWindow = new InputWindow((ViewGroup) rootView);
                        inputWindow.setInputEvent(ScrollLockLayer.this);
                        return true;
                    }
                });
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mLockViewLayoutParams = new LayoutParams();
        // mLockViewLayoutParams.alpha = (float) 0.5;
        mLockViewLayoutParams.width = LayoutParams.MATCH_PARENT;
        mLockViewLayoutParams.height = LayoutParams.MATCH_PARENT;
//        mLockViewLayoutParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
//        mLockViewLayoutParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
        mLockViewLayoutParams.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
    }

    private void setRecycleView() {
        Log.d(TAG, "scorll");
        List<Comment> comments = CommentManager.getList();
        if (comments == null || comments.size() == 0) {
            return;
        }
        AutoRecyclerView atuoRecyclerView = (AutoRecyclerView) rootView.findViewById(R.id.rc_comment);
        ViewGroup.LayoutParams layoutParams = atuoRecyclerView.getLayoutParams();
        layoutParams.height = mContext.getResources().getDisplayMetrics().heightPixels;
        final ScrollSpeedLinearLayoutManger layoutManger = new ScrollSpeedLinearLayoutManger(mContext);
        layoutManger.setSpeedSlow();
        layoutManger.setOrientation(LinearLayoutManager.VERTICAL);
        atuoRecyclerView.setLayoutManager(layoutManger);
        atuoRecyclerView.setAdapter(new RecyclerAdapter(mContext, comments));
        final int size = atuoRecyclerView.getAdapter().getItemCount();
        int contentSize = comments.size();
        atuoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int p = layoutManger.findFirstVisibleItemPosition();
                if (p < 0)
                    return;
                if (p != lastPosition) {
                    lastPosition = p;
                    if (size - p <= 100) {
                        CommentManager.savePosition(0);
                        setRecycleView();
                    }
                }
            }
        });
        lastPosition = CommentManager.getPosition() % contentSize;
        atuoRecyclerView.scrollToPosition(lastPosition);
        atuoRecyclerView.startSmoothDownToUp();
    }

    /**
     * 锁住
     */
    public synchronized void lock() {
        setRecycleView();
        if (rootView != null && !isLocked) {
            mWindowManager.addView(rootView, mLockViewLayoutParams);
        }
        isLocked = true;
        SystemUi.fullScreen(rootView);
    }

    /**
     * 解锁
     */
    public synchronized void unlock() {
        if (mWindowManager != null && isLocked) {
            mWindowManager.removeView(rootView);
        }
        isLocked = false;
    }

    @Override
    public void change() {
        Log.d(TAG, "change");
        setRecycleView();
    }

    @Override
    public void httpState(boolean b) {
        rootView.findViewById(R.id.internet_error).setVisibility(b ? View.GONE : View.VISIBLE);
    }

    public void onDestroy() {
        CommentManager.savePosition(lastPosition);
        CommentManager.setNotifyChange(null);
    }

    @Override
    public void pass(String type) {
        if (!type.equals(InputWindow.TEST_PASSWORD)) {
            unlock();
            Intent it = new Intent(mContext, SetUpActivity.class);
            mContext.startActivity(it);
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ((Activity) mContext).finish();
        } else {
            TestLockView testLockView = new TestLockView(mContext){
                @Override
                public synchronized void unlock() {
                    super.unlock();
                    SystemUi.fullScreen(rootView);
                }
            };
            testLockView.lock();
        }
    }

    @Override
    public void notifyUpdate(DBManager db) {
        int yc = db.getYearsCount();
        int dc = db.getDaysCount();
        textYears.setText(decimalFormat.format(yc));
        textDays.setText(decimalFormat.format(dc));
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