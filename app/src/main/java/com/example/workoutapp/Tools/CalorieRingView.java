package com.example.workoutapp.Tools;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.workoutapp.R;

public class CalorieRingView extends View {

    private Paint paint;
    private final RectF outerRect = new RectF();
    private final RectF innerRect = new RectF();

    // Ссылки на текстовые поля (управляются изнутри)
    private TextView goalTextView;
    private TextView remainingTextView;
    private View warningLayout;

    // Настройки толщины линий
    private final float macroStroke = 28f;
    private final float calorieStroke = 24f;
    private final float gapAngle = 8f;

    // Данные по умолчанию (чтобы View не была пустой до загрузки БД)
    private int protein = 0, proteinGoal = 120;
    private int fat = 0, fatGoal = 90;
    private int carbs = 0, carbsGoal = 378;
    private int eatenCalories = 0, goalCalories = 2800;

    private boolean isUsingDefaultGoals = false;

    // Цветовая палитра
    private final int colorGrayBg = ContextCompat.getColor(getContext(), R.color.black2);
    private final int colorProtein = Color.parseColor("#F48FB1");
    private final int colorFat = Color.parseColor("#FFD166");
    private final int colorCarbs = Color.parseColor("#81C784");
    private final int colorCalories = Color.parseColor("#0091FF");

    public CalorieRingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    /**
     * Связываем View с текстовыми полями из XML
     */
    public void setupLabels(TextView goalTv, TextView remainTv, View warningLayout) {
        this.goalTextView = goalTv;
        this.remainingTextView = remainTv;
        this.warningLayout = warningLayout; // Сохраняем ссылку на всё предупреждение
        updateTextContent();
    }

    /**
     * Основной метод обновления данных из фрагмента
     */
    public void setNutritionData(
            int p, int pg,
            int f, int fg,
            int c, int cg,
            int cal, int calg,
            boolean isUsingDefaultGoals
    ) {
        this.isUsingDefaultGoals = isUsingDefaultGoals;

        this.protein = p;
        this.proteinGoal = pg > 0 ? pg : 120;

        this.fat = f;
        this.fatGoal = fg > 0 ? fg : 90;

        this.carbs = c;
        this.carbsGoal = cg > 0 ? cg : 378;

        this.eatenCalories = cal;
        this.goalCalories = calg > 0 ? calg : 2800;

        updateTextContent();
        invalidate();
    }

    private void updateTextContent() {
        if (goalTextView != null) {
            goalTextView.setText("Цель: " + goalCalories + " Ккал");
        }

        // ЛОГИКА СКРЫТИЯ/ПОКАЗА ПРЕДУПРЕЖДЕНИЯ
        if (warningLayout != null) {
            warningLayout.setVisibility(
                    isUsingDefaultGoals ? View.VISIBLE : View.GONE
            );
        }

        if (remainingTextView != null) {
            int remaining = goalCalories - eatenCalories;
            if (remaining >= 0) {
                remainingTextView.setText("осталось " + remaining + " ккал");
                remainingTextView.setTextColor(Color.parseColor("#A0A0A0"));
            } else {
                remainingTextView.setText("перебор " + Math.abs(remaining) + " ккал");
                remainingTextView.setTextColor(Color.parseColor("#FF5252")); // Красный при превышении
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float size = Math.min(getWidth(), getHeight());

        // 1. ВНЕШНЕЕ КОЛЬЦО (БЖУ)
        float outerPadding = macroStroke / 2f + 10f;
        outerRect.set(outerPadding, outerPadding, size - outerPadding, size - outerPadding);

        int totalGoalBase = proteinGoal + fatGoal + carbsGoal;
        float proteinSweepMax = (360f * proteinGoal / totalGoalBase) - gapAngle;
        float fatSweepMax = (360f * fatGoal / totalGoalBase) - gapAngle;
        float carbsSweepMax = (360f * carbsGoal / totalGoalBase) - gapAngle;

        float currentAngle = -90f;

        drawSegment(canvas, outerRect, currentAngle, proteinSweepMax, protein, proteinGoal, colorProtein, macroStroke);
        currentAngle += proteinSweepMax + gapAngle;

        drawSegment(canvas, outerRect, currentAngle, fatSweepMax, fat, fatGoal, colorFat, macroStroke);
        currentAngle += fatSweepMax + gapAngle;

        drawSegment(canvas, outerRect, currentAngle, carbsSweepMax, carbs, carbsGoal, colorCarbs, macroStroke);

        // 2. ВНУТРЕННЕЕ КОЛЬЦО (КАЛОРИИ)
        float innerOffset = macroStroke + 35f; // Увеличенный отступ для красоты
        innerRect.set(innerOffset, innerOffset, size - innerOffset, size - innerOffset);

        paint.setStrokeWidth(calorieStroke);
        paint.setColor(colorGrayBg);
        canvas.drawArc(innerRect, 0, 360, false, paint);

        float calorieProgress = Math.min(1f, (float) eatenCalories / goalCalories);
        paint.setColor(colorCalories);
        canvas.drawArc(innerRect, -90, 360 * calorieProgress, false, paint);
    }

    private void drawSegment(Canvas canvas, RectF rect, float startAngle, float sweepMax, int val, int goal, int color, float strokeWidth) {
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(colorGrayBg);
        canvas.drawArc(rect, startAngle, sweepMax, false, paint);

        float progressFactor = Math.min(1f, (float) val / goal);
        if (progressFactor > 0) {
            paint.setColor(color);
            canvas.drawArc(rect, startAngle, sweepMax * progressFactor, false, paint);
        }
    }
}