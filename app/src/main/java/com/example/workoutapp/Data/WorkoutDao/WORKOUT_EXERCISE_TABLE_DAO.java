package com.example.workoutapp.Data.WorkoutDao;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.WorkoutModels.CardioSetModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.Models.WorkoutModels.StrengthSetModel;
import com.example.workoutapp.Models.WorkoutModels.WorkoutSessionModel;

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
    // Получение упражнений по состоянию
    // =========================
    public List<ExerciseModel> getExByState(String state) {
        List<ExerciseModel> exerciseList = new ArrayList<>();
        Cursor cursor = null;

        try {
            String query =
                    "SELECT " +
                            AppDataBase.WORKOUT_EXERCISE_ID + ", " +
                            AppDataBase.WORKOUT_EXERCISE_NAME + ", " +
                            AppDataBase.WORKOUT_EXERCISE_TYPE + ", " +
                            AppDataBase.WORKOUT_EXERCISE_BODY_TYPE + ", " +
                            AppDataBase.WORKOUT_EXERCISE_DATE + ", " +
                            AppDataBase.WORKOUT_EXERCISE_STATE +
                            " FROM " + AppDataBase.WORKOUT_EXERCISE_TABLE +
                            " WHERE " + AppDataBase.WORKOUT_EXERCISE_STATE + " = ?";

            cursor = db.rawQuery(query, new String[]{state});

            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_NAME));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_TYPE));
                String bodyType = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_BODY_TYPE));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_DATE));
                String currentState = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_STATE));

                List<Object> sets = new ArrayList<>();

                if ("Кардио".equalsIgnoreCase(type) || "Время".equalsIgnoreCase(type)) {
                    List<CardioSetModel> cardioSets = cardioSetDao.getSetsForExercise(id);
                    sets.addAll(cardioSets);
                } else {
                    List<StrengthSetModel> strengthSets = strengthSetDao.getSetsForExercise(id);
                    sets.addAll(strengthSets);
                }

                exerciseList.add(
                        new ExerciseModel(id, name, type, bodyType, date, currentState, sets)
                );
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return exerciseList;
    }

    // =========================
    // Добавление упражнения
    // =========================
    public void addExercise(String name, String type, String bodyType) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.WORKOUT_EXERCISE_NAME, name);
        values.put(AppDataBase.WORKOUT_EXERCISE_TYPE, type);
        values.put(AppDataBase.WORKOUT_EXERCISE_BODY_TYPE, bodyType);
        values.put(
                AppDataBase.WORKOUT_EXERCISE_DATE,
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date())
        );
        values.put(AppDataBase.WORKOUT_EXERCISE_STATE, "unfinished");

        db.insert(AppDataBase.WORKOUT_EXERCISE_TABLE, null, values);
    }

    // =========================
    // Удаление упражнения
    // =========================
    public void deleteExercise(ExerciseModel exercise) {
        if (exercise == null) return;

        long exerciseId = exercise.getExercise_id();

        strengthSetDao.deleteSetsForExercise(exerciseId);
        cardioSetDao.deleteSetsForExercise(exerciseId);

        db.delete(
                AppDataBase.WORKOUT_EXERCISE_TABLE,
                AppDataBase.WORKOUT_EXERCISE_ID + " = ?",
                new String[]{String.valueOf(exerciseId)}
        );
    }


    // =========================
    // Завершение упражнения
    // =========================
    public void markExerciseAsFinished(long exerciseId) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.WORKOUT_EXERCISE_STATE, "finished");

        db.update(
                AppDataBase.WORKOUT_EXERCISE_TABLE,
                values,
                AppDataBase.WORKOUT_EXERCISE_ID + " = ?",
                new String[]{String.valueOf(exerciseId)}
        );
    }

    // =========================
    // Последняя дата тренировки
    // =========================
    public String getLatestWorkoutDate() {
        Cursor cursor = null;
        try {
            String query =
                    "SELECT MAX(" + AppDataBase.WORKOUT_EXERCISE_DATE + ")" +
                            " FROM " + AppDataBase.WORKOUT_EXERCISE_TABLE +
                            " WHERE " + AppDataBase.WORKOUT_EXERCISE_STATE + " = ?";

            cursor = db.rawQuery(query, new String[]{"finished"});
            return cursor.moveToFirst() ? cursor.getString(0) : null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public long getCount() {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + AppDataBase.WORKOUT_EXERCISE_TABLE, null);
        long count = 0;
        if (cursor.moveToFirst()) count = cursor.getLong(0);
        cursor.close();
        return count;
    }

    public void deleteAllWorkouts() {
        // Удаляем все записи из таблицы упражнений тренировок
        db.delete(AppDataBase.WORKOUT_EXERCISE_TABLE, null, null);
        db.delete("sqlite_sequence", "name = ?", new String[]{AppDataBase.WORKOUT_EXERCISE_TABLE});
        // Также необходимо очистить таблицы сетов, чтобы не занимать место
        db.delete(AppDataBase.STRENGTH_SET_DETAILS_TABLE, null, null);
        db.delete(AppDataBase.CARDIO_SET_DETAILS_TABLE, null, null);
    }

    // =========================
    // Получение списка СЕССИЙ (группировка по датам) с пагинацией
    // =========================
    public List<WorkoutSessionModel> getWorkoutHistory(int limit, int offset) {
        List<WorkoutSessionModel> history = new ArrayList<>();

        // 1. Получаем список уникальных дат завершенных тренировок
        String dateQuery = "SELECT DISTINCT " + AppDataBase.WORKOUT_EXERCISE_DATE +
                " FROM " + AppDataBase.WORKOUT_EXERCISE_TABLE +
                " WHERE " + AppDataBase.WORKOUT_EXERCISE_STATE + " = 'finished'" +
                " ORDER BY " + AppDataBase.WORKOUT_EXERCISE_DATE + " DESC" +
                " LIMIT ? OFFSET ?";

        Cursor dateCursor = db.rawQuery(dateQuery, new String[]{String.valueOf(limit), String.valueOf(offset)});

        while (dateCursor.moveToNext()) {
            String date = dateCursor.getString(0);
            // 2. Для каждой даты подтягиваем список упражнений
            List<ExerciseModel> exercisesForDate = getExercisesByDate(date);
            history.add(new WorkoutSessionModel(date, exercisesForDate));
        }
        dateCursor.close();
        return history;
    }

    // =========================
    // Получение упражнений за конкретную дату
    // =========================
    public List<ExerciseModel> getExercisesByDate(String date) {
        List<ExerciseModel> exerciseList = new ArrayList<>();
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM " + AppDataBase.WORKOUT_EXERCISE_TABLE +
                    " WHERE " + AppDataBase.WORKOUT_EXERCISE_DATE + " = ?" +
                    " AND " + AppDataBase.WORKOUT_EXERCISE_STATE + " = 'finished'";

            cursor = db.rawQuery(query, new String[]{date});

            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_NAME));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_TYPE));
                String bodyType = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_BODY_TYPE));
                String exDate = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_DATE));
                String state = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_STATE));

                List<Object> sets = new ArrayList<>();
                if ("Кардио".equalsIgnoreCase(type) || "Время".equalsIgnoreCase(type)) {
                    sets.addAll(cardioSetDao.getSetsForExercise(id));
                } else {
                    sets.addAll(strengthSetDao.getSetsForExercise(id));
                }

                exerciseList.add(new ExerciseModel(id, name, type, bodyType, exDate, state, sets));
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return exerciseList;
    }

    // =========================
    // Получение истории за конкретную дату (для поиска)
    // =========================
    public List<WorkoutSessionModel> getWorkoutHistoryByDate(String date) {
        List<WorkoutSessionModel> history = new ArrayList<>();

        // Получаем список упражнений за этот день
        List<ExerciseModel> exercisesForDate = getExercisesByDate(date);

        // Если упражнения найдены, создаем одну сессию и добавляем в список
        if (!exercisesForDate.isEmpty()) {
            history.add(new WorkoutSessionModel(date, exercisesForDate));
        }

        return history;
    }

    public List<ExerciseModel> getAllExercisesForSync() {
        List<ExerciseModel> exerciseList = new ArrayList<>();
        Cursor cursor = null;

        try {
            // Выбираем абсолютно все упражнения без фильтрации по state
            String query = "SELECT * FROM " + AppDataBase.WORKOUT_EXERCISE_TABLE;
            cursor = db.rawQuery(query, null);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_NAME));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_TYPE));
                String bodyType = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_BODY_TYPE));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_DATE));
                String state = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_STATE));

                List<Object> sets = new ArrayList<>();
                // Проверяем тип и подтягиваем сеты через существующие DAO сетов
                if ("Кардио".equalsIgnoreCase(type) || "Время".equalsIgnoreCase(type)) {
                    sets.addAll(cardioSetDao.getSetsForExercise(id));
                } else {
                    sets.addAll(strengthSetDao.getSetsForExercise(id));
                }

                exerciseList.add(new ExerciseModel(id, name, type, bodyType, date, state, sets));
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return exerciseList;
    }

    private void saveCloudSessionToSQLite(WorkoutSessionModel session) {
        if (session == null || session.getExercises() == null) return;

        // Нам нужен доступ к базе. В вашем случае можно взять через MainActivity
        net.sqlcipher.database.SQLiteDatabase db = com.example.workoutapp.MainActivity.getAppDataBase();
        com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO dao =
                new com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO(db);

        // Логика сохранения сессии из облака
        // ВАЖНО: В вашем DAO должен быть метод, который принимает готовый ExerciseModel
        // и сохраняет его вместе с вложенными сетами.
        for (ExerciseModel ex : session.getExercises()) {
            // Устанавливаем дату из сессии, если в самом упражнении она вдруг пустая
            if (ex.getEx_Data() == null || ex.getEx_Data().isEmpty()) {
                ex.setEx_Data(session.getWorkoutDate());
            }

            // Вызываем сохранение в базу (нужно реализовать метод addFullExercise в DAO)
            dao.addFullExerciseFromCloud(ex);
        }
        Log.d("FirestoreSync", "Данные за " + session.getWorkoutDate() + " скачаны и сохранены локально");
    }

    public void addFullExerciseFromCloud(ExerciseModel ex) {
        if (ex == null) return;

        ContentValues values = new ContentValues();
        values.put(AppDataBase.WORKOUT_EXERCISE_NAME, ex.getExerciseName());
        values.put(AppDataBase.WORKOUT_EXERCISE_TYPE, ex.getExerciseType());
        values.put(AppDataBase.WORKOUT_EXERCISE_BODY_TYPE, ex.getExerciseBodyType());
        values.put(AppDataBase.WORKOUT_EXERCISE_DATE, ex.getEx_Data());
        values.put(AppDataBase.WORKOUT_EXERCISE_STATE, ex.getState());

        long newExId = db.insert(AppDataBase.WORKOUT_EXERCISE_TABLE, null, values);

        if (ex.getSets() != null && newExId != -1) {
            for (Object setObj : ex.getSets()) {

                // ЕСЛИ ПРИШЛА MAP (из Firebase)
                if (setObj instanceof java.util.Map) {
                    java.util.Map<String, Object> map = (java.util.Map<String, Object>) setObj;
                    String type = (String) map.get("type");

                    if ("strength".equals(type)) {
                        double weight = getDouble(map.get("weight"));
                        int rep = getInt(map.get("rep"));
                        int order = getInt(map.get("order"));
                        String state = (String) map.get("state");
                        strengthSetDao.addStrengthSet(newExId, weight, rep, order, state);

                    } else if ("cardio".equals(type)) {
                        double temp = getDouble(map.get("temp"));
                        int time = getInt(map.get("time"));
                        double distance = getDouble(map.get("distance"));
                        int order = getInt(map.get("order"));
                        String state = (String) map.get("state");
                        cardioSetDao.addCardioSet(newExId, temp, time, distance, order, state);
                    }
                }
                // ЕСЛИ ПРИШЕЛ ОБЪЕКТ (локально)
                else if (setObj instanceof StrengthSetModel) {
                    StrengthSetModel s = (StrengthSetModel) setObj;
                    strengthSetDao.addStrengthSet(newExId, s.getWeight(), s.getRep(), s.getOrder(), s.getState());
                } else if (setObj instanceof CardioSetModel) {
                    CardioSetModel c = (CardioSetModel) setObj;
                    cardioSetDao.addCardioSet(newExId, c.getTemp(), c.getTime(), c.getDistance(), c.getOrder(), c.getState());
                }
            }
        }
    }

    // Вспомогательные методы для безопасного извлечения чисел из Map
    private double getDouble(Object val) {
        if (val instanceof Number) return ((Number) val).doubleValue();
        return 0.0;
    }

    private int getInt(Object val) {
        if (val instanceof Number) return ((Number) val).intValue();
        return 0;
    }

    /**
     * Удаляет все упражнения за конкретную дату.
     * Используется для предотвращения дублирования данных при синхронизации.
     * @param date Дата в формате String (например, "2026-02-24")
     */
    public void deleteExercisesByDate(String date) {
        if (date == null || date.isEmpty()) return;

        try {
            // Замени WORKOUT_EXERCISE_TABLE и ex_Data на свои константы,
            // если они называются иначе в твоем классе AppDataBase
            db.delete("WORKOUT_EXERCISE_TABLE", "ex_Data = ?", new String[]{date});
        } catch (Exception e) {
            android.util.Log.e("SQL_ERROR", "Ошибка при удалении упражнений за дату: " + date, e);
        }
    }

    public boolean isExerciseExists(String exerciseName, String date) {
        // Используй свои имена колонок (из CSV видно: workout_exercise_name и workout_exercise_data)
        String query = "SELECT 1 FROM WORKOUT_EXERCISE_TABLE WHERE workout_exercise_name = ? AND workout_exercise_data = ?";
        Cursor cursor = db.rawQuery(query, new String[]{exerciseName, date});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}
