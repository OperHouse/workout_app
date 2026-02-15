package com.example.workoutapp.Fragments.ProfileFragments;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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

import com.example.workoutapp.Data.EncryptionTools.DatabaseExporter;
import com.example.workoutapp.Data.NutritionDao.MealNameDao;
import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_PRESET_NAME_TABLE_DAO;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.R;
import com.example.workoutapp.RegistrationActivity.RegistrationActivity;
import com.example.workoutapp.Tools.OnNavigationVisibilityListener;
import com.example.workoutapp.Tools.PdfReportManager;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class DataManagementFragment extends Fragment {

    private DataUsagePieChartView pieChart;
    private OnNavigationVisibilityListener navigationListener;

    // DAO
    private MealNameDao mealNameDao;
    private WORKOUT_EXERCISE_TABLE_DAO workoutExerciseDao;
    private WORKOUT_PRESET_NAME_TABLE_DAO workoutPresetDao;

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

        view.findViewById(R.id.btn_export_data).setOnClickListener(v -> exportDatabaseToJson());
        view.findViewById(R.id.btn_import_data).setOnClickListener(v -> confirmImport());
        Button btnExportDb =  view.findViewById(R.id.btn_export_db);

        btnExportDb.setOnClickListener(v -> {
            // Вызываем экспорт
            File decryptedDb = DatabaseExporter.exportDecryptedDatabase(requireContext());

            if (decryptedDb != null && decryptedDb.exists()) {
                // Отправляем файл (используем ваш метод shareFile)
                shareFile(decryptedDb, "application/x-sqlite3");
            } else {
                Toast.makeText(getContext(), "Не удалось подготовить файл базы", Toast.LENGTH_SHORT).show();
            }
        });

        View btnPdf = view.findViewById(R.id.btn_export_pdf);
        if (btnPdf != null) {
            btnPdf.setOnClickListener(v -> showPdfSelectionDialog());
        }


    }

    private void confirmImport() {
        View dialogView = getLayoutInflater().inflate(R.layout.confirm_dialog_layout, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(dialogView).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));

        TextView titleTv = dialogView.findViewById(R.id.delete_title_D_TV);
        TextView messageTv = dialogView.findViewById(R.id.delete_message_D_TV);
        Button confirmBtn = dialogView.findViewById(R.id.delete_confirm_D_BTN);
        Button cancelBtn = dialogView.findViewById(R.id.delete_cancel_D_BTN);

        titleTv.setText("Импорт данных");
        messageTv.setText("Данные из файла будут добавлены в базу. Продолжить?");
        confirmBtn.setText("Выбрать файл");

        confirmBtn.setOnClickListener(v -> {
            importFileLauncher.launch("application/json"); // Запуск выбора файла
            dialog.dismiss();
        });
        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void importDataFromUri(Uri uri) {
        new Thread(() -> {
            try {
                InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
                String jsonString = scanner.hasNext() ? scanner.next() : "";
                inputStream.close();

                if (jsonString.isEmpty()) throw new Exception("Файл пуст");

                TypeToken<Map<String, Object>> typeToken = new TypeToken<Map<String, Object>>(){};
                Map<String, Object> root = new Gson().fromJson(jsonString, typeToken.getType());
                Map<String, List<Map<String, Object>>> dataMap = (Map<String, List<Map<String, Object>>>) root.get("data");

                SQLiteDatabase db = AppDataBase.getInstance(requireContext()).getWritableDatabase("");

                db.beginTransaction();
                try {
                    for (String tableName : dataMap.keySet()) {
                        List<Map<String, Object>> rows = dataMap.get(tableName);
                        if (rows == null) continue;
                        for (Map<String, Object> row : rows) {
                            ContentValues values = new ContentValues();
                            for (String key : row.keySet()) {
                                Object val = row.get(key);
                                if (val instanceof Double) values.put(key, (Double) val);
                                else if (val instanceof Long) values.put(key, (Long) val);
                                else values.put(key, val != null ? val.toString() : null);
                            }
                            db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (isAdded()) requireActivity().runOnUiThread(() -> {
                    updatePieChart();
                    Toast.makeText(getContext(), "Импорт завершен успешно!", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                if (isAdded()) requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void exportDatabaseToJson() {
        new Thread(() -> {
            try {
                // ВАЖНО: Используем ваш AppDataBase и передаем пароль (пустая строка если нет пароля)
                AppDataBase dbHelper = AppDataBase.getInstance(requireContext());
                // Если вы используете пароль, впишите его вместо ""
                net.sqlcipher.database.SQLiteDatabase db = dbHelper.getReadableDatabase("");

                Map<String, Object> fullExport = new HashMap<>();
                fullExport.put("export_date", System.currentTimeMillis());
                fullExport.put("app_version", 3); // Ваша DB_VERSION

                Map<String, Object> dataMap = new HashMap<>();

                // Список всех ваших таблиц из AppDataBase
                String[] tables = {
                        AppDataBase.BASE_EXERCISE_TABLE, AppDataBase.WORKOUT_PRESET_NAME_TABLE,
                        AppDataBase.CONNECTING_WORKOUT_PRESET_TABLE, AppDataBase.WORKOUT_EXERCISE_TABLE,
                        AppDataBase.STRENGTH_SET_DETAILS_TABLE, AppDataBase.CARDIO_SET_DETAILS_TABLE,
                        AppDataBase.BASE_FOOD_TABLE, AppDataBase.PRESET_FOOD_TABLE,
                        AppDataBase.MEAL_PRESET_NAME_TABLE, AppDataBase.CONNECTING_MEAL_PRESET_TABLE,
                        AppDataBase.MEAL_NAME_TABLE, AppDataBase.MEAL_FOOD_TABLE,
                        AppDataBase.CONNECTING_MEAL_TABLE, AppDataBase.USER_PROFILE_TABLE,
                        AppDataBase.WEIGHT_HISTORY_TABLE, AppDataBase.DAILY_ACTIVITY_TRACKING_TABLE,
                        AppDataBase.GENERAL_GOAL_TABLE, AppDataBase.ACTIVITY_GOAL_TABLE,
                        AppDataBase.FOOD_GAIN_GOAL_TABLE, AppDataBase.DAILY_FOOD_TRACKING_TABLE
                };

                for (String tableName : tables) {
                    dataMap.put(tableName, getAllRowsFromTable(db, tableName));
                }

                fullExport.put("data", dataMap);

                String jsonString = new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(fullExport);
                saveAndShareFile(jsonString);

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void saveAndShareFile(String jsonString) {
        try {
            // 1. Создаем файл во временном кэше приложения [cite: 1]
            File cachePath = new File(requireContext().getCacheDir(), "exports");
            cachePath.mkdirs(); // Создаем папку, если её нет
            File tempFile = new File(cachePath, "workout_data_backup.json");

            // 2. Записываем JSON в файл
            FileOutputStream stream = new FileOutputStream(tempFile);
            stream.write(jsonString.getBytes());
            stream.close();

            // 3. Получаем безопасный URI через FileProvider
            Uri contentUri = androidx.core.content.FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    tempFile);

            if (contentUri != null) {
                // 4. Создаем Intent для отправки файла [cite: 1]
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Разрешаем чтение файла [cite: 1]
                shareIntent.setDataAndType(contentUri, "application/json");
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);

                // 5. Показываем системное окно выбора приложения
                startActivity(Intent.createChooser(shareIntent, "Выгрузить данные в..."));
            }

        } catch (IOException e) {
            e.printStackTrace();
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), "Ошибка при создании файла", Toast.LENGTH_SHORT).show());
        }
    }

    private List<Map<String, Object>> getAllRowsFromTable(net.sqlcipher.database.SQLiteDatabase db, String tableName) {
        List<Map<String, Object>> rows = new ArrayList<>();
        net.sqlcipher.Cursor cursor = db.rawQuery("SELECT * FROM " + tableName, null);

        if (cursor.moveToFirst()) {
            do {
                Map<String, Object> row = new HashMap<>();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    int type = cursor.getType(i);
                    String colName = cursor.getColumnName(i);

                    switch (type) {
                        case net.sqlcipher.Cursor.FIELD_TYPE_INTEGER:
                            row.put(colName, cursor.getLong(i));
                            break;
                        case net.sqlcipher.Cursor.FIELD_TYPE_FLOAT:
                            row.put(colName, cursor.getDouble(i));
                            break;
                        default:
                            row.put(colName, cursor.getString(i));
                            break;
                    }
                }
                rows.add(row);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return rows;
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
    private void exportFullDatabaseFile() {
        new Thread(() -> {
            try {
                File dbFile = requireContext().getDatabasePath("WorkoutApp.db");
                File cachePath = new File(requireContext().getCacheDir(), "exports");
                cachePath.mkdirs();
                File backupFile = new File(cachePath, "workout_backup.db");

                try (FileInputStream in = new FileInputStream(dbFile);
                     FileOutputStream out = new FileOutputStream(backupFile)) {
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
                }

                requireActivity().runOnUiThread(() -> shareFile(backupFile, "application/x-sqlite3"));
            } catch (IOException e) {
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Ошибка бэкапа", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // ===================== ЭКСПОРТ PDF =====================
    private void showPdfSelectionDialog() {
        // 1. Понятные пользователю названия
        String[] items = {"История тренировок", "История питания", "История активности"};
        // Все галочки по умолчанию включены
        boolean[] checkedItems = {true, true, true};

        new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Данные для PDF отчета")
                .setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton("Создать", (dialog, id) -> {
                    ArrayList<String> selected = new ArrayList<>();

                    // 2. Сопоставляем текст с ключами, которые ждет PdfReportManager
                    if (checkedItems[0]) selected.add("workouts");
                    if (checkedItems[1]) selected.add("nutrition");
                    if (checkedItems[2]) selected.add("activity");

                    if (selected.isEmpty()) {
                        Toast.makeText(getContext(), "Выберите хотя бы один пункт", Toast.LENGTH_SHORT).show();
                    } else {
                        generatePdfReport(selected);
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void generatePdfReport(List<String> categories) {
        // Создаем экземпляр менеджера
        PdfReportManager reportManager = new PdfReportManager(requireContext(), MainActivity.getAppDataBase());

        // Генерируем файл
        File pdfFile = reportManager.createReport(categories);

        // Отправляем через ваш универсальный метод
        if (pdfFile != null) {
            shareFile(pdfFile, "application/pdf");
        } else {
            Toast.makeText(getContext(), "Не удалось создать PDF", Toast.LENGTH_SHORT).show();
        }
    }

    // ===================== УНИВЕРСАЛЬНЫЙ ШЕРИНГ =====================
    private void shareFile(File file, String mimeType) {
        Uri contentUri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".fileprovider",
                file);

        if (contentUri != null) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_STREAM, contentUri);
            startActivity(Intent.createChooser(intent, "Поделиться через..."));
        }
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