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

public class ActivityRingView extends View {

    private Paint paint;
    private final RectF rectF = new RectF();

    // Labels
    private TextView stepsValueTv, burnedValueTv, distanceValueTv;
    private TextView stepsTitleTv, burnedTitleTv, distanceTitleTv;
    private View warningLayout;

    // Графика
    private final float strokeWidth = 35f;
    private final int colorBg = ContextCompat.getColor(getContext(), R.color.black2);
    private final int colorSteps = Color.parseColor("#4CAF50");

    // Дефолты
    private static final int DEF_STEPS = 10000;
    private static final int DEF_BURNED = 300;
    private static final double DEF_DISTANCE = 5.0;

    // Данные
    private int steps = 0, stepsGoal = DEF_STEPS;
    private int burned = 0, burnedGoal = DEF_BURNED;
    private double distance = 0.0;
    private boolean isUsingDefaultGoals = false;

    public ActivityRingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    /**
     * Привязка карточек и предупреждения
     */
    public void setupLabels(
            View stepsCard,
            View burnedCard,
            View distanceCard,
            View warningLayout
    ) {
        this.warningLayout = warningLayout;

        stepsTitleTv = stepsCard.findViewById(R.id.item_title);
        stepsValueTv = stepsCard.findViewById(R.id.carbs_value_tv);

        burnedTitleTv = burnedCard.findViewById(R.id.item_title);
        burnedValueTv = burnedCard.findViewById(R.id.carbs_value_tv);

        distanceTitleTv = distanceCard.findViewById(R.id.item_title);
        distanceValueTv = distanceCard.findViewById(R.id.carbs_value_tv);

        updateTextContent();
    }

    public void setActivityData(
            int steps,
            int stepsGoal,
            int burned,
            int burnedGoal,
            double distance,
            boolean isUsingDefaultGoals
    ) {
        this.isUsingDefaultGoals = isUsingDefaultGoals;

        this.steps = steps;
        this.stepsGoal = stepsGoal > 0 ? stepsGoal : DEF_STEPS;

        this.burned = burned;
        this.burnedGoal = burnedGoal > 0 ? burnedGoal : DEF_BURNED;

        this.distance = distance;

        updateTextContent();
        invalidate();
    }

    private void updateTextContent() {
        // Шаги
        stepsTitleTv.setText("Шаги");
        stepsTitleTv.setTextColor(colorSteps);
        stepsValueTv.setText(steps + " / " + stepsGoal);

        // Калории
        burnedTitleTv.setText("Калории");
        burnedTitleTv.setTextColor(Color.parseColor("#F48FB1"));
        burnedValueTv.setText(burned + " / " + burnedGoal);

        // Дистанция
        distanceTitleTv.setText("Дистанция");
        distanceTitleTv.setTextColor(Color.parseColor("#2196F3"));
        distanceValueTv.setText(String.format("%.2f км", distance));


        if (warningLayout != null) {
            warningLayout.setVisibility(
                    isUsingDefaultGoals ? VISIBLE : GONE
            );
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float size = Math.min(getWidth(), getHeight());
        float pad = strokeWidth / 2f + 10;
        rectF.set(pad, pad, size - pad, size - pad);

        // Фон
        paint.setColor(colorBg);
        canvas.drawArc(rectF, 0, 360, false, paint);

        // Прогресс шагов
        float progress = Math.min(1f, (float) steps / stepsGoal);
        paint.setColor(colorSteps);
        canvas.drawArc(rectF, -90, 360 * progress, false, paint);
    }
}