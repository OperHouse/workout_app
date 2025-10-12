package com.example.workoutapp.Data.ProfileDao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.ProfileModels.GoalModel;

import java.util.ArrayList;
import java.util.List;

public class GoalDao {
    private final AppDataBase dbHelper;

    public GoalDao(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Вставляет новую цель, сохраняя историчность
    public long insertNewGoal(GoalModel goal) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(AppDataBase.GOAL_START_DATE, goal.getGoalStartDate());
        values.put(AppDataBase.USER_GOAL_TEXT, goal.getUserGoalText());
        values.put(AppDataBase.GOAL_CALORIES_GAIN, goal.getGoalCaloriesGain());
        values.put(AppDataBase.GOAL_PROTEIN, goal.getGoalProtein());
        values.put(AppDataBase.GOAL_FAT, goal.getGoalFat());
        values.put(AppDataBase.GOAL_CARB, goal.getGoalCarb());
        values.put(AppDataBase.GOAL_TO_BURN_CALORIES, goal.getGoalToBurnCalories());
        values.put(AppDataBase.GOAL_STEPS, goal.getGoalSteps());
        values.put(AppDataBase.GOAL_WORKOUTS_WEEKLY, goal.getGoalWorkoutsWeekly());

        long id = db.insert(AppDataBase.GOAL_TABLE, null, values);
        db.close();
        return id;
    }

    // Получает активную цель на текущий момент (самая последняя запись по дате старта)
    public GoalModel getCurrentGoal() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        GoalModel currentGoal = null;

        Cursor cursor = db.query(
                AppDataBase.GOAL_TABLE,
                null,
                null,
                null,
                null,
                null,
                AppDataBase.GOAL_START_DATE + " DESC", // Сортируем по убыванию даты старта
                "1" // Берем только одну запись
        );

        if (cursor != null && cursor.moveToFirst()) {
            currentGoal = new GoalModel(
                    cursor.getLong(cursor.getColumnIndexOrThrow(AppDataBase.GOAL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.GOAL_START_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.USER_GOAL_TEXT)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.GOAL_CALORIES_GAIN)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.GOAL_PROTEIN)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.GOAL_FAT)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.GOAL_CARB)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.GOAL_TO_BURN_CALORIES)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.GOAL_STEPS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.GOAL_WORKOUTS_WEEKLY))
            );
            cursor.close();
        }

        db.close();
        return currentGoal;
    }

    // Получает цель, которая действовала на конкретную дату (для исторического анализа)
    public GoalModel getGoalByDate(String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        GoalModel goal = null;

        // Запрос: выбрать самую последнюю цель, дата старта которой МЕНЬШЕ или РАВНА заданной дате
        Cursor cursor = db.query(
                AppDataBase.GOAL_TABLE,
                null,
                AppDataBase.GOAL_START_DATE + " <= ?",
                new String[]{date},
                null,
                null,
                AppDataBase.GOAL_START_DATE + " DESC", // Сортируем по убыванию даты старта
                "1" // Берем только одну (самую свежую) запись
        );

        if (cursor != null && cursor.moveToFirst()) {
            goal = new GoalModel(
                    cursor.getLong(cursor.getColumnIndexOrThrow(AppDataBase.GOAL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.GOAL_START_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.USER_GOAL_TEXT)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.GOAL_CALORIES_GAIN)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.GOAL_PROTEIN)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.GOAL_FAT)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.GOAL_CARB)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.GOAL_TO_BURN_CALORIES)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.GOAL_STEPS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.GOAL_WORKOUTS_WEEKLY))
            );
            cursor.close();
        }

        db.close();
        return goal;
    }
}
