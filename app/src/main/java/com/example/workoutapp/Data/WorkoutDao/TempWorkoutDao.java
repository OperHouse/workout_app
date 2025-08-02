package com.example.workoutapp.Data.WorkoutDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.TEMP_WORKOUT_DATE;
import static com.example.workoutapp.Data.Tables.AppDataBase.TEMP_WORKOUT_EXERCISE_BODY_TYPE;
import static com.example.workoutapp.Data.Tables.AppDataBase.TEMP_WORKOUT_EXERCISE_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.TEMP_WORKOUT_EXERCISE_NAME;
import static com.example.workoutapp.Data.Tables.AppDataBase.TEMP_WORKOUT_EXERCISE_TABLE;
import static com.example.workoutapp.Data.Tables.AppDataBase.TEMP_WORKOUT_EXERCISE_TYPE;
import static com.example.workoutapp.Data.Tables.AppDataBase.TEMP_WORKOUT_SET_EXERCISE_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.TEMP_WORKOUT_SET_IS_SELECTED;
import static com.example.workoutapp.Data.Tables.AppDataBase.TEMP_WORKOUT_SET_NUMBER;
import static com.example.workoutapp.Data.Tables.AppDataBase.TEMP_WORKOUT_SET_REP;
import static com.example.workoutapp.Data.Tables.AppDataBase.TEMP_WORKOUT_SET_TABLE;
import static com.example.workoutapp.Data.Tables.AppDataBase.TEMP_WORKOUT_SET_WEIGHT;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.WorkoutModels.SetsModel;
import com.example.workoutapp.Models.WorkoutModels.TempExModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TempWorkoutDao {

    private final AppDataBase dbHelper;

    public TempWorkoutDao(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Добавить новое упражнение во временную таблицу
    public void addTempExercise(String name, String type, String bodyType) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        ContentValues values = new ContentValues();
        values.put(TEMP_WORKOUT_EXERCISE_NAME, name);
        values.put(TEMP_WORKOUT_EXERCISE_TYPE, type);
        values.put(TEMP_WORKOUT_EXERCISE_BODY_TYPE, bodyType);
        values.put(TEMP_WORKOUT_DATE, date);

        db.insert(TEMP_WORKOUT_EXERCISE_TABLE, null, values);
        db.close();
    }

    // Добавить новый подход
    public void addTempSet(int exerciseId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String query = "SELECT MAX(" + TEMP_WORKOUT_SET_NUMBER + ") FROM " + TEMP_WORKOUT_SET_TABLE +
                " WHERE " + TEMP_WORKOUT_SET_EXERCISE_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(exerciseId)});

        int nextSetNumber = 1;
        if (cursor.moveToFirst()) {
            nextSetNumber = cursor.getInt(0) + 1;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(TEMP_WORKOUT_SET_EXERCISE_ID, exerciseId);
        values.put(TEMP_WORKOUT_SET_NUMBER, nextSetNumber);
        values.putNull(TEMP_WORKOUT_SET_WEIGHT);
        values.putNull(TEMP_WORKOUT_SET_REP);
        values.put(TEMP_WORKOUT_SET_IS_SELECTED, 0);

        db.insert(TEMP_WORKOUT_SET_TABLE, null, values);
        db.close();
    }

    // Получить все подходы по упражнению
    public ArrayList<SetsModel> getTempSetsByExercise(long exerciseId) {
        ArrayList<SetsModel> setsList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT " + TEMP_WORKOUT_SET_NUMBER + ", " + TEMP_WORKOUT_SET_WEIGHT + ", " +
                TEMP_WORKOUT_SET_REP + ", " + TEMP_WORKOUT_SET_IS_SELECTED +
                " FROM " + TEMP_WORKOUT_SET_TABLE +
                " WHERE " + TEMP_WORKOUT_SET_EXERCISE_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(exerciseId)});

        while (cursor.moveToNext()) {
            SetsModel set = new SetsModel();
            set.setSet_id(cursor.getInt(cursor.getColumnIndexOrThrow(TEMP_WORKOUT_SET_NUMBER)));
            set.setWeight(cursor.getInt(cursor.getColumnIndexOrThrow(TEMP_WORKOUT_SET_WEIGHT)));
            set.setReps(cursor.getInt(cursor.getColumnIndexOrThrow(TEMP_WORKOUT_SET_REP)));
            set.setIsSelected(cursor.getInt(cursor.getColumnIndexOrThrow(TEMP_WORKOUT_SET_IS_SELECTED)) == 1);
            setsList.add(set);
        }

        cursor.close();
        db.close();
        return setsList;
    }

    // Обновить флаг выбора у сета
    public void updateTempSetIsSelected(int exerciseId, int setNumber, boolean isSelected) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TEMP_WORKOUT_SET_IS_SELECTED, isSelected ? 1 : 0);

        db.update(TEMP_WORKOUT_SET_TABLE, values,
                TEMP_WORKOUT_SET_EXERCISE_ID + "=? AND " + TEMP_WORKOUT_SET_NUMBER + "=?",
                new String[]{String.valueOf(exerciseId), String.valueOf(setNumber)});

        db.close();
    }

    public boolean checkIfTempExerciseExists(String exerciseName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + TEMP_WORKOUT_EXERCISE_TABLE + " WHERE " + TEMP_WORKOUT_EXERCISE_NAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{exerciseName});

        boolean exists = cursor.moveToFirst();

        cursor.close();
        db.close();

        return exists;
    }

    // Получить список всех упражнений с подходами
    public ArrayList<TempExModel> getAllTempExercisesWithSets() {
        ArrayList<TempExModel> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT " + TEMP_WORKOUT_EXERCISE_ID + ", " + TEMP_WORKOUT_EXERCISE_NAME + ", " +
                TEMP_WORKOUT_EXERCISE_TYPE + ", " + TEMP_WORKOUT_EXERCISE_BODY_TYPE + ", " +
                TEMP_WORKOUT_DATE + " FROM " + TEMP_WORKOUT_EXERCISE_TABLE;

        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(TEMP_WORKOUT_EXERCISE_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(TEMP_WORKOUT_EXERCISE_NAME));
            String type = cursor.getString(cursor.getColumnIndexOrThrow(TEMP_WORKOUT_EXERCISE_TYPE));
            String body = cursor.getString(cursor.getColumnIndexOrThrow(TEMP_WORKOUT_EXERCISE_BODY_TYPE));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(TEMP_WORKOUT_DATE));

            ArrayList<SetsModel> sets = getTempSetsByExercise(id);
            TempExModel model = new TempExModel(name, sets);
            model.setEx_id(id);
            model.setTypeEx(type);
            model.setBodyType(body);
            model.setData(date);
            list.add(model);
        }

        cursor.close();
        db.close();
        return list;
    }

    public void deleteTempSetAndRearrangeNumbers(int exerciseId, int setId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Удаляем конкретный сет по ID упражнения и номеру сета
        db.delete(TEMP_WORKOUT_SET_TABLE,
                TEMP_WORKOUT_SET_EXERCISE_ID + "=? AND " + TEMP_WORKOUT_SET_NUMBER + "=?",
                new String[]{String.valueOf(exerciseId), String.valueOf(setId)});

        // Обновляем номера сетов, которые были выше удалённого
        String updateQuery = "UPDATE " + TEMP_WORKOUT_SET_TABLE +
                " SET " + TEMP_WORKOUT_SET_NUMBER + " = " + TEMP_WORKOUT_SET_NUMBER + " - 1" +
                " WHERE " + TEMP_WORKOUT_SET_EXERCISE_ID + " = ? AND " + TEMP_WORKOUT_SET_NUMBER + " > ?";

        db.execSQL(updateQuery, new String[]{String.valueOf(exerciseId), String.valueOf(setId)});
        db.close();
    }

    // Удалить упражнение и все его сеты
    public void deleteTempExerciseWithSets(int exId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TEMP_WORKOUT_SET_TABLE, TEMP_WORKOUT_SET_EXERCISE_ID + "=?", new String[]{String.valueOf(exId)});
        db.delete(TEMP_WORKOUT_EXERCISE_TABLE, TEMP_WORKOUT_EXERCISE_ID + "=?", new String[]{String.valueOf(exId)});
        db.close();
    }

    // Обновить/вставить данные сета
    public void updateOrInsertTempSet(SetsModel set, int exerciseId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String query = "SELECT * FROM " + TEMP_WORKOUT_SET_TABLE +
                " WHERE " + TEMP_WORKOUT_SET_EXERCISE_ID + "=? AND " + TEMP_WORKOUT_SET_NUMBER + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(exerciseId), String.valueOf(set.getSet_id())});

        ContentValues values = new ContentValues();
        values.put(TEMP_WORKOUT_SET_WEIGHT, set.getWeight());
        values.put(TEMP_WORKOUT_SET_REP, set.getReps());

        if (cursor.moveToFirst()) {
            // update
            db.update(TEMP_WORKOUT_SET_TABLE, values,
                    TEMP_WORKOUT_SET_EXERCISE_ID + "=? AND " + TEMP_WORKOUT_SET_NUMBER + "=?",
                    new String[]{String.valueOf(exerciseId), String.valueOf(set.getSet_id())});
        } else {
            // insert
            values.put(TEMP_WORKOUT_SET_EXERCISE_ID, exerciseId);
            values.put(TEMP_WORKOUT_SET_NUMBER, set.getSet_id());
            values.put(TEMP_WORKOUT_SET_IS_SELECTED, 0);
            db.insert(TEMP_WORKOUT_SET_TABLE, null, values);
        }

        cursor.close();
        db.close();
    }

    public List<TempExModel> extractCompletedWorkoutsFromTemp() {
        List<TempExModel> validExercises = new ArrayList<>();

        List<TempExModel> tempList = getAllTempExercisesWithSets();

        for (TempExModel ex : tempList) {
            List<SetsModel> filteredSets = new ArrayList<>();
            for (SetsModel set : ex.getSetsList()) {
                if (set.getIsSelected() || (set.getReps() > 0 && set.getWeight() > 0)) {
                    filteredSets.add(set);
                }
            }

            if (!filteredSets.isEmpty()) {
                TempExModel filteredExercise = new TempExModel(ex.getExName(), filteredSets);
                filteredExercise.setEx_id(ex.getEx_id());
                filteredExercise.setTypeEx(ex.getTypeEx());
                filteredExercise.setBodyType(ex.getBodyType());
                filteredExercise.setData(ex.getData());
                validExercises.add(filteredExercise);
            }
        }

        return validExercises;
    }
    public void clearTempWorkoutData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(TEMP_WORKOUT_SET_TABLE, null, null);
        db.delete(TEMP_WORKOUT_EXERCISE_TABLE, null, null);

        // Сброс AUTOINCREMENT, если таблица упражнений пуста
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TEMP_WORKOUT_EXERCISE_TABLE, null);
        if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
            db.execSQL("DELETE FROM sqlite_sequence WHERE name='" + TEMP_WORKOUT_EXERCISE_TABLE + "'");
            Log.d("DB_RESET", "AUTOINCREMENT сброшен для временных упражнений");
        }

        cursor.close();
        db.close();
    }

    public void updateTempSetsExerciseId(int oldExId, int newExId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TEMP_WORKOUT_SET_EXERCISE_ID, newExId);
        db.update(TEMP_WORKOUT_SET_TABLE, cv, TEMP_WORKOUT_SET_EXERCISE_ID + "=?", new String[]{String.valueOf(oldExId)});
        db.close();
    }

    public void updateTempExerciseId(int oldId, int newId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TEMP_WORKOUT_EXERCISE_ID, newId);
        db.update(TEMP_WORKOUT_EXERCISE_TABLE, cv, TEMP_WORKOUT_EXERCISE_ID + "=?", new String[]{String.valueOf(oldId)});
        db.close();
    }


}