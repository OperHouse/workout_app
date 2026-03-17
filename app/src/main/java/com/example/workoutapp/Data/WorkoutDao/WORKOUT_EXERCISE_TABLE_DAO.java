package com.example.workoutapp.Data.WorkoutDao;

import android.content.ContentValues;
import android.database.Cursor;

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
}
