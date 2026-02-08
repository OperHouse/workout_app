package com.example.workoutapp.Fragments.ProfileFragments;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DataUsagePieChartView extends View {

    private Paint paint;
    private Paint textPaint;
    private Paint linePaint;
    private RectF rectF;
    private List<DataSegment> segments = new ArrayList<>();
    private String centerText = "0";

    public static class DataSegment {
        public float value;
        public int color;
        public String label;

        public DataSegment(float value, int color, String label) {
            this.value = value;
            this.color = color;
            this.label = label;
        }
    }

    public DataUsagePieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(60f); // Толщина кольца

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.BLACK); // Черные разделители
        linePaint.setStrokeWidth(4f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(50f);
        textPaint.setFakeBoldText(true);

        rectF = new RectF();
    }

    public void setData(List<DataSegment> newSegments) {
        this.segments = newSegments;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (segments.isEmpty()) return;

        float total = getTotal();
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) - 40f;
        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        float startAngle = -90f;

        for (DataSegment s : segments) {
            if (s.value == 0) continue;
            float sweepAngle = (s.value / total) * 360f;

            // 1. Рисуем цветной сектор
            paint.setColor(s.color);
            canvas.drawArc(rectF, startAngle, sweepAngle, false, paint);

            // 2. Рисуем черные разделители (линии на стыках)
            drawSeparator(canvas, centerX, centerY, radius, startAngle);

            startAngle += sweepAngle;

            // Рисуем последний разделитель после завершения цикла
            drawSeparator(canvas, centerX, centerY, radius, startAngle);
        }

        // 3. Текст в центре (общий объем)
        canvas.drawText(centerText, centerX, centerY + 15f, textPaint);

        Paint labelPaint = new Paint(textPaint);
        labelPaint.setTextSize(24f);
        labelPaint.setFakeBoldText(false);
        canvas.drawText("ВСЕГО", centerX, centerY - 45f, labelPaint);
    }

    private void drawSeparator(Canvas canvas, float cx, float cy, float r, float angle) {
        float stopX = (float) (cx + (r + 30f) * Math.cos(Math.toRadians(angle)));
        float stopY = (float) (cy + (r + 30f) * Math.sin(Math.toRadians(angle)));
        float startX = (float) (cx + (r - 30f) * Math.cos(Math.toRadians(angle)));
        float startY = (float) (cy + (r - 30f) * Math.sin(Math.toRadians(angle)));
        canvas.drawLine(startX, startY, stopX, stopY, linePaint);
    }

    private float getTotal() {
        float t = 0;
        for (DataSegment s : segments) t += s.value;
        return t == 0 ? 1 : t;
    }

    public void setCenterText(String text) {
        this.centerText = text;
    }
}