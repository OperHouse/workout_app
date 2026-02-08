package com.example.workoutapp.Fragments.ProfileFragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.workoutapp.Data.NutritionDao.BaseEatDao;
import com.example.workoutapp.Data.NutritionDao.ConnectingMealDao;
import com.example.workoutapp.Data.NutritionDao.ConnectingMealPresetDao;
import com.example.workoutapp.Data.NutritionDao.MealFoodDao;
import com.example.workoutapp.Data.NutritionDao.MealNameDao;
import com.example.workoutapp.Data.NutritionDao.PresetEatDao;
import com.example.workoutapp.Data.NutritionDao.PresetMealNameDao;
import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Data.WorkoutDao.BASE_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_PRESET_NAME_TABLE_DAO;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.R;
import com.example.workoutapp.Tools.OnNavigationVisibilityListener;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class DataManagementFragment extends Fragment {

    private DataUsagePieChartView pieChart;
    private SharedPreferences prefs;

    // DAO
    private BaseEatDao baseEatDao;
    private MealNameDao mealNameDao;
    private MealFoodDao mealFoodDao;
    private PresetMealNameDao presetMealNameDao;
    private PresetEatDao presetEatDao;
    private ConnectingMealDao connectingMealDao;
    private ConnectingMealPresetDao connectingMealPresetDao;

    private BASE_EXERCISE_TABLE_DAO baseExerciseDao;
    private WORKOUT_EXERCISE_TABLE_DAO workoutExerciseDao;
    private WORKOUT_PRESET_NAME_TABLE_DAO workoutPresetDao;

    private OnNavigationVisibilityListener navigationListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_data_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SQLiteDatabase db = MainActivity.getAppDataBase();
        prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE);

        // Инициализация всех DAO
        baseEatDao = new BaseEatDao(db);
        mealNameDao = new MealNameDao(db);
        mealFoodDao = new MealFoodDao(db);
        presetMealNameDao = new PresetMealNameDao(db);
        presetEatDao = new PresetEatDao(db);
        connectingMealDao = new ConnectingMealDao(db);
        connectingMealPresetDao = new ConnectingMealPresetDao(db);
        baseExerciseDao = new BASE_EXERCISE_TABLE_DAO(db);
        workoutExerciseDao = new WORKOUT_EXERCISE_TABLE_DAO(db);
        workoutPresetDao = new WORKOUT_PRESET_NAME_TABLE_DAO(db);

        pieChart = view.findViewById(R.id.data_pie_chart);
        initViews(view);
        updatePieChart();
    }

    private void initViews(View view) {
        view.findViewById(R.id.data_back_btn).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Очистка истории питания
        view.findViewById(R.id.btn_del_food_history).setOnClickListener(v -> confirmDeletion("Очистить дневник питания?", () -> {
            mealNameDao.deleteAll();
            mealFoodDao.deleteAll();
            connectingMealDao.deleteAll();
        }));

        // Очистка истории тренировок
        view.findViewById(R.id.btn_del_workout_history).setOnClickListener(v -> confirmDeletion("Очистить историю тренировок?", () -> {
            workoutExerciseDao.deleteAllWorkouts();
        }));

        // Очистка упражнений
        view.findViewById(R.id.btn_del_exercises).setOnClickListener(v -> confirmDeletion("Удалить все созданные упражнения?", () -> {
            baseExerciseDao.deleteAllExercises();
        }));

        // Полная очистка
        view.findViewById(R.id.btn_delete_everything).setOnClickListener(v -> confirmDeletion("Стереть АБСОЛЮТНО все данные?", () -> {
            SQLiteDatabase db = MainActivity.getAppDataBase();
            db.delete(AppDataBase.MEAL_NAME_TABLE, null, null);
            db.delete(AppDataBase.BASE_FOOD_TABLE, null, null);
            db.delete(AppDataBase.BASE_EXERCISE_TABLE, null, null);
            db.delete(AppDataBase.WORKOUT_EXERCISE_TABLE, null, null);
            db.execSQL("VACUUM");
        }));
    }

    private void updatePieChart() {
        new Thread(() -> {
            SQLiteDatabase db = MainActivity.getAppDataBase();
            java.io.File dbFile = requireContext().getDatabasePath("WorkoutApp.db");
            long dbSizeInBytes = (dbFile != null && dbFile.exists()) ? dbFile.length() : 0;
            String totalSizeFormatted = formatFileSize(dbSizeInBytes);

            // Используем ручной подсчет вместо DatabaseUtils из-за SQLCipher
            long cFood = getRowCount(db, AppDataBase.BASE_FOOD_TABLE);
            long hFood = getRowCount(db, AppDataBase.MEAL_NAME_TABLE);
            long pFood = getRowCount(db, AppDataBase.MEAL_PRESET_NAME_TABLE);
            long cEx = getRowCount(db, AppDataBase.BASE_EXERCISE_TABLE);
            long hEx = getRowCount(db, AppDataBase.WORKOUT_EXERCISE_TABLE);

            long totalRecords = cFood + hFood + pFood + cEx + hEx;

            List<DataUsagePieChartView.DataSegment> segments = new ArrayList<>();
            if (totalRecords == 0) {
                segments.add(new DataUsagePieChartView.DataSegment(1, Color.GRAY, "Пусто"));
            } else {
                if (cFood > 0) segments.add(new DataUsagePieChartView.DataSegment(cFood, Color.parseColor("#FF9800"), "Блюда"));
                if (hFood > 0) segments.add(new DataUsagePieChartView.DataSegment(hFood, Color.parseColor("#4CAF50"), "История еды"));
                if (pFood > 0) segments.add(new DataUsagePieChartView.DataSegment(pFood, Color.parseColor("#00B0FF"), "Пресеты еды"));
                if (cEx > 0) segments.add(new DataUsagePieChartView.DataSegment(cEx, Color.parseColor("#E91E63"), "Упражнения"));
                if (hEx > 0) segments.add(new DataUsagePieChartView.DataSegment(hEx, Color.parseColor("#9C27B0"), "Тренировки"));
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    pieChart.setCenterText(totalSizeFormatted);
                    pieChart.setData(segments);
                    populateLegend(segments, totalRecords);
                });
            }
        }).start();
    }

    // Метод-замена DatabaseUtils.queryNumEntries для SQLCipher
    private long getRowCount(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null);
        long count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getLong(0);
            }
            cursor.close();
        }
        return count;
    }

    private void populateLegend(List<DataUsagePieChartView.DataSegment> segments, long totalRecords) {
        LinearLayout container = getView().findViewById(R.id.legend_container);
        if (container == null) return;
        container.removeAllViews();

        for (int i = 0; i < segments.size(); i++) {
            DataUsagePieChartView.DataSegment segment = segments.get(i);
            View row = getLayoutInflater().inflate(R.layout.item_data_legend, container, false);

            androidx.cardview.widget.CardView colorCircle = row.findViewById(R.id.legend_color_card);
            TextView nameText = row.findViewById(R.id.legend_name_stats);
            TextView statsRight = row.findViewById(R.id.legend_size_right);

            colorCircle.setCardBackgroundColor(segment.color);
            nameText.setText(segment.label);

            if (totalRecords > 0 && !segment.label.equals("Пусто")) {
                int percent = Math.round((segment.value * 100f) / totalRecords);
                statsRight.setText(String.format("%d%% (%d)", percent, (int)segment.value));
            } else {
                statsRight.setText("");
            }

            container.addView(row);

            // Разделительная линия #6E6E6E
            if (i < segments.size() - 1) {
                View divider = new View(getContext());
                int height = (int) (1 * getResources().getDisplayMetrics().density);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
                params.setMargins(0, (int)(6 * getResources().getDisplayMetrics().density), 0, (int)(6 * getResources().getDisplayMetrics().density));
                divider.setLayoutParams(params);
                divider.setBackgroundColor(Color.parseColor("#6E6E6E"));
                container.addView(divider);
            }
        }
    }

    private void confirmDeletion(String message, Runnable action) {
        new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Внимание")
                .setMessage(message)
                .setPositiveButton("Удалить", (d, w) -> new Thread(() -> {
                    action.run();
                    requireActivity().runOnUiThread(() -> {
                        updatePieChart();
                        Toast.makeText(getContext(), "Готово", Toast.LENGTH_SHORT).show();
                    });
                }).start())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new java.text.DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    @Override
    public void onResume() {
        super.onResume();
        if (navigationListener != null) navigationListener.setBottomNavVisibility(false);
        updatePieChart();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (navigationListener != null) navigationListener.setBottomNavVisibility(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnNavigationVisibilityListener) {
            navigationListener = (OnNavigationVisibilityListener) context;
        }
    }
}