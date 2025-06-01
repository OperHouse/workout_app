package com.example.workoutapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class NutritionCircleView extends View {

    private float protein = 0;
    private float fat = 0;
    private float carb = 0;

    private Paint proteinPaint = new Paint();
    private Paint fatPaint = new Paint();
    private Paint carbPaint = new Paint();

    public NutritionCircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        proteinPaint.setColor(0xFFD32F2F); // насыщенный красный
        fatPaint.setColor(0xFFFFEB3B);     // яркий жёлтый
        carbPaint.setColor(0xFF81C784);    // зелёный (как и раньше)

        proteinPaint.setStyle(Paint.Style.STROKE);
        fatPaint.setStyle(Paint.Style.STROKE);
        carbPaint.setStyle(Paint.Style.STROKE);

        proteinPaint.setStrokeWidth(40f);
        fatPaint.setStrokeWidth(40f);
        carbPaint.setStrokeWidth(40f);

        proteinPaint.setAntiAlias(true);
        fatPaint.setAntiAlias(true);
        carbPaint.setAntiAlias(true);
    }

    public void setMacros(float protein, float fat, float carb) {
        this.protein = protein;
        this.fat = fat;
        this.carb = carb;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float total = protein + fat + carb;
        if (total <= 0) return;

        float startAngle = -90f;
        float sweepProtein = (protein / total) * 360f;
        float sweepFat = (fat / total) * 360f;
        float sweepCarb = (carb / total) * 360f;

        int padding = 60;
        int size = Math.min(getWidth(), getHeight()) - padding * 2;

        canvas.drawArc(
                padding, padding,
                padding + size, padding + size,
                startAngle, sweepProtein, false, proteinPaint
        );

        canvas.drawArc(
                padding, padding,
                padding + size, padding + size,
                startAngle + sweepProtein, sweepFat, false, fatPaint
        );

        canvas.drawArc(
                padding, padding,
                padding + size, padding + size,
                startAngle + sweepProtein + sweepFat, sweepCarb, false, carbPaint
        );
    }
}
