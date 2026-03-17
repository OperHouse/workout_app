package com.example.workoutapp.Fragments.ActivityStatisticFragment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CustomBarChartView extends View {

    private Paint barPaint, goalLinePaint, gridLinePaint, textPaint, circlePaint, progressPaint;
    private float[] dataValues = new float[]{0, 0, 0, 0, 0, 0, 0};
    private float goalValue = 1000f;
    private int mainColor = Color.parseColor("#00BCD4");
    private String[] days = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
    private String[] dates = {"1", "2", "3", "4", "5", "6", "7"};

    public CustomBarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setStyle(Paint.Style.FILL);

        // Линия цели (Цвет задается динамически в onDraw)
        goalLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        goalLinePaint.setStyle(Paint.Style.STROKE);
        goalLinePaint.setStrokeWidth(4f);
        goalLinePaint.setPathEffect(new DashPathEffect(new float[]{15, 10}, 0));

        // Вспомогательная сетка (Непрозрачная)
        gridLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridLinePaint.setColor(Color.parseColor("#6E6E6E"));
        gridLinePaint.setStyle(Paint.Style.STROKE);
        gridLinePaint.setStrokeWidth(2f);
        gridLinePaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(26f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(4f);
        circlePaint.setColor(Color.parseColor("#EEEEEE"));

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(4f);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setData(float[] values, float goal, int color, String[] datesArray) {
        this.dataValues = values;
        this.goalValue = (goal <= 0) ? 1.0f : goal;
        this.mainColor = color;
        this.dates = datesArray;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float leftPadding = 100f;
        float rightPadding = 40f;
        float chartBottom = height - 120f;
        float chartTop = 60f;
        float chartHeight = chartBottom - chartTop;

        // Расчет максимума для динамической шкалы
        float maxVal = goalValue;
        for (float v : dataValues) if (v > maxVal) maxVal = v;
        float maxValueOnChart = maxVal * 1.2f;

        // 1. Отрисовка вспомогательной сетки и чисел Y
        int divisions = 4;
        textPaint.setColor(Color.GRAY);
        textPaint.setAlpha(255);
        for (int i = 0; i <= divisions; i++) {
            float value = (maxValueOnChart / divisions) * i;
            float y = chartBottom - (chartHeight * (value / maxValueOnChart));
            canvas.drawText(String.valueOf((int)value), leftPadding / 2, y + 10, textPaint);

            if (i > 0) { // Пропускаем линию 0
                canvas.drawLine(leftPadding, y, width - rightPadding, y, gridLinePaint);
            }
        }

        // 2. Отрисовка линии цели (Цвет совпадает с mainColor)
        float goalY = chartBottom - (chartHeight * (goalValue / maxValueOnChart));
        goalLinePaint.setColor(mainColor);
        goalLinePaint.setAlpha(255); // Без прозрачности
        canvas.drawLine(leftPadding, goalY, width - rightPadding, goalY, goalLinePaint);



        // 3. Отрисовка столбиков и подписей
        float colWidth = (width - leftPadding - rightPadding) / 7f;
        for (int i = 0; i < 7; i++) {
            float centerX = leftPadding + (colWidth * i) + (colWidth / 2);
            float barH = chartHeight * (dataValues[i] / maxValueOnChart);

            barPaint.setColor(mainColor);
            RectF barRect = new RectF(centerX - (colWidth / 4), chartBottom - barH, centerX + (colWidth / 4), chartBottom);
            canvas.drawRoundRect(barRect, 15, 15, barPaint);

            // День недели
            textPaint.setColor(Color.GRAY);
            canvas.drawText(days[i], centerX, chartBottom + 45, textPaint);

            // Круг прогресса
            float circleY = chartBottom + 90;
            canvas.drawCircle(centerX, circleY, 22, circlePaint);

            if (dataValues[i] >= goalValue) {
                progressPaint.setColor(Color.parseColor("#4CAF50")); // Выполнено - Зеленый
                canvas.drawCircle(centerX, circleY, 22, progressPaint);
            } else {
                progressPaint.setColor(mainColor);
                float sweepAngle = (dataValues[i] / goalValue) * 360f;
                canvas.drawArc(new RectF(centerX-22, circleY-22, centerX+22, circleY+22), -90, sweepAngle, false, progressPaint);
            }
            canvas.drawText(dates[i], centerX, circleY + 8, textPaint);
        }
    }
}
