package com.huanhong.decathlonstb.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class NoPaddingTextView extends TextView {

    private TextPaint mTextPaint = new TextPaint();

    public NoPaddingTextView(Context context) {
        super(context);
    }

    public NoPaddingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTextPaint.setTextSize(50);
        mTextPaint.setColor(Color.BLUE);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        Log.d("Aige", "ascent：" + fontMetrics.ascent);
        Log.d("Aige", "top：" + fontMetrics.top);
        Log.d("Aige", "leading：" + fontMetrics.leading);
        Log.d("Aige", "descent：" + fontMetrics.descent);
        Log.d("Aige", "bottom：" + fontMetrics.bottom);

        mTextPaint.clearShadowLayer();
        canvas.drawText(getText().toString(), 0, (float) (Math.abs(fontMetrics.ascent) - (int) fontMetrics.descent / 1.5),
                mTextPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        int p = (int) (fontMetrics.descent / 1.5);
        setMeasuredDimension(widthSize, (int) Math.abs(fontMetrics.ascent) - p);
    }
}

