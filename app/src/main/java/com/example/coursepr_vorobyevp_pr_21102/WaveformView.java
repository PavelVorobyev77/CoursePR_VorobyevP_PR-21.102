package com.example.coursepr_vorobyevp_pr_21102;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class WaveformView extends View {

    private byte[] mWaveform;

    private Paint mPaint;

    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setColor(0xFF4CAF50); // Зеленый цвет для визуализации
        mPaint.setStrokeWidth(5);
    }

    public void setWaveform(byte[] waveform) {
        mWaveform = waveform;
        invalidate(); // Перерисовываем вид
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mWaveform == null) {
            return;
        }

        int width = getWidth();
        int height = getHeight();

        int centerY = height / 2;

        for (int i = 0; i < mWaveform.length - 1; i++) {
            float startX = (float) i / mWaveform.length * width;
            float startY = centerY + mWaveform[i];
            float stopX = (float) (i + 1) / mWaveform.length * width;
            float stopY = centerY + mWaveform[i + 1];
            canvas.drawLine(startX, startY, stopX, stopY, mPaint);
        }
    }
}
