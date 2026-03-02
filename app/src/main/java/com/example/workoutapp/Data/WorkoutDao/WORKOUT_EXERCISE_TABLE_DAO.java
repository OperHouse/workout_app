package com.example.workoutapp.Data.WorkoutDao;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.Helpers.WorkoutSessionModel;
import com.example.workoutapp.Models.WorkoutModels.CardioSetModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.Models.WorkoutModels.StrengthSetModel;

import net.sqlcipher.database.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WORKOUT_EXERCISE_TABLE_DAO {

    private final SQLiteDatabase db;
    private final STRENGTH_SET_DETAILS_TABLE_DAO strengthSetDao;
    private final CARDIO_SET_DETAILS_TABLE_DAO cardioSetDao;

    public WORKOUT_EXERCISE_TABLE_DAO(SQLiteDatabase db) {
        this.db = db;
        this.strengthSetDao = new STRENGTH_SET_DETAILS_TABLE_DAO(db);
        this.cardioSetDao = new CARDIO_SET_DETAILS_TABLE_DAO(db);
    }


    // =========================
    // Добавление упражнения (Локальное)
    // =========================
    public void addExercise(String name, String type, String bodyType, String uid) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.WORKOUT_EXERCISE_NAME, name);
        values.put(AppDataBase.WORKOUT_EXERCISE_TYPE, type);
        values.put(AppDataBase.WORKOUT_EXERCISE_BODY_TYPE, bodyType);
        values.put(AppDataBase.WORKOUT_EXERCISE_UID, uid);
        values.put(AppDataBase.WORKOUT_EXERCISE_DATE, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        values.put(AppDataBase.WORKOUT_EXERCISE_STATE, "unfinished");

        // Устанавливаем текущее время при создании
        values.put(AppDataBase.WORKOUT_EXERCISE_LAST_MODIFIED, System.currentTimeMillis());

        db.insert(AppDataBase.WORKOUT_EXERCISE_TABLE, null, values);
    }

    // =========================
    // Маппинг из курсора (теперь с LAST_MODIFIED)
    // =========================
    private ExerciseModel mapCursorToExercise(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_NAME));
        String type = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_TYPE));
        String bodyType = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_BODY_TYPE));
        String date = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_DATE));
        String state = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_STATE));
        String uid = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_UID));

        // Читаем метку времени из базы
        long lastModified = cursor.getLong(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_LAST_MODIFIED));

        List<Object> sets = new ArrayList<>();
        if ("Кардио".equalsIgnoreCase(type) || "Время".equalsIgnoreCase(type)) {
            sets.addAll(cardioSetDao.getSetsForExercise(id));
        } else {
            sets.addAll(strengthSetDao.getSetsForExercise(id));
        }

        ExerciseModel model = new ExerciseModel(id, name, type, bodyType, date, state, sets);
        model.setExercise_uid(uid);
        model.setExercise_time_lastModified(lastModified); // Убедись, что добавил это поле и сеттер в ExerciseModel
        return model;
    }

    // =========================
    // Обновление только метки времени (вызывай это в DAO подходов)
    // =========================
    public void updateLastModified(long exerciseId) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.WORKOUT_EXERCISE_LAST_MODIFIED, System.currentTimeMillis());
        db.update(AppDataBase.WORKOUT_EXERCISE_TABLE, values,
                AppDataBase.WORKOUT_EXERCISE_ID + " = ?",
                new String[]{String.valueOf(exerciseId)});
    }

    // =========================
    // Сохранение из облака (с учетом времени)
    // =========================
    public void addFullExerciseFromCloud(ExerciseModel ex) {
        if (ex == null || isExerciseUidExists(ex.getExercise_uid())) return;

        ContentValues values = new ContentValues();
        values.put(AppDataBase.WORKOUT_EXERCISE_NAME, ex.getExerciseName());
        values.put(AppDataBase.WORKOUT_EXERCISE_TYPE, ex.getExerciseType());
        values.put(AppDataBase.WORKOUT_EXERCISE_BODY_TYPE, ex.getExerciseBodyType());
        values.put(AppDataBase.WORKOUT_EXERCISE_DATE, ex.getEx_Data());
        values.put(AppDataBase.WORKOUT_EXERCISE_STATE, ex.getState());
        values.put(AppDataBase.WORKOUT_EXERCISE_UID, ex.getExercise_uid());

        // Сохраняем время, которое пришло из облака
        values.put(AppDataBase.WORKOUT_EXERCISE_LAST_MODIFIED, ex.getExercise_time_lastModified());

        long newExId = db.insert(AppDataBase.WORKOUT_EXERCISE_TABLE, null, values);

        if (ex.getSets() != null && newExId != -1) {
            for (Object setObj : ex.getSets()) {
                saveSet(newExId, setObj, ex.getExerciseType());
                Log.d("SyncDebug", "Обработка упражнения из облака: " + ex.getExerciseName() + " UID: " + ex.getExercise_uid());
            }
        }
    }

    public void updateFullExerciseFromCloud(ExerciseModel ex) {
        if (ex == null || ex.getExercise_uid() == null) return;

        Cursor cursor = db.query(AppDataBase.WORKOUT_EXERCISE_TABLE,
                new String[]{AppDataBase.WORKOUT_EXERCISE_ID},
                AppDataBase.WORKOUT_EXERCISE_UID + " = ?",
                new String[]{ex.getExercise_uid()}, null, null, null);

        if (cursor == null || !cursor.moveToFirst()) {
            if (cursor != null) cursor.close();
            return;
        }
        long localId = cursor.getLong(0);
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(AppDataBase.WORKOUT_EXERCISE_NAME, ex.getExerciseName());
        values.put(AppDataBase.WORKOUT_EXERCISE_TYPE, ex.getExerciseType());
        values.put(AppDataBase.WORKOUT_EXERCISE_BODY_TYPE, ex.getExerciseBodyType());
        values.put(AppDataBase.WORKOUT_EXERCISE_DATE, ex.getEx_Data());
        values.put(AppDataBase.WORKOUT_EXERCISE_STATE, ex.getState());

        // При обновлении из облака записываем облачное время
        values.put(AppDataBase.WORKOUT_EXERCISE_LAST_MODIFIED, ex.getExercise_time_lastModified());

        db.update(AppDataBase.WORKOUT_EXERCISE_TABLE, values,
                AppDataBase.WORKOUT_EXERCISE_UID + " = ?",
                new String[]{ex.getExercise_uid()});

        strengthSetDao.deleteSetsForExercise(localId);
        cardioSetDao.deleteSetsForExercise(localId);

        if (ex.getSets() != null) {
            for (Object setObj : ex.getSets()) {
                saveSet(localId, setObj, ex.getExerciseType());
            }
        }
    }



    public List<ExerciseModel> getExByState(String state) {
        List<ExerciseModel> exerciseList = new ArrayList<>();
        Cursor cursor = null;

        try {
            // Выбираем все колонки, включая UID
            cursor = db.query(
                    AppDataBase.WORKOUT_EXERCISE_TABLE, // Твоя константа таблицы
                    null,                         // null вернет все колонки
                    AppDataBase.WORKOUT_EXERCISE_STATE + " = ?",
                    new String[]{state},
                    null, null, null
            );

            while (cursor.moveToNext()) {
                // Используем вспомогательный метод mapCursorToExercise, который мы создали ранее
                exerciseList.add(mapCursorToExercise(cursor));
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return exerciseList;
    }

    // =========================
    // Проверка существования по UID
    // =========================
    public boolean isExerciseUidExists(String uid) {
        if (uid == null || uid.isEmpty()) return false;
        Cursor cursor = db.query(AppDataBase.WORKOUT_EXERCISE_TABLE,
                new String[]{AppDataBase.WORKOUT_EXERCISE_ID},
                AppDataBase.WORKOUT_EXERCISE_UID + " = ?",
                new String[]{uid}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // =========================
    // Получение всех данных для синхронизации (С UID)
    // =========================
    public List<ExerciseModel> getAllExercisesForSync() {
        List<ExerciseModel> exerciseList = new ArrayList<>();
        Cursor cursor = db.query(AppDataBase.WORKOUT_EXERCISE_TABLE, null, null, null, null, null, null);

        try {
            while (cursor.moveToNext()) {
                exerciseList.add(mapCursorToExercise(cursor));
            }
        } finally {
            cursor.close();
        }
        return exerciseList;
    }


    private void saveSet(long exerciseId, Object setObj, String type) {
        if (setObj instanceof java.util.Map) {
            java.util.Map<String, Object> map = (java.util.Map<String, Object>) setObj;
            String setType = (String) map.get("type");
            if ("strength".equals(setType)) {
                strengthSetDao.addStrengthSet(exerciseId, getDouble(map.get("weight")), getInt(map.get("rep")), getInt(map.get("order")), (String) map.get("state"));
            } else if ("cardio".equals(setType)) {
                cardioSetDao.addCardioSet(exerciseId, getDouble(map.get("temp")), getInt(map.get("time")), getDouble(map.get("distance")), getInt(map.get("order")), (String) map.get("state"));
            }
        } else if (setObj instanceof StrengthSetModel) {
            StrengthSetModel s = (StrengthSetModel) setObj;
            strengthSetDao.addStrengthSet(exerciseId, s.getStrength_set_weight(), s.getStrength_set_rep(), s.getStrength_set_order(), s.getStrength_set_state());
        } else if (setObj instanceof CardioSetModel) {
            CardioSetModel c = (CardioSetModel) setObj;
            cardioSetDao.addCardioSet(exerciseId, c.getCardio_set_temp(), c.getCardio_set_time(), c.getCardio_set_distance(), c.getCardio_set_order(), c.getCardio_set_state());
        }
    }

    // ОСТАЛЬНЫЕ МЕТОДЫ (getExByState, deleteExercise, и т.д.)
    // Нужно обновить, вызывая внутри них mapCursorToExercise(cursor), чтобы UID не терялся.

    public void deleteExercisesByDate(String date) {
        if (date == null || date.isEmpty()) return;
        db.delete(AppDataBase.WORKOUT_EXERCISE_TABLE, AppDataBase.WORKOUT_EXERCISE_DATE + " = ?", new String[]{date});
    }

    public void deleteExercise(ExerciseModel exercise) {
        if (exercise == null) return;

        long exerciseId = exercise.getExercise_id();

        // 1. Сначала удаляем все связанные сеты (подходы) из других таблиц
        // Это важно, чтобы не оставлять "мусор" в базе
        if (strengthSetDao != null) {
            strengthSetDao.deleteSetsForExercise(exerciseId);
        }
        if (cardioSetDao != null) {
            cardioSetDao.deleteSetsForExercise(exerciseId);
        }

        // 2. Теперь удаляем само упражнение по его локальному ID
        db.delete(
                AppDataBase.WORKOUT_EXERCISE_TABLE,
                AppDataBase.WORKOUT_EXERCISE_ID + " = ?",
                new String[]{String.valueOf(exerciseId)}
        );

        Log.d("DAO", "Упражнение удалено: " + exercise.getExerciseName() + " (ID: " + exerciseId + ")");
    }
    public void deleteAllWorkouts() {
        try {
            // 1. Удаляем все подходы из связанных таблиц
            db.delete(AppDataBase.STRENGTH_SET_DETAILS_TABLE, null, null);
            db.delete(AppDataBase.CARDIO_SET_DETAILS_TABLE, null, null);

            // 2. Удаляем все упражнения
            db.delete(AppDataBase.WORKOUT_EXERCISE_TABLE, null, null);

            // 3. Сбрасываем счетчик ID (sqlite_sequence), чтобы новые ID снова начинались с 1
            db.delete("sqlite_sequence", "name = ?", new String[]{AppDataBase.WORKOUT_EXERCISE_TABLE});

            Log.d("DAO", "Вся история тренировок полностью очищена локально");
        } catch (Exception e) {
            Log.e("DAO", "Ошибка при полной очистке тренировок: " + e.getMessage());
        }
    }

    /**
     * Возвращает дату последней завершенной тренировки.
     * @return Дата в формате String или null, если тренировок нет.
     */
    public String getLatestWorkoutDate() {
        Cursor cursor = null;
        try {
            // Ищем максимальную дату среди упражнений со статусом 'finished'
            String query = "SELECT MAX(" + AppDataBase.WORKOUT_EXERCISE_DATE + ")" +
                    " FROM " + AppDataBase.WORKOUT_EXERCISE_TABLE +
                    " WHERE " + AppDataBase.WORKOUT_EXERCISE_STATE + " = ?";

            cursor = db.rawQuery(query, new String[]{"finished"});

            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0); // Возвращает дату или null
            }
        } catch (Exception e) {
            Log.e("DAO", "Ошибка при получении даты последней тренировки: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }
    /**
     * Помечает упражнение как завершенное.
     * @param exerciseId Локальный ID упражнения из SQLite.
     */
    public void markExerciseAsFinished(long exerciseId) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.WORKOUT_EXERCISE_STATE, "finished");

        try {
            int rowsUpdated = db.update(
                    AppDataBase.WORKOUT_EXERCISE_TABLE,
                    values,
                    AppDataBase.WORKOUT_EXERCISE_ID + " = ?",
                    new String[]{String.valueOf(exerciseId)}
            );

            if (rowsUpdated > 0) {
                Log.d("DAO", "Упражнение ID " + exerciseId + " успешно завершено");
            }
        } catch (Exception e) {
            Log.e("DAO", "Ошибка при завершении упражнения: " + e.getMessage());
        }
    }


    /**
     * Получает историю тренировок, сгруппированную по датам.
     * @param limit Количество дней (сессий) для загрузки.
     * @param offset Смещение (для пагинации).
     * @return Список моделей сессий.
     */
    public List<WorkoutSessionModel> getWorkoutHistory(int limit, int offset) {
        List<WorkoutSessionModel> history = new ArrayList<>();

        // 1. Получаем список уникальных дат, когда были завершенные упражнения
        String dateQuery = "SELECT DISTINCT " + AppDataBase.WORKOUT_EXERCISE_DATE +
                " FROM " + AppDataBase.WORKOUT_EXERCISE_TABLE +
                " WHERE " + AppDataBase.WORKOUT_EXERCISE_STATE + " = 'finished'" +
                " ORDER BY " + AppDataBase.WORKOUT_EXERCISE_DATE + " DESC" +
                " LIMIT ? OFFSET ?";

        Cursor dateCursor = db.rawQuery(dateQuery, new String[]{String.valueOf(limit), String.valueOf(offset)});

        try {
            if (dateCursor != null) {
                while (dateCursor.moveToNext()) {
                    String date = dateCursor.getString(0);

                    // 2. Для каждой найденной даты вытягиваем список упражнений
                    // Мы используем уже созданный метод getExercisesByDate
                    List<ExerciseModel> exercisesForDate = getExercisesByDate(date);

                    if (!exercisesForDate.isEmpty()) {
                        history.add(new WorkoutSessionModel(date, exercisesForDate));
                    }
                }
            }
        } catch (Exception e) {
            Log.e("DAO", "Ошибка при получении истории: " + e.getMessage());
        } finally {
            if (dateCursor != null) dateCursor.close();
        }

        return history;
    }

    /**
     * Вспомогательный метод для получения упражнений за конкретный день.
     */
    public List<ExerciseModel> getExercisesByDate(String date) {
        List<ExerciseModel> exerciseList = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = db.query(
                    AppDataBase.WORKOUT_EXERCISE_TABLE,
                    null,
                    AppDataBase.WORKOUT_EXERCISE_DATE + " = ? AND " + AppDataBase.WORKOUT_EXERCISE_STATE + " = ?",
                    new String[]{date, "finished"},
                    null, null, null
            );

            while (cursor.moveToNext()) {
                // Используем наш универсальный mapCursorToExercise, который уже умеет в UID
                exerciseList.add(mapCursorToExercise(cursor));
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return exerciseList;
    }

    /**
     * Получает историю тренировок за конкретную выбранную дату.
     * Используется для поиска или календаря.
     * @param date Дата в формате "yyyy-MM-dd"
     * @return Список сессий (WorkoutSessionModel)
     */
    public List<WorkoutSessionModel> getWorkoutHistoryByDate(String date) {
        List<WorkoutSessionModel> history = new ArrayList<>();

        // 1. Используем уже существующий метод для получения списка упражнений за эту дату
        List<ExerciseModel> exercisesForDate = getExercisesByDate(date);

        // 2. Если упражнения найдены, упаковываем их в модель сессии
        if (exercisesForDate != null && !exercisesForDate.isEmpty()) {
            history.add(new WorkoutSessionModel(date, exercisesForDate));
        }

        return history;
    }



    // Вспомогательные методы getDouble/getInt оставляем без изменений
    private double getDouble(Object val) { if (val instanceof Number) return ((Number) val).doubleValue(); return 0.0; }
    private int getInt(Object val) { if (val instanceof Number) return ((Number) val).intValue(); return 0; }
}