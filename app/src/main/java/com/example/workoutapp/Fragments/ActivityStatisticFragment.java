package com.example.workoutapp.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Data.ProfileDao.DailyActivityTrackingDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.ProfileModels.DailyActivityTrackingModel;
import com.example.workoutapp.R;
import com.example.workoutapp.Tools.OnNavigationVisibilityListener;
import com.example.workoutapp.Tools.StatisticActivityRingView;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class ActivityStatisticFragment extends Fragment {

    private RecyclerView recyclerView;
    private CustomBarChartView chartSteps, chartCalories, chartDistance;
    private ImageView backBtn, calendarBtn;
    private StatisticActivityRingView rings;

    private TextView tvStepsVal, tvCalVal, tvDistVal;
    private TextView tvMonthYear;

    private DailyActivityTrackingDao trackingDao;
    private OnNavigationVisibilityListener navigationListener;

    private final SimpleDateFormat monthFormat = new SimpleDateFormat("LLLL yyyy", new Locale("ru"));
    private final SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private final int OFFSET = 5000; // Константа из адаптера

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity_statistic, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (MainActivity.getAppDataBase() != null) {
            trackingDao = new DailyActivityTrackingDao(MainActivity.getAppDataBase());
        }

        initViews(view);
        setupActivityStats(view);
        setupWeekCalendar();

        // Загрузка данных за сегодня при старте
        updateDailyData(dbDateFormat.format(Calendar.getInstance().getTime()));
    }

    private void initViews(View view) {
        backBtn = view.findViewById(R.id.stats_back_btn);
        backBtn.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        calendarBtn = view.findViewById(R.id.stats_calendar_btn);
        calendarBtn.setOnClickListener(v -> showDatePicker());

        recyclerView = view.findViewById(R.id.week_calendar_rv);
        chartSteps = view.findViewById(R.id.chart_steps);
        chartCalories = view.findViewById(R.id.chart_calories);
        chartDistance = view.findViewById(R.id.chart_distance);
        rings = view.findViewById(R.id.activityRing);
        tvMonthYear = view.findViewById(R.id.tv_month_year);
    }

    private void setupWeekCalendar() {
        WeekCalendarAdapter adapter = new WeekCalendarAdapter((fullDate, dayNumber) -> {
            updateDailyData(fullDate);
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Позиционирование на текущую неделю
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysFromMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - 2;

        recyclerView.scrollToPosition(OFFSET - daysFromMonday);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                updateMonthLabel();
            }
        });

        recyclerView.post(this::updateMonthLabel);
    }

    private void updateMonthLabel() {
        LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (lm != null && tvMonthYear != null) {
            int firstPos = lm.findFirstVisibleItemPosition();
            if (firstPos == RecyclerView.NO_POSITION) return;

            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR, firstPos - OFFSET);

            String monthName = monthFormat.format(c.getTime());
            if (monthName.length() > 0) {
                monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
            }
            tvMonthYear.setText(monthName);
        }
    }

    private void showDatePicker() {
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())
                .build();

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTheme(R.style.CustomCalendarTheme)
                .setTitleText("Выберите дату")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraints)
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            // 1. Получаем дату правильно (без сдвига часовых поясов)
            Calendar selectedCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            selectedCal.setTimeInMillis(selection);

            // Форматируем для БД
            String selectedDateStr = dbDateFormat.format(selectedCal.getTime());

            // 2. Обновляем все данные (Кольца + Графики)
            updateDailyData(selectedDateStr);

            // 3. Вычисляем позицию для ресайклера
            Calendar today = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            // Обнуляем время для чистого расчета дней
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            long diffInMillis = selection - today.getTimeInMillis();
            long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

            int targetPos = OFFSET + (int) diffInDays;

            if (recyclerView.getAdapter() instanceof WeekCalendarAdapter) {
                ((WeekCalendarAdapter) recyclerView.getAdapter()).setSelectedPosition(targetPos);
            }
            recyclerView.scrollToPosition(targetPos);

            // Используем scrollToPositionWithOffset чтобы дата встала ПЕРВОЙ в списке
            LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (lm != null) {
                lm.scrollToPositionWithOffset(targetPos, 0);
            }

            // 4. Обновляем заголовок месяца с задержкой, чтобы ресайклер успел прокрутиться
            recyclerView.postDelayed(this::updateMonthLabel, 100);
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    private void updateDailyData(String fullDate) {
        if (trackingDao == null) return;
        DailyActivityTrackingModel stats = trackingDao.getActivityByDate(fullDate);

        float goalSteps = 10000f;
        float goalCals = 600f;
        float goalDist = 8.0f;

        if (stats != null) {
            float currentSteps = (float) stats.getTrackingActivitySteps();
            float currentCals = stats.getTrackingCaloriesBurned();
            float currentDist = calculateDistance(currentSteps);

            rings.setData(currentSteps, goalSteps, currentCals, goalCals, currentDist, goalDist);
            tvStepsVal.setText(String.format(Locale.getDefault(), "%.0f / %.0f", currentSteps, goalSteps));
            tvCalVal.setText(String.format(Locale.getDefault(), "%.0f / %.0f ккал", currentCals, goalCals));
            tvDistVal.setText(String.format(Locale.getDefault(), "%.1f / %.1f км", currentDist, goalDist));
        } else {
            rings.setData(0, goalSteps, 0, goalCals, 0, goalDist);
            tvStepsVal.setText("0 / 10000");
            tvCalVal.setText("0 / 600 ккал");
            tvDistVal.setText("0.0 / 8.0 км");
        }
        updateChartsForDate(fullDate);
    }

    private void updateChartsForDate(String selectedDate) {
        if (trackingDao == null) return;
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(dbDateFormat.parse(selectedDate));
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

            float[] stepsData = new float[7];
            float[] calData = new float[7];
            float[] distData = new float[7];
            String[] daysLabels = new String[7];
            SimpleDateFormat dayDf = new SimpleDateFormat("d", Locale.getDefault());

            for (int i = 0; i < 7; i++) {
                String dateStr = dbDateFormat.format(cal.getTime());
                daysLabels[i] = dayDf.format(cal.getTime());
                DailyActivityTrackingModel dayStats = trackingDao.getActivityByDate(dateStr);

                if (dayStats != null) {
                    stepsData[i] = (float) dayStats.getTrackingActivitySteps();
                    calData[i] = dayStats.getTrackingCaloriesBurned();
                    distData[i] = calculateDistance(stepsData[i]);
                } else {
                    stepsData[i] = 0f; calData[i] = 0f; distData[i] = 0f;
                }
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            if (chartSteps != null) chartSteps.setData(stepsData, 10000f, Color.parseColor("#4CAF50"), daysLabels);
            if (chartCalories != null) chartCalories.setData(calData, 600f, Color.parseColor("#FF9800"), daysLabels);
            if (chartDistance != null) chartDistance.setData(distData, 8.0f, Color.parseColor("#00B0FF"), daysLabels);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private float calculateDistance(float steps) {
        return (steps * 0.75f) / 1000f;
    }

    private void setupActivityStats(View rootView) {
        View stepsInclude = rootView.findViewById(R.id.statSteps);
        View burnedInclude = rootView.findViewById(R.id.statBurned);
        View distanceInclude = rootView.findViewById(R.id.statDistance);

        if (stepsInclude != null) {
            ((TextView) stepsInclude.findViewById(R.id.item_title)).setText("Шаги");
            ((TextView) stepsInclude.findViewById(R.id.item_title)).setTextColor(Color.parseColor("#4CAF50"));
            tvStepsVal = stepsInclude.findViewById(R.id.carbs_value_tv);
        }
        if (burnedInclude != null) {
            ((TextView) burnedInclude.findViewById(R.id.item_title)).setText("Калории");
            ((TextView) burnedInclude.findViewById(R.id.item_title)).setTextColor(Color.parseColor("#FF9800"));
            tvCalVal = burnedInclude.findViewById(R.id.carbs_value_tv);
        }
        if (distanceInclude != null) {
            ((TextView) distanceInclude.findViewById(R.id.item_title)).setText("Дистанция");
            ((TextView) distanceInclude.findViewById(R.id.item_title)).setTextColor(Color.parseColor("#D1D9FF"));
            tvDistVal = distanceInclude.findViewById(R.id.carbs_value_tv);
        }
    }

    @Override public void onAttach(@NonNull Context context) { super.onAttach(context); if (context instanceof OnNavigationVisibilityListener) navigationListener = (OnNavigationVisibilityListener) context; }
    @Override public void onResume() { super.onResume(); if (navigationListener != null) navigationListener.setBottomNavVisibility(false); }
    @Override public void onPause() { super.onPause(); if (navigationListener != null) navigationListener.setBottomNavVisibility(true); }
    @Override public void onDetach() { super.onDetach(); navigationListener = null; }
}