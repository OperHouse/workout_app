package com.example.workoutapp.Data.ProfileDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.GENERAL_GLOBAL_GOAL_TEXT;
import static com.example.workoutapp.Data.Tables.AppDataBase.GENERAL_GOAL_DATE;
import static com.example.workoutapp.Data.Tables.AppDataBase.GENERAL_GOAL_FOOD_TRACKING_WEEKLY;
import static com.example.workoutapp.Data.Tables.AppDataBase.GENERAL_GOAL_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.GENERAL_GOAL_TABLE;
import static com.example.workoutapp.Data.Tables.AppDataBase.GENERAL_GOAL_WORKOUTS_WEEKLY;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.ProfileModels.GeneralGoalModel;

import net.sqlcipher.database.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

        // Добавляем UID в ContentValues
        values.put(AppDataBase.GENERAL_GOAL_UID, goal.getGeneral_goal_uid());

        if (!values.containsKey(GENERAL_GOAL_DATE)) {
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(new Date(System.currentTimeMillis()));
            values.put(GENERAL_GOAL_DATE, date);
        }
        db.insert(GENERAL_GOAL_TABLE, null, values);
    }

    public GeneralGoalModel getLatestGoal() {
        GeneralGoalModel latestGoal = null;
        Cursor cursor = null;
        try {
            cursor = db.query(GENERAL_GOAL_TABLE, null, null, null, null, null,
                    GENERAL_GOAL_DATE + " DESC, " + GENERAL_GOAL_ID + " DESC", "1");

            if (cursor != null && cursor.moveToFirst()) {
                latestGoal = new GeneralGoalModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(GENERAL_GOAL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(GENERAL_GLOBAL_GOAL_TEXT)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(GENERAL_GOAL_WORKOUTS_WEEKLY)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(GENERAL_GOAL_FOOD_TRACKING_WEEKLY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(GENERAL_GOAL_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.GENERAL_GOAL_UID)) // Читаем UID
                );
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return latestGoal;
    }

    // Новые методы для синхронизации
    public boolean isGoalUidExists(String uid) {
        if (uid == null) return false;
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + GENERAL_GOAL_TABLE + " WHERE general_goal_uid = ?", new String[]{uid});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public List<GeneralGoalModel> getAllGoals() {
        List<GeneralGoalModel> list = new ArrayList<>();
        Cursor cursor = db.query(GENERAL_GOAL_TABLE, null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(new GeneralGoalModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(GENERAL_GOAL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(GENERAL_GLOBAL_GOAL_TEXT)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(GENERAL_GOAL_WORKOUTS_WEEKLY)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(GENERAL_GOAL_FOOD_TRACKING_WEEKLY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(GENERAL_GOAL_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.GENERAL_GOAL_UID))
                ));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public void updateGoalUid(int id, String uid) {
        ContentValues cv = new ContentValues();
        cv.put(AppDataBase.GENERAL_GOAL_UID, uid);
        db.update(GENERAL_GOAL_TABLE, cv, GENERAL_GOAL_ID + " = ?", new String[]{String.valueOf(id)});
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

        if (goal.getGeneral_goal_Text() != null && !goal.getGeneral_goal_Text().isEmpty())
            values.put(GENERAL_GLOBAL_GOAL_TEXT, goal.getGeneral_goal_Text());

        if (goal.getGeneral_goal_workoutsWeekly() >= 0)
            values.put(GENERAL_GOAL_WORKOUTS_WEEKLY, goal.getGeneral_goal_workoutsWeekly());

        if (goal.getGeneral_goal_foodTrackingWeekly() >= 0)
            values.put(GENERAL_GOAL_FOOD_TRACKING_WEEKLY, goal.getGeneral_goal_foodTrackingWeekly());

        if (goal.getGeneral_goal_date() != null && !goal.getGeneral_goal_date().isEmpty())
            values.put(GENERAL_GOAL_DATE, goal.getGeneral_goal_date());

        return values;
    }
}
