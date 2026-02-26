package com.example.workoutapp.Data.ProfileDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.ACTIVITY_CALORIES_TO_BURN;
import static com.example.workoutapp.Data.Tables.AppDataBase.ACTIVITY_GOAL_DATE;
import static com.example.workoutapp.Data.Tables.AppDataBase.ACTIVITY_GOAL_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.ACTIVITY_GOAL_STEPS;
import static com.example.workoutapp.Data.Tables.AppDataBase.ACTIVITY_GOAL_TABLE;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.workoutapp.Models.ProfileModels.ActivityGoalModel;

import net.sqlcipher.database.SQLiteDatabase;

public class ActivityGoalDao {

    private final SQLiteDatabase db;

    public ActivityGoalDao(SQLiteDatabase db) {
        this.db = db;
    }

    // =========================
    // Добавление новой цели активности
    // =========================
    public void addGoal(ActivityGoalModel goal) {
        if (goal == null) return;

        ContentValues values = new ContentValues();
        values.put(ACTIVITY_GOAL_DATE, goal.getActivity_goal_date());
        values.put(ACTIVITY_GOAL_STEPS, goal.getActivity_goal_steps());
        values.put(ACTIVITY_CALORIES_TO_BURN, goal.getActivity_goal_caloriesToBurn());

        db.insert(ACTIVITY_GOAL_TABLE, null, values);
    }

    // =========================
    // Получение последней цели активности
    // =========================
    public ActivityGoalModel getLatestGoal() {
        ActivityGoalModel goal = null;
        Cursor cursor = null;

        try {
            cursor = db.query(
                    ACTIVITY_GOAL_TABLE,
                    null,
                    null,
                    null,
                    null,
                    null,
                    ACTIVITY_GOAL_DATE + " DESC, " + ACTIVITY_GOAL_ID + " DESC",
                    "1"
            );

            if (cursor != null && cursor.moveToFirst()) {
                goal = new ActivityGoalModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(ACTIVITY_GOAL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ACTIVITY_GOAL_DATE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(ACTIVITY_GOAL_STEPS)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(ACTIVITY_CALORIES_TO_BURN))
                );
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return goal;
    }
}
