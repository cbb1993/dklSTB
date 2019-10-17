package com.huanhong.decathlonstb.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.huanhong.decathlonstb.R;

public class Star extends View {
    private Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
            R.drawable.star_empty);
    private Bitmap bitmapSelect = BitmapFactory.decodeResource(getResources(),
            R.drawable.star_full);
    private int maxStarCount = 5;
    private int selectedStarCount;
    public boolean touchEnable;
    public boolean onlyShowSelectStar;
    private int starPadding = 0;
    private OnStarListener onStarListener;

    public Star(Context context) {
        super(context);
    }

    public Star(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Star(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setStarPadding(int starPadding) {
        this.starPadding = starPadding;
        invalidate();
    }

    public void setImage(Bitmap normal, Bitmap selected) {
        bitmap = normal;
        bitmapSelect = selected;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        if (onlyShowSelectStar) {
            for (int i = 0; i < selectedStarCount; i++) {
                canvas.drawBitmap(bitmapSelect, i * (bitmap.getWidth() + starPadding), 0,
                        paint);
            }
        } else
            for (int i = 0; i < maxStarCount; i++) {
                canvas.drawBitmap(selectedStarCount > i ? bitmapSelect : bitmap, i
                        * (bitmap.getWidth() + starPadding), 0, paint);
            }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((bitmap.getWidth() + starPadding) * maxStarCount,
                bitmap.getHeight());
    }

    public void setMaxStarCount(int star) {
        this.maxStarCount = star;
    }

    public void setSelectedStarCount(int star) {
        if (star > maxStarCount)
            star = maxStarCount;
        this.selectedStarCount = star;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (touchEnable) {
            int w = getWidth() / maxStarCount;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                selectedStarCount = (int) event.getX() / w + 1;
                if (selectedStarCount > maxStarCount)
                    selectedStarCount = maxStarCount;
                invalidate();
                if (onStarListener != null)
                    onStarListener.selected(selectedStarCount);
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    public int getSelectedStarCount() {
        return selectedStarCount;
    }

    public void setOnStarListener(OnStarListener onStarListener) {
        this.onStarListener = onStarListener;
    }

    public interface OnStarListener {
        void selected(int star);
    }
}
