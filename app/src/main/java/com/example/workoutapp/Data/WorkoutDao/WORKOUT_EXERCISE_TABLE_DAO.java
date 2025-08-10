package com.example.workoutapp.Data.WorkoutDao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.Models.WorkoutModels.StrengthSetModel;
import com.example.workoutapp.Models.WorkoutModels.CardioSetModel;
import java.util.ArrayList;
import java.util.List;

public class WORKOUT_EXERCISE_TABLE_DAO {

    private final SQLiteDatabase database;
    // Добавляем DAO для сетов, чтобы получить к ним доступ
    private final STRENGTH_SET_DETAILS_TABLE_DAO strengthSetDao;
    private final CARDIO_SET_DETAILS_TABLE_DAO cardioSetDao;

    public WORKOUT_EXERCISE_TABLE_DAO(AppDataBase dbHelper) {
        this.database = dbHelper.getReadableDatabase();
        this.strengthSetDao = new STRENGTH_SET_DETAILS_TABLE_DAO(dbHelper);
        this.cardioSetDao = new CARDIO_SET_DETAILS_TABLE_DAO(dbHelper);
    }

    public List<ExerciseModel> getExByState(String state) {
        List<ExerciseModel> exerciseList = new ArrayList<>();
        Cursor cursor = null;

        try {
            String query = "SELECT " +
                    AppDataBase.WORKOUT_EXERCISE_ID + ", " +
                    AppDataBase.WORKOUT_EXERCISE_NAME + ", " +
                    AppDataBase.WORKOUT_EXERCISE_TYPE + ", " +
                    AppDataBase.WORKOUT_EXERCISE_BODY_TYPE + ", " +
                    AppDataBase.WORKOUT_EXERCISE_DATE + ", " +
                    AppDataBase.WORKOUT_EXERCISE_STATE +
                    " FROM " + AppDataBase.WORKOUT_EXERCISE_TABLE +
                    " WHERE " + AppDataBase.WORKOUT_EXERCISE_STATE + " = ?";

            cursor = database.rawQuery(query, new String[]{state});

            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_ID);
                int nameIndex = cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_NAME);
                int typeIndex = cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_TYPE);
                int bodyTypeIndex = cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_BODY_TYPE);
                int dateIndex = cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_DATE);
                int stateIndex = cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_STATE);

                do {
                    long id = cursor.getLong(idIndex);
                    String name = cursor.getString(nameIndex);
                    String type = cursor.getString(typeIndex);
                    String bodyType = cursor.getString(bodyTypeIndex);
                    String date = cursor.getString(dateIndex);
                    String currentState = cursor.getString(stateIndex);

                    // Создаем пустой список для сетов
                    List<Object> sets = new ArrayList<>();

                    // Проверяем тип упражнения и заполняем соответствующий список сетов
                    if (type.equalsIgnoreCase("strength")) {
                        // Запрашиваем силовые сеты
                        List<StrengthSetModel> strengthSets = strengthSetDao.getSetsForExercise(id);
                        sets.addAll(strengthSets);
                    } else if (type.equalsIgnoreCase("cardio")) {
                        // Запрашиваем кардио-сеты
                        List<CardioSetModel> cardioSets = cardioSetDao.getSetsForExercise(id);
                        sets.addAll(cardioSets);
                    }

                    // Создаем ExerciseModel и передаем заполненный список сетов
                    ExerciseModel exercise = new ExerciseModel(id, name, type, bodyType, date, currentState, sets);
                    exerciseList.add(exercise);

                } while (cursor.moveToNext());
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return exerciseList;
    }
}