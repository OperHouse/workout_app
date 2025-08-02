package com.example.workoutapp.Data.WorkoutDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.COMPLETED_WORKOUT_DATE;
import static com.example.workoutapp.Data.Tables.AppDataBase.COMPLETED_WORKOUT_EXERCISE_BODY_TYPE;
import static com.example.workoutapp.Data.Tables.AppDataBase.COMPLETED_WORKOUT_EXERCISE_NAME;
import static com.example.workoutapp.Data.Tables.AppDataBase.COMPLETED_WORKOUT_EXERCISE_TABLE;
import static com.example.workoutapp.Data.Tables.AppDataBase.COMPLETED_WORKOUT_EXERCISE_TYPE;
import static com.example.workoutapp.Data.Tables.AppDataBase.COMPLETED_WORKOUT_SET_EXERCISE_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.COMPLETED_WORKOUT_SET_IS_SELECTED;
import static com.example.workoutapp.Data.Tables.AppDataBase.COMPLETED_WORKOUT_SET_NUMBER;
import static com.example.workoutapp.Data.Tables.AppDataBase.COMPLETED_WORKOUT_SET_REP;
import static com.example.workoutapp.Data.Tables.AppDataBase.COMPLETED_WORKOUT_SET_TABLE;
import static com.example.workoutapp.Data.Tables.AppDataBase.COMPLETED_WORKOUT_SET_WEIGHT;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.WorkoutModels.SetsModel;
import com.example.workoutapp.Models.WorkoutModels.TempExModel;

import java.util.List;

public class CompletedWorkoutDao {

    private final AppDataBase dbHelper;

    public CompletedWorkoutDao(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void insertCompletedWorkouts(List<TempExModel> workouts) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        for (TempExModel ex : workouts) {
            ContentValues workoutValues = new ContentValues();
            workoutValues.put(COMPLETED_WORKOUT_EXERCISE_NAME, ex.getExName());
            workoutValues.put(COMPLETED_WORKOUT_EXERCISE_TYPE, ex.getTypeEx());
            workoutValues.put(COMPLETED_WORKOUT_EXERCISE_BODY_TYPE, ex.getBodyType());
            workoutValues.put(COMPLETED_WORKOUT_DATE, ex.getData());

            long workoutId = db.insert(COMPLETED_WORKOUT_EXERCISE_TABLE, null, workoutValues);

            for (SetsModel set : ex.getSetsList()) {
                ContentValues setValues = new ContentValues();
                setValues.put(COMPLETED_WORKOUT_SET_EXERCISE_ID, workoutId);
                setValues.put(COMPLETED_WORKOUT_SET_NUMBER, set.getSet_id());
                setValues.put(COMPLETED_WORKOUT_SET_WEIGHT, set.getWeight());
                setValues.put(COMPLETED_WORKOUT_SET_REP, set.getReps());
                setValues.put(COMPLETED_WORKOUT_SET_IS_SELECTED, set.getIsSelected() ? 1 : 0);

                db.insert(COMPLETED_WORKOUT_SET_TABLE, null, setValues);
            }
        }

        db.close();
    }
}
