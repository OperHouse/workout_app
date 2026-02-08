package com.example.workoutapp.Fragments.ProfileFragments;

import android.app.AlertDialog;
import android.content.Context;
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

import com.example.workoutapp.Data.NutritionDao.MealNameDao;
import com.example.workoutapp.Data.Tables.AppDataBase;
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
    private OnNavigationVisibilityListener navigationListener;

    // DAO
    private MealNameDao mealNameDao;
    private WORKOUT_EXERCISE_TABLE_DAO workoutExerciseDao;
    private WORKOUT_PRESET_NAME_TABLE_DAO workoutPresetDao;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_data_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SQLiteDatabase db = MainActivity.getAppDataBase();

        // Инициализация основных DAO для комплексного удаления
        mealNameDao = new MealNameDao(db);
        workoutExerciseDao = new WORKOUT_EXERCISE_TABLE_DAO(db);
        workoutPresetDao = new WORKOUT_PRESET_NAME_TABLE_DAO(db);

        pieChart = view.findViewById(R.id.data_pie_chart);
        initViews(view);
        updatePieChart();
    }

    private void initViews(View view) {
        view.findViewById(R.id.data_back_btn).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // --- ГРУППА: ТРЕНИРОВКИ ---

        // Удалить историю тренировок
        view.findViewById(R.id.btn_del_workout_history).setOnClickListener(v ->
                confirmDeletion("Очистить историю тренировок и все сеты?", () -> clearCategoryData(1)));

        // Удалить все упражнения
        view.findViewById(R.id.btn_del_exercises).setOnClickListener(v ->
                confirmDeletion("Удалить все созданные упражнения?", () -> clearCategoryData(2)));

        // Удалить пресеты тренировок
        view.findViewById(R.id.btn_del_workout_presets).setOnClickListener(v ->
                confirmDeletion("Удалить все шаблоны тренировок?", () -> clearCategoryData(3)));


        // --- ГРУППА: ПИТАНИЕ ---

        // Очистить дневник питания
        view.findViewById(R.id.btn_del_food_history).setOnClickListener(v ->
                confirmDeletion("Удалить все записи о приемах пищи?", () -> clearCategoryData(4)));

        View btnDelStats = view.findViewById(R.id.btn_del_food_stats);
        if (btnDelStats != null) {
            btnDelStats.setOnClickListener(v ->
                    confirmDeletion("Очистить ежедневную статистику калорий (графики)?", () -> clearCategoryData(12)));
        }

        // Удалить созданные блюда
        view.findViewById(R.id.btn_del_custom_dishes).setOnClickListener(v ->
                confirmDeletion("Удалить все блюда из вашей библиотеки?", () -> clearCategoryData(5)));

        // Удалить пресеты приемов пищи
        view.findViewById(R.id.btn_del_meal_presets).setOnClickListener(v ->
                confirmDeletion("Удалить все шаблоны приемов пищи?", () -> clearCategoryData(6)));


        // --- ГРУППА: ЦЕЛИ И ПРОФИЛЬ ---

        // Удалить данные активности
        view.findViewById(R.id.btn_del_activity).setOnClickListener(v ->
                confirmDeletion("Удалить историю шагов и активности?", () -> clearCategoryData(7)));

        // Сбросить общие цели
        view.findViewById(R.id.btn_del_general_goals).setOnClickListener(v ->
                confirmDeletion("Сбросить общие цели?", () -> clearCategoryData(8)));

        // Сбросить цели активности
        view.findViewById(R.id.btn_del_activity_goals).setOnClickListener(v ->
                confirmDeletion("Сбросить цели по шагам и калориям?", () -> clearCategoryData(9)));

        // Сбросить цели питания
        view.findViewById(R.id.btn_del_food_goals).setOnClickListener(v ->
                confirmDeletion("Сбросить КБЖУ цели?", () -> clearCategoryData(10)));

        // Удалить профиль пользователя
        view.findViewById(R.id.btn_del_profile).setOnClickListener(v ->
                confirmDeletion("Удалить данные профиля и историю веса?", () -> clearCategoryData(11)));


        // --- ПОЛНОЕ УДАЛЕНИЕ ---
        view.findViewById(R.id.btn_delete_everything).setOnClickListener(v ->
                confirmDeletion("ВНИМАНИЕ! Это действие сотрет ВСЕ данные безвозвратно. Продолжить?", () -> {
                    // Запускаем в новом потоке, чтобы не блокировать интерфейс
                    new Thread(() -> {
                        SQLiteDatabase db = MainActivity.getAppDataBase();

                        // Проходим по всем категориям удаления
                        for (int i = 1; i <= 12; i++) {
                            clearCategoryDataInternal(db, i);
                        }

                        // Сжимаем файл БД после удаления всех строк
                        db.execSQL("VACUUM");

                        // Возвращаемся в главный поток для обновления UI
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                updatePieChart(); // Обновляем график (он станет "Пустым")
                                Toast.makeText(getContext(), "Все данные стерты", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }).start();
                }));
    }

    private void clearCategoryData(int categoryId) {
        new Thread(() -> {
            SQLiteDatabase db = MainActivity.getAppDataBase();
            clearCategoryDataInternal(db, categoryId);
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    updatePieChart();
                    Toast.makeText(getContext(), "Данные успешно удалены", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void clearCategoryDataInternal(SQLiteDatabase db, int categoryId) {
        db.beginTransaction();
        try {
            switch (categoryId) {
                case 1: // История тренировок
                    workoutExerciseDao.deleteAllWorkouts();
                    break;
                case 2: // Упражнения
                    db.delete(AppDataBase.BASE_EXERCISE_TABLE, null, null);
                    break;
                case 3: // Пресеты тренировок
                    workoutPresetDao.deleteAllPresets();
                    break;
                case 4: // Дневник питания (История приемов пищи)
                    // Удаляем сами приемы пищи (завтрак, обед и т.д. за конкретные даты)
                    db.delete(AppDataBase.MEAL_NAME_TABLE, null, null);
                    // Удаляем связи: какие продукты были в этих приемах пищи
                    db.delete(AppDataBase.MEAL_FOOD_TABLE, null, null);
                    db.delete(AppDataBase.CONNECTING_MEAL_TABLE, null, null);
                    break;
                case 5: // Созданные блюда (Библиотека продуктов)
                    db.delete(AppDataBase.BASE_FOOD_TABLE, null, null);
                    break;
                case 6: // Пресеты еды (Шаблоны)
                    db.delete(AppDataBase.MEAL_PRESET_NAME_TABLE, null, null);
                    // Удаляем связи продуктов внутри шаблонов
                    db.delete("preset_food_table", null, null);
                    db.delete(AppDataBase.CONNECTING_MEAL_PRESET_TABLE, null, null);
                    break;
                case 7: // Данные активности
                    db.delete(AppDataBase.DAILY_ACTIVITY_TRACKING_TABLE, null, null);
                    break;
                case 8: // Общие цели
                    db.delete(AppDataBase.GENERAL_GOAL_TABLE, null, null);
                    break;
                case 9: // Цели активности
                    db.delete(AppDataBase.ACTIVITY_GOAL_TABLE, null, null);
                    break;
                case 10: // Цели питания
                    db.delete(AppDataBase.FOOD_GAIN_GOAL_TABLE, null, null);
                    break;
                case 11: // Профиль и вес
                    db.delete(AppDataBase.USER_PROFILE_TABLE, null, null);
                    db.delete(AppDataBase.WEIGHT_HISTORY_TABLE, null, null);
                    break;
                case 12: // НОВАЯ КНОПКА: Очистить ежедневную статистику БЖУ
                    // Удаляет только итоговые цифры за день в календаре питания
                    db.delete(AppDataBase.DAILY_FOOD_TRACKING_TABLE, null, null);
                    break;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void updatePieChart() {
        new Thread(() -> {
            SQLiteDatabase db = MainActivity.getAppDataBase();

            // --- РАСЧЕТ КОЛИЧЕСТВА ЗАПИСЕЙ ---

            // 1. История тренировок
            long hWorkouts = getRowCount(db, AppDataBase.WORKOUT_EXERCISE_TABLE);

            // 2. Упражнения
            long cExercises = getRowCount(db, AppDataBase.BASE_EXERCISE_TABLE);

            // 3. Пресеты тренировок
            long pWorkouts = getRowCount(db, AppDataBase.WORKOUT_PRESET_NAME_TABLE);

            // 4. Дневник питания
            long hFood = getRowCount(db, AppDataBase.MEAL_NAME_TABLE);

            // 12. Статистика калорий
            long foodStats = getRowCount(db, AppDataBase.DAILY_FOOD_TRACKING_TABLE);

            // 5. Созданные блюда
            long cFood = getRowCount(db, AppDataBase.BASE_FOOD_TABLE);

            // 6. Пресеты приемов пищи
            long pFood = getRowCount(db, AppDataBase.MEAL_PRESET_NAME_TABLE);

            // 7. Данные активности
            long activityData = getRowCount(db, AppDataBase.DAILY_ACTIVITY_TRACKING_TABLE);

            // 8. Профиль и цели
            long profileData = getRowCount(db, AppDataBase.USER_PROFILE_TABLE)
                    + getRowCount(db, AppDataBase.GENERAL_GOAL_TABLE)
                    + getRowCount(db, AppDataBase.FOOD_GAIN_GOAL_TABLE)
                    + getRowCount(db, AppDataBase.ACTIVITY_GOAL_TABLE)
                    + getRowCount(db, AppDataBase.WEIGHT_HISTORY_TABLE);

            // ОБЩЕЕ КОЛИЧЕСТВО СТРОК
            long totalRecords = hWorkouts + cExercises + pWorkouts + hFood + foodStats + cFood + pFood + activityData + profileData;

            // Текст для центра (например: "1,250 записей")
            String totalRecordsFormatted = java.text.NumberFormat.getInstance().format(totalRecords) + " зап.";

            // --- ФОРМИРОВАНИЕ СЕГМЕНТОВ ---
            List<DataUsagePieChartView.DataSegment> segments = new ArrayList<>();
            if (totalRecords == 0) {
                segments.add(new DataUsagePieChartView.DataSegment(1f, 0xFF9E9E9E, "Пусто"));
            } else {
                segments.add(new DataUsagePieChartView.DataSegment((float) hWorkouts, 0xFFE57373, "История тренировок"));
                segments.add(new DataUsagePieChartView.DataSegment((float) cExercises, 0xFF64B5F6, "Упражнения"));
                segments.add(new DataUsagePieChartView.DataSegment((float) pWorkouts, 0xFFFFB74D, "Пресеты тренировок"));
                segments.add(new DataUsagePieChartView.DataSegment((float) hFood, 0xFF81C784, "Дневник питания"));
                segments.add(new DataUsagePieChartView.DataSegment((float) foodStats, 0xFF26A69A, "Статистика калорий"));
                segments.add(new DataUsagePieChartView.DataSegment((float) cFood, 0xFF4DB6AC, "Созданные блюда"));
                segments.add(new DataUsagePieChartView.DataSegment((float) pFood, 0xFF7986CB, "Пресеты приемов пищи"));
                segments.add(new DataUsagePieChartView.DataSegment((float) activityData, 0xFFBA68C8, "Данные активности"));
                segments.add(new DataUsagePieChartView.DataSegment((float) profileData, 0xFF90A4AE, "Профиль и цели"));
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    // ТЕПЕРЬ В ЦЕНТРЕ КОЛИЧЕСТВО ЗАПИСЕЙ
                    pieChart.setCenterText(totalRecordsFormatted);
                    pieChart.setData(segments);
                    populateLegend(segments, totalRecords);
                });
            }
        }).start();
    }

    private long getRowCount(SQLiteDatabase db, String tableName) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{tableName});
            if (cursor == null || !cursor.moveToFirst()) return 0;
            cursor.close();

            cursor = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null);
            if (cursor != null && cursor.moveToFirst()) return cursor.getLong(0);
        } catch (Exception e) {
            return 0;
        } finally {
            if (cursor != null) cursor.close();
        }
        return 0;
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
                int percent = (int) Math.round((segment.value * 100.0) / totalRecords);
                statsRight.setText(String.format("%d%% (%d)", percent, (int)segment.value));
            } else {
                statsRight.setText(segment.label.equals("Пусто") ? "" : "0% (0)");
            }

            container.addView(row);

            if (i < segments.size() - 1) {
                View divider = new View(getContext());
                int height = (int) (0.8f * getResources().getDisplayMetrics().density);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
                params.setMargins(0, (int)(6 * getResources().getDisplayMetrics().density), 0, (int)(6 * getResources().getDisplayMetrics().density));
                divider.setLayoutParams(params);
                divider.setBackgroundColor(Color.parseColor("#226E6E6E"));
                container.addView(divider);
            }
        }
    }

    private void confirmDeletion(String message, Runnable action) {
        new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Подтверждение")
                .setMessage(message)
                .setPositiveButton("Удалить", (d, w) -> action.run())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
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