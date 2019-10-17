package com.huanhong.decathlonstb.custom.recycle;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class AutoRecyclerView extends RecyclerView {

    public AutoRecyclerView(Context context) {
        this(context, null);
    }

    public AutoRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public AutoRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent e) {
//        switch (e.getAction()) {
//            case MotionEvent.ACTION_UP:
//                startSmooth();
//                break;
//        }
//        return super.onTouchEvent(e);
//    }

    public void startSmoothUpToDown() {
        if (getAdapter() != null)
            startSmooth(0);
    }

    public void startSmoothDownToUp() {
        startSmooth(getAdapter().getItemCount() - 1);
    }

    private void startSmooth(final int position) {
        if (getAdapter() != null && getAdapter().getItemCount() > 0)
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    smoothScrollToPosition(position);
                }
            }, 500);
    }
}
