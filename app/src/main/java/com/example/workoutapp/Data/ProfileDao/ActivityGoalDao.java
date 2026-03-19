package com.example.workoutapp.Data.ProfileDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.ACTIVITY_CALORIES_TO_BURN;
import static com.example.workoutapp.Data.Tables.AppDataBase.ACTIVITY_GOAL_DATE;
import static com.example.workoutapp.Data.Tables.AppDataBase.ACTIVITY_GOAL_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.ACTIVITY_GOAL_STEPS;
import static com.example.workoutapp.Data.Tables.AppDataBase.ACTIVITY_GOAL_TABLE;
import static com.example.workoutapp.Data.Tables.AppDataBase.ACTIVITY_GOAL_UID;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.workoutapp.Models.ProfileModels.ActivityGoalModel;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

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
        values.put(ACTIVITY_GOAL_UID, goal.getActivity_goal_uid());

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
                        cursor.getInt(cursor.getColumnIndexOrThrow(ACTIVITY_CALORIES_TO_BURN)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ACTIVITY_GOAL_UID))
                );
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return goal;
    }

    public boolean isGoalUidExists(String uid) {
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + ACTIVITY_GOAL_TABLE + " WHERE activity_goal_uid = ?", new String[]{uid});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public List<ActivityGoalModel> getAllGoals() {
        List<ActivityGoalModel> list = new ArrayList<>();
        Cursor cursor = db.query(ACTIVITY_GOAL_TABLE, null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(new ActivityGoalModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(ACTIVITY_GOAL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ACTIVITY_GOAL_DATE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(ACTIVITY_GOAL_STEPS)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(ACTIVITY_CALORIES_TO_BURN)),
                        cursor.getString(cursor.getColumnIndexOrThrow("activity_goal_uid"))
                ));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    // Обновление UID для старых записей
    public void updateGoalUid(int id, String uid) {
        ContentValues cv = new ContentValues();
        cv.put("activity_goal_uid", uid);
        db.update(ACTIVITY_GOAL_TABLE, cv, ACTIVITY_GOAL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public ActivityGoalModel getGoalByUid(String uid) {
        ActivityGoalModel goal = null;
        Cursor cursor = db.query(ACTIVITY_GOAL_TABLE, null, ACTIVITY_GOAL_UID + " = ?", new String[]{uid}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            goal = new ActivityGoalModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(ACTIVITY_GOAL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(ACTIVITY_GOAL_DATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(ACTIVITY_GOAL_STEPS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(ACTIVITY_CALORIES_TO_BURN)),
                    cursor.getString(cursor.getColumnIndexOrThrow(ACTIVITY_GOAL_UID))
            );
            cursor.close();
        }
        return goal;
    }
}
