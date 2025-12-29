package com.example.workoutapp.Data.ProfileDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.GENERAL_GLOBAL_GOAL_TEXT;
import static com.example.workoutapp.Data.Tables.AppDataBase.GENERAL_GOAL_DATE;
import static com.example.workoutapp.Data.Tables.AppDataBase.GENERAL_GOAL_FOOD_TRACKING_WEEKLY;
import static com.example.workoutapp.Data.Tables.AppDataBase.GENERAL_GOAL_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.GENERAL_GOAL_TABLE;
import static com.example.workoutapp.Data.Tables.AppDataBase.GENERAL_GOAL_WORKOUTS_WEEKLY;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.ProfileModels.GeneralGoalModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GeneralGoalDao {

    private final AppDataBase dbHelper;

    public GeneralGoalDao(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Добавление новой цели
    // Добавление новой цели без возврата ID
    public void insertGoal(GeneralGoalModel goal) {
        if (goal == null) return;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        if (goal.getGoalText() != null && !goal.getGoalText().isEmpty())
            values.put(GENERAL_GLOBAL_GOAL_TEXT, goal.getGoalText());

        if (goal.getWorkoutsWeekly() >= 0)
            values.put(GENERAL_GOAL_WORKOUTS_WEEKLY, goal.getWorkoutsWeekly());

        if (goal.getFoodTrackingWeekly() >= 0)
            values.put(GENERAL_GOAL_FOOD_TRACKING_WEEKLY, goal.getFoodTrackingWeekly());

        // Дата — если null, ставим текущую
        String date = goal.getDate();
        if (date == null || date.isEmpty()) {
            date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(new Date(System.currentTimeMillis()));
        }
        values.put(GENERAL_GOAL_DATE, date);

        db.insert(GENERAL_GOAL_TABLE, null, values);
        db.close();
    }


    // Обновление существующей цели по ID
    public int updateGoal(GeneralGoalModel goal) {
        if (goal == null || goal.getId() <= 0) return 0;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = getContentValues(goal);

        int updatedRows = db.update(
                GENERAL_GOAL_TABLE,
                values,
                GENERAL_GOAL_ID + " = ?",
                new String[]{String.valueOf(goal.getId())}
        );
        db.close();
        return updatedRows;
    }

    @NonNull
    private static ContentValues getContentValues(GeneralGoalModel goal) {
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

    // Получение последней цели (по дате)
    public GeneralGoalModel getLatestGoal() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        GeneralGoalModel latestGoal = null;

        Cursor cursor = db.query(
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
            cursor.close();
        }

        db.close();
        return latestGoal;
    }

    // Полная очистка таблицы
    public void clearAllGoals() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(GENERAL_GOAL_TABLE, null, null);
        db.close();
    }
}
