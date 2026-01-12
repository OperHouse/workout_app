package com.example.workoutapp.Data.ProfileDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.GENERAL_GLOBAL_GOAL_TEXT;
import static com.example.workoutapp.Data.Tables.AppDataBase.GENERAL_GOAL_DATE;
import static com.example.workoutapp.Data.Tables.AppDataBase.GENERAL_GOAL_FOOD_TRACKING_WEEKLY;
import static com.example.workoutapp.Data.Tables.AppDataBase.GENERAL_GOAL_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.GENERAL_GOAL_TABLE;
import static com.example.workoutapp.Data.Tables.AppDataBase.GENERAL_GOAL_WORKOUTS_WEEKLY;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.workoutapp.Models.ProfileModels.GeneralGoalModel;

import net.sqlcipher.database.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GeneralGoalDao {

    private final SQLiteDatabase db;

    public GeneralGoalDao(SQLiteDatabase db) {
        this.db = db;
    }

    // =========================
    // Добавление новой цели
    // =========================
    public void insertGoal(GeneralGoalModel goal) {
        if (goal == null) return;

        ContentValues values = getContentValues(goal);

        // Если дата не указана, ставим текущую
        if (!values.containsKey(GENERAL_GOAL_DATE)) {
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(new Date(System.currentTimeMillis()));
            values.put(GENERAL_GOAL_DATE, date);
        }

        db.insert(GENERAL_GOAL_TABLE, null, values);
    }

    // =========================
    // Обновление существующей цели по ID
    // =========================
    public int updateGoal(GeneralGoalModel goal) {
        if (goal == null || goal.getId() <= 0) return 0;

        ContentValues values = getContentValues(goal);

        return db.update(
                GENERAL_GOAL_TABLE,
                values,
                GENERAL_GOAL_ID + " = ?",
                new String[]{String.valueOf(goal.getId())}
        );
    }

    // =========================
    // Получение последней цели (по дате)
    // =========================
    public GeneralGoalModel getLatestGoal() {
        GeneralGoalModel latestGoal = null;
        Cursor cursor = null;

        try {
            cursor = db.query(
                    GENERAL_GOAL_TABLE,
                    null,
                    null,
                    null,
                    null,
                    null,
                    GENERAL_GOAL_DATE + " DESC, " + GENERAL_GOAL_ID + " DESC",
                    "1"
            );

            if (cursor != null && cursor.moveToFirst()) {
                latestGoal = new GeneralGoalModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(GENERAL_GOAL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(GENERAL_GLOBAL_GOAL_TEXT)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(GENERAL_GOAL_WORKOUTS_WEEKLY)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(GENERAL_GOAL_FOOD_TRACKING_WEEKLY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(GENERAL_GOAL_DATE))
                );
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return latestGoal;
    }

    // =========================
    // Полная очистка таблицы
    // =========================
    public void clearAllGoals() {
        db.delete(GENERAL_GOAL_TABLE, null, null);
    }

    // =========================
    // Приватный метод для подготовки ContentValues
    // =========================
    private ContentValues getContentValues(GeneralGoalModel goal) {
        ContentValues values = new ContentValues();

        if (goal.getGoalText() != null && !goal.getGoalText().isEmpty())
            values.put(GENERAL_GLOBAL_GOAL_TEXT, goal.getGoalText());

        if (goal.getWorkoutsWeekly() >= 0)
            values.put(GENERAL_GOAL_WORKOUTS_WEEKLY, goal.getWorkoutsWeekly());

        if (goal.getFoodTrackingWeekly() >= 0)
            values.put(GENERAL_GOAL_FOOD_TRACKING_WEEKLY, goal.getFoodTrackingWeekly());

        if (goal.getDate() != null && !goal.getDate().isEmpty())
            values.put(GENERAL_GOAL_DATE, goal.getDate());

        return values;
    }
}
