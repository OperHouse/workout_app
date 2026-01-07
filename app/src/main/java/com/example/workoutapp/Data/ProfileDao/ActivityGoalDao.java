package com.example.workoutapp.Data.ProfileDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.ACTIVITY_CALORIES_TO_BURN;
import static com.example.workoutapp.Data.Tables.AppDataBase.ACTIVITY_GOAL_DATE;
import static com.example.workoutapp.Data.Tables.AppDataBase.ACTIVITY_GOAL_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.ACTIVITY_GOAL_STEPS;
import static com.example.workoutapp.Data.Tables.AppDataBase.ACTIVITY_GOAL_TABLE;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.ProfileModels.ActivityGoalModel;

public class ActivityGoalDao {

    private final AppDataBase dbHelper;

    public ActivityGoalDao(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Добавление новой цели активности
    public void insertGoal(ActivityGoalModel goal) {
        if (goal == null) return;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ACTIVITY_GOAL_DATE, goal.getDate());
        values.put(ACTIVITY_GOAL_STEPS, goal.getStepsGoal());
        values.put(ACTIVITY_CALORIES_TO_BURN, goal.getCaloriesToBurn());

        db.insert(ACTIVITY_GOAL_TABLE, null, values);
    }

    // Получение последней цели активности (по дате и ID)
    public ActivityGoalModel getLatestGoal() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ActivityGoalModel goal = null;

        Cursor cursor = db.query(
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
            cursor.close();
        }
        return goal;
    }
}
