package com.example.workoutapp.Fragments.ProfileFragments.Divide;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.workoutapp.Data.NutritionDao.MealNameDao;
import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_PRESET_NAME_TABLE_DAO;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.R;
import com.example.workoutapp.RegistrationActivity.RegistrationActivity;
import com.example.workoutapp.Tools.DataManagementTools.DataExportService;
import com.example.workoutapp.Tools.DataManagementTools.DataImportService;
import com.example.workoutapp.Tools.DataManagementTools.DataUsagePieChartView;
import com.example.workoutapp.Tools.DataManagementTools.FileStorageManager;
import com.example.workoutapp.Tools.DataManagementTools.PdfReportManager;
import com.example.workoutapp.Tools.EncryptionTools.DatabaseExporter;
import com.example.workoutapp.Tools.OnNavigationVisibilityListener;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DataManagementFragment extends Fragment {

    private DataUsagePieChartView pieChart;
    private OnNavigationVisibilityListener navigationListener;

    // DAO
    private MealNameDao mealNameDao;
    private WORKOUT_EXERCISE_TABLE_DAO workoutExerciseDao;
    private WORKOUT_PRESET_NAME_TABLE_DAO workoutPresetDao;
    private static final int PICK_FILE_CODE = 1001;

    // Выносим установку слушателя в отдельный метод для удобного переподключения
    private boolean isUpdatingProgrammatically = false;

    private final ActivityResultLauncher<String> importFileLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    importDataFromUri(uri);
                }
            });


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_data_management, container, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Проверяем, что это наш запрос и пользователь успешно выбрал файл
        if (requestCode == PICK_FILE_CODE && resultCode == android.app.Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri selectedFileUri = data.getData();

                // Вызываем твой метод импорта, который мы до этого доработали
                importDataFromUri(selectedFileUri);
            }
        }
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
        // Кнопка назад
        view.findViewById(R.id.data_back_btn).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Инициализация групп логики
        initWorkoutButtons(view);
        initNutritionButtons(view);
        initProfileButtons(view);
        initTransferButtons(view); // Импорт, экспорт, PDF, DB

        // СИНХРОНИЗАЦИЯ (Твой новый свитч)
        com.google.android.material.switchmaterial.SwitchMaterial syncSwitch = view.findViewById(R.id.sync_switch);
        if (syncSwitch != null) {
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

            isUpdatingProgrammatically = true;
            syncSwitch.setChecked(prefs.getBoolean("sync_enabled", false));
            isUpdatingProgrammatically = false;

            initSyncLogic(syncSwitch, prefs);
        }

        // ПОЛНОЕ УДАЛЕНИЕ (Оставляем тут, так как это критическая кнопка)
        view.findViewById(R.id.btn_delete_everything).setOnClickListener(v ->
                confirmDeletion("ВНИМАНИЕ! Это действие сотрет ВСЕ данные безвозвратно. Продолжить?", this::deleteAllData));
    }

    private void initWorkoutButtons(View view) {
        view.findViewById(R.id.btn_del_workout_history).setOnClickListener(v ->
                confirmDeletion("Очистить историю тренировок и все сеты?", () -> clearCategoryData(1)));
        view.findViewById(R.id.btn_del_exercises).setOnClickListener(v ->
                confirmDeletion("Удалить все созданные упражнения?", () -> clearCategoryData(2)));
        view.findViewById(R.id.btn_del_workout_presets).setOnClickListener(v ->
                confirmDeletion("Удалить все шаблоны тренировок?", () -> clearCategoryData(3)));
    }

    private void initNutritionButtons(View view) {
        view.findViewById(R.id.btn_del_food_history).setOnClickListener(v ->
                confirmDeletion("Удалить все записи о приемах пищи?", () -> clearCategoryData(4)));

        View btnDelStats = view.findViewById(R.id.btn_del_food_stats);
        if (btnDelStats != null) {
            btnDelStats.setOnClickListener(v ->
                    confirmDeletion("Очистить ежедневную статистику калорий (графики)?", () -> clearCategoryData(12)));
        }

        view.findViewById(R.id.btn_del_custom_dishes).setOnClickListener(v ->
                confirmDeletion("Удалить все блюда из вашей библиотеки?", () -> clearCategoryData(5)));
        view.findViewById(R.id.btn_del_meal_presets).setOnClickListener(v ->
                confirmDeletion("Удалить все шаблоны приемов пищи?", () -> clearCategoryData(6)));
    }

    private void initProfileButtons(View view) {
        view.findViewById(R.id.btn_del_activity).setOnClickListener(v ->
                confirmDeletion("Удалить историю шагов и активности?", () -> clearCategoryData(7)));
        view.findViewById(R.id.btn_del_general_goals).setOnClickListener(v ->
                confirmDeletion("Сбросить общие цели?", () -> clearCategoryData(8)));
        view.findViewById(R.id.btn_del_activity_goals).setOnClickListener(v ->
                confirmDeletion("Сбросить цели по шагам и калориям?", () -> clearCategoryData(9)));
        view.findViewById(R.id.btn_del_food_goals).setOnClickListener(v ->
                confirmDeletion("Сбросить КБЖУ цели?", () -> clearCategoryData(10)));
        view.findViewById(R.id.btn_del_profile).setOnClickListener(v ->
                confirmDeletion("Удалить данные профиля и историю веса?", () -> clearCategoryData(11)));
    }

    private void initTransferButtons(View view) {
        // JSON Экспорт и Импорт
        view.findViewById(R.id.btn_export_data).setOnClickListener(v -> {
            // 1. Генерируем JSON строку
            DataExportService des = new DataExportService(MainActivity.getAppDataBase());
            String json = des.generateJsonExport();

            if (json != null && !json.isEmpty()) {
                // 2. Сохраняем её во временный файл
                File jsonFile = saveJsonToTempFile(json);

                // 3. Вызываем твой универсальный диалог
                // Передаем файл, mime-тип и префикс для сохранения
                showExportActionsDialog(jsonFile, "application/json", "Workout_Data");
            } else {
                Toast.makeText(getContext(), "Данные для экспорта отсутствуют", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.btn_import_data).setOnClickListener(v -> openFilePicker());

        // Экспорт БД
        Button btnExportDb = view.findViewById(R.id.btn_export_db);
        if (btnExportDb != null) {
            btnExportDb.setOnClickListener(v -> {
                File decryptedDb = DatabaseExporter.exportDecryptedDatabase(requireContext());
                if (decryptedDb != null && decryptedDb.exists()) {
                    showExportActionsDialog(decryptedDb, "application/x-sqlite3", "Workout_Backup");
                } else {
                    Toast.makeText(getContext(), "Не удалось подготовить файл базы", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // PDF Экспорт
        View btnPdf = view.findViewById(R.id.btn_export_pdf);
        if (btnPdf != null) {
            btnPdf.setOnClickListener(v -> showPdfSelectionDialog());
        }
    }

    // Вынес логику удаления в отдельный поток, чтобы не засорять UI метод
    private void deleteAllData() {
        new Thread(() -> {
            SQLiteDatabase db = MainActivity.getAppDataBase();
            for (int i = 1; i <= 12; i++) {
                clearCategoryDataInternal(db, i);
            }
            db.execSQL("VACUUM");
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    updatePieChart();
                    Toast.makeText(getContext(), "Все данные стерты", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void importDataFromUri(Uri uri) {
        new Thread(() -> {
            try {
                Context context = requireContext();
                String fileName = getFileName(uri); // Вспомогательный метод ниже

                DataImportService importService = new DataImportService(MainActivity.getAppDataBase());

                if (fileName != null && fileName.endsWith(".db")) {
                    // --- ЛОГИКА ДЛЯ .DB ФАЙЛА ---

                    // 1. Создаем временный файл в кэше приложения
                    File tempFile = new File(context.getCacheDir(), "import_temp.db");

                    // 2. Копируем данные из Uri в этот файл
                    try (InputStream is = context.getContentResolver().openInputStream(uri);
                         FileOutputStream os = new FileOutputStream(tempFile)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = is.read(buffer)) > 0) {
                            os.write(buffer, 0, length);
                        }
                    }

                    // 3. Вызываем метод импорта из файла
                    importService.importDataFromDbFile(tempFile, "");

                    // 4. Удаляем временный файл
                    tempFile.delete();

                } else {
                    // --- ЛОГИКА ДЛЯ JSON ---
                    InputStream inputStream = context.getContentResolver().openInputStream(uri);
                    Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
                    String jsonString = scanner.hasNext() ? scanner.next() : "";
                    inputStream.close();

                    importService.importDataFromJson(jsonString);
                }

                // Обновляем UI
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        updatePieChart();
                        Toast.makeText(getContext(), "Данные успешно восстановлены!", Toast.LENGTH_SHORT).show();
                    });
                }

            } catch (Exception e) {
                Log.e("DATA_IMPORT", "Ошибка импорта: ", e);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }
        }).start();
    }

    // Вспомогательный метод для получения имени файла из Uri
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            // Явно указываем android.database.Cursor вместо просто Cursor
            try (android.database.Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    // Используем константу DISPLAY_NAME напрямую из провайдера
                    int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (index != -1) result = cursor.getString(index);
                }
            } catch (Exception e) {
                Log.e("GET_FILE_NAME", "Ошибка при получении имени файла", e);
            }
        }

        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result != null ? result.toLowerCase() : "unknown.db";
    }

    private void initSyncLogic(com.google.android.material.switchmaterial.SwitchMaterial syncSwitch, android.content.SharedPreferences prefs) {
        syncSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingProgrammatically) return; // Игнорируем, если меняем код, а не пользователь

            if (isChecked) {
                boolean hasAccount = false; // Твоя заглушка

                if (!hasAccount) {
                    // Если аккаунта нет, выключаем обратно
                    isUpdatingProgrammatically = true;
                    syncSwitch.setChecked(false);
                    isUpdatingProgrammatically = false;

                    // Показываем твой красивый кастомный диалог
                    showAuthDialog();
                } else {
                    prefs.edit().putBoolean("sync_enabled", true).apply();
                }
            } else {
                prefs.edit().putBoolean("sync_enabled", false).apply();
            }
        });
    }

    private void showAuthDialog() {
        // Инфлейтим твой макет
        View dialogView = getLayoutInflater().inflate(R.layout.confirm_dialog_layout, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(dialogView).create();

        // Прозрачный фон для закругленных углов
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        // Инициализация элементов твоего макета
        TextView titleTv = dialogView.findViewById(R.id.delete_title_D_TV);
        TextView messageTv = dialogView.findViewById(R.id.delete_message_D_TV);
        Button loginBtn = dialogView.findViewById(R.id.delete_confirm_D_BTN);
        Button cancelBtn = dialogView.findViewById(R.id.delete_cancel_D_BTN);

        // Настраиваем текст под ситуацию
        titleTv.setText("Нужен аккаунт");
        messageTv.setText("Чтобы синхронизировать данные с облаком, пожалуйста, войдите в систему или создайте новый аккаунт.");
        loginBtn.setText("Войти"); // Заменяем "Да" на "Войти"
        cancelBtn.setText("Позже"); // Заменяем "Нет" на "Позже"

        loginBtn.setOnClickListener(v -> {
            // Тут будет переход на экран логина
            Intent intent = new Intent(requireContext(), RegistrationActivity.class);
            startActivity(intent);
            Toast.makeText(getContext(), "Переход к регистрации...", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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
                    db.delete(AppDataBase.PRESET_FOOD_TABLE, null, null);
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

            // Инициализация UI компонентов
            androidx.cardview.widget.CardView colorCircle = row.findViewById(R.id.legend_color_card);
            TextView nameText = row.findViewById(R.id.legend_name_stats);
            TextView statsRight = row.findViewById(R.id.legend_size_right);

            // Установка данных
            colorCircle.setCardBackgroundColor(segment.color);
            nameText.setText(segment.label);

            if (totalRecords > 0 && !segment.label.equals("Пусто")) {
                int percent = (int) Math.round((segment.value * 100.0) / totalRecords);
                statsRight.setText(String.format("%d%% (%d)", percent, (int)segment.value));

                // Безопасная установка фона
                android.util.TypedValue outValue = new android.util.TypedValue();
                if (getContext() != null) {
                    getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                    row.setBackgroundResource(outValue.resourceId);
                }

                row.setOnClickListener(v -> handleNavigation(segment.label));
            }

            container.addView(row);

            // Отрисовка разделителя
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


    /**
     * Логика переходов при нажатии на элемент легенды
     */
    private void handleNavigation(String label) {
        androidx.fragment.app.Fragment destination = null;

        switch (label) {
            case "История тренировок":
                // Замени на свои реальные классы фрагментов
                // destination = new TrainingHistoryFragment();
                break;
            case "Упражнения":
                // destination = new AllExercisesFragment();
                break;
            case "Пресеты тренировок":
                // destination = new WorkoutPresetsFragment();
                break;
            case "Дневник питания":
                // destination = new NutritionDiaryFragment();
                break;
            case "Статистика калорий":
                // destination = new CalorieStatsFragment();
                break;
            case "Созданные блюда":
                // destination = new CustomFoodsFragment();
                break;
            case "Пресеты приемов пищи":
                // destination = new MealPresetsFragment();
                break;
            case "Данные активности":
                // destination = new ActivityDataFragment();
                break;
            case "Профиль и цели":
                getParentFragmentManager().popBackStack();
                return;
        }

        if (destination != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, destination)
                    .addToBackStack(null)
                    .commit();
        } else {
            // Если фрагмент еще не создан, можно вывести уведомление
            Toast.makeText(getContext(), "Раздел '" + label + "' находится в разработке :)", Toast.LENGTH_SHORT).show();
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

    // ===================== ЭКСПОРТ PDF =====================
    private void showPdfSelectionDialog() {
        String[] items = {"История тренировок", "История питания", "История активности"};
        boolean[] checkedItems = {true, true, true};

        new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Данные для PDF отчета")
                .setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton("Далее", (dialog, id) -> {
                    ArrayList<String> selected = new ArrayList<>();
                    if (checkedItems[0]) selected.add("workouts");
                    if (checkedItems[1]) selected.add("nutrition");
                    if (checkedItems[2]) selected.add("activity");

                    if (selected.isEmpty()) {
                        Toast.makeText(getContext(), "Выберите хотя бы один пункт", Toast.LENGTH_SHORT).show();
                    } else {
                        // Переходим к генерации и выбору места сохранения
                        processPdfExport(selected);
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
    private void processPdfExport(ArrayList<String> selectedCategories) {
        // 1. Генерируем PDF файл через ваш менеджер
        PdfReportManager pdfManager = new PdfReportManager(requireContext(), MainActivity.getAppDataBase());
        File pdfFile = pdfManager.createReport(selectedCategories);

        if (pdfFile == null || !pdfFile.exists()) {
            Toast.makeText(getContext(), "Ошибка при создании PDF", Toast.LENGTH_SHORT).show();
            return;
        }

        showExportActionsDialog(pdfFile, "application/pdf", "Workout_Report");
    }

    private void showExportActionsDialog(File file, String mimeType, String defaultPrefix) {
        if (file == null || !file.exists()) {
            Toast.makeText(getContext(), "Файл не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        requireActivity().runOnUiThread(() -> {
            new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Dialog_Alert)
                    .setTitle("Файл готов")
                    .setMessage("Выберите действие для " + file.getName() + ":")
                    .setNeutralButton("Сохранить", (dialog, which) -> {
                        // Генерируем имя: Префикс + Таймстемп + расширение из исходного файла
                        String extension = file.getName().substring(file.getName().lastIndexOf("."));
                        String fileName = defaultPrefix + "_" + System.currentTimeMillis() + extension;

                        FileStorageManager.saveFileToDownloads(requireContext(), file, fileName);
                    })
                    .setNegativeButton("Поделиться", (dialog, which) -> {
                        FileStorageManager.shareFile(requireActivity(), file, mimeType);
                    })
                    .setPositiveButton("Отмена", null)
                    .show();
        });
    }

    private File saveJsonToTempFile(String json) {
        try {
            File tempFile = new File(requireContext().getCacheDir(), "Workout_Data.json");
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(json);
            }
            return tempFile;
        } catch (IOException e) {
            Log.e("EXPORT", "Ошибка записи JSON во временный файл", e);
            return null;
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Ставим общий тип, чтобы фильтр по EXTRA_MIME_TYPES сработал корректно
        intent.setType("*/*");

        // Указываем конкретные типы, которые мы хотим видеть
        String[] mimeTypes = {
                "application/json",        // Для JSON
                "application/x-sqlite3",   // Официальный тип SQLite
                "application/octet-stream" // Часто файлы .db помечаются так
        };

        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        // Запускаем (используй свой способ получения результата)
        startActivityForResult(Intent.createChooser(intent, "Выберите файл бэкапа"), PICK_FILE_CODE);
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