package com.example.workoutapp.Tools;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class StatisticActivityRingView extends View {

    private Paint paint;
    private final RectF rect = new RectF();

    // Данные
    private float steps = 0, stepsGoal = 10000;
    private float calories = 0, caloriesGoal = 600;
    private float distance = 0, distanceGoal = 5.0f;

    // Настройки графики
    private final float strokeWidth = 32f; // Толщина колец
    private final float spacing = 12f;     // Расстояние между кольцами

    // Цвета
    private final int colorBg = Color.parseColor("#2C2C2E"); // Темный фон колец
    private final int colorSteps = Color.parseColor("#4CAF50");    // Зеленый
    private final int colorCalories = Color.parseColor("#FF9800"); // Оранжевый
    private final int colorDistance = Color.parseColor("#00B0FF"); // Голубой

    public StatisticActivityRingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND); // Закругленные края
    }

    public void setData(float steps, float sGoal, float cals, float cGoal, float dist, float dGoal) {
        this.steps = steps;
        this.stepsGoal = sGoal > 0 ? sGoal : 10000;
        this.calories = cals;
        this.caloriesGoal = cGoal > 0 ? cGoal : 600;
        this.distance = dist;
        this.distanceGoal = dGoal > 0 ? dGoal : 5;
        invalidate(); // Перерисовать
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) - strokeWidth;

        // 1. ВНЕШНЕЕ КОЛЬЦО (ШАГИ)
        drawRing(canvas, centerX, centerY, radius, steps, stepsGoal, colorSteps);

        // 2. СРЕДНЕЕ КОЛЬЦО (КАЛОРИИ)
        radius -= (strokeWidth + spacing);
        drawRing(canvas, centerX, centerY, radius, calories, caloriesGoal, colorCalories);

        // 3. ВНУТРЕННЕЕ КОЛЬЦО (ДИСТАНЦИЯ)
        radius -= (strokeWidth + spacing);
        drawRing(canvas, centerX, centerY, radius, distance, distanceGoal, colorDistance);
    }

    private void drawRing(Canvas canvas, float cx, float cy, float radius, float value, float goal, int color) {
        rect.set(cx - radius, cy - radius, cx + radius, cy + radius);

        // Рисуем фоновую "дорожку"
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(colorBg);
        canvas.drawArc(rect, 0, 360, false, paint);

        // Рисуем прогресс
        float sweepAngle = (value / goal) * 360f;
        if (sweepAngle > 360f) sweepAngle = 360f; // Ограничиваем полным кругом

        paint.setColor(color);
        canvas.drawArc(rect, -90, sweepAngle, false, paint);
    }
}