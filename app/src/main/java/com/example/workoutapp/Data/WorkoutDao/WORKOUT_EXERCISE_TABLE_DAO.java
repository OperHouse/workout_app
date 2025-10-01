package com.example.workoutapp.Data.WorkoutDao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingSource;
import androidx.paging.PagingState;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.WorkoutModels.CardioSetModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.Models.WorkoutModels.StrengthSetModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kotlin.coroutines.Continuation;

public class WORKOUT_EXERCISE_TABLE_DAO {

    private final AppDataBase dbHelper;
    // Добавляем DAO для сетов, чтобы получить к ним доступ
    private final STRENGTH_SET_DETAILS_TABLE_DAO strengthSetDao;
    private final CARDIO_SET_DETAILS_TABLE_DAO cardioSetDao;

    public WORKOUT_EXERCISE_TABLE_DAO(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
        this.strengthSetDao = new STRENGTH_SET_DETAILS_TABLE_DAO(dbHelper);
        this.cardioSetDao = new CARDIO_SET_DETAILS_TABLE_DAO(dbHelper);
    }

    public List<ExerciseModel> getExByState(String state) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
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
                    if (type.equalsIgnoreCase("Время") || type.equalsIgnoreCase( "Кардио")) {

                        // Запрашиваем кардио-сеты
                        List<CardioSetModel> cardioSets = cardioSetDao.getSetsForExercise(id);
                        sets.addAll(cardioSets);

                    } else{
                        // Запрашиваем силовые сеты
                        List<StrengthSetModel> strengthSets = strengthSetDao.getSetsForExercise(id);
                        sets.addAll(strengthSets);
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

    public void addExercise(String name, String type, String bodyType) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AppDataBase.WORKOUT_EXERCISE_NAME, name);
        values.put(AppDataBase.WORKOUT_EXERCISE_TYPE, type);
        values.put(AppDataBase.WORKOUT_EXERCISE_BODY_TYPE, bodyType);


        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
        values.put(AppDataBase.WORKOUT_EXERCISE_DATE, currentDate);


        values.put(AppDataBase.WORKOUT_EXERCISE_STATE, "unfinished");

        database.insert(AppDataBase.WORKOUT_EXERCISE_TABLE, null, values);
    }

    /**
     * Удаляет упражнение и все его подходы из базы данных.
     * @param exercise Объект ExerciseModel, который нужно удалить.
     */
    public void deleteExercise(ExerciseModel exercise) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        if (exercise == null) {
            return;
        }

        long exerciseId = exercise.getExercise_id();

        // Сначала удаляем все подходы, связанные с этим упражнением
        strengthSetDao.deleteSetsForExercise(exerciseId);
        cardioSetDao.deleteSetsForExercise(exerciseId);

        // Затем удаляем само упражнение
        database.delete(AppDataBase.WORKOUT_EXERCISE_TABLE,
                AppDataBase.WORKOUT_EXERCISE_ID + " = ?",
                new String[]{String.valueOf(exerciseId)});
    }

    public PagingSource<Integer, ExerciseModel> getExercisesPagingSource(String state) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        return new PagingSource<Integer, ExerciseModel>() {
            @Nullable
            @Override
            public Object load(@NonNull LoadParams<Integer> loadParams, @NonNull Continuation<? super LoadResult<Integer, ExerciseModel>> continuation) {
                return null;
            }

            @NonNull
            public PagingSource.LoadResult<Integer, ExerciseModel> load(@NonNull PagingSource.LoadParams<Integer> loadParams) {
                try {
                    Integer key = loadParams.getKey();
                    if (key == null) key = 0;

                    int offset = key * loadParams.getLoadSize();
                    int limit = loadParams.getLoadSize();

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
                                " WHERE " + AppDataBase.WORKOUT_EXERCISE_STATE + " = ?" +
                                " LIMIT ? OFFSET ?";

                        cursor = database.rawQuery(query, new String[]{state, String.valueOf(limit), String.valueOf(offset)});

                        if (cursor.moveToFirst()) {
                            do {
                                long id = cursor.getLong(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_ID));
                                String name = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_NAME));
                                String type = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_TYPE));
                                String bodyType = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_BODY_TYPE));
                                String date = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_DATE));
                                String currentState = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_EXERCISE_STATE));

                                // Важно: здесь мы НЕ загружаем сеты! Это делается позже, в адаптере
                                ExerciseModel exercise = new ExerciseModel(id, name, type, bodyType, date, currentState, new ArrayList<>());
                                exerciseList.add(exercise);

                            } while (cursor.moveToNext());
                        }

                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }

                    return new PagingSource.LoadResult.Page<>(
                            exerciseList,
                            loadParams.getKey(), // ключ для предыдущей страницы
                            (exerciseList.isEmpty() || exerciseList.size() < limit) ? null : key + 1 // ключ для следующей страницы
                    );
                } catch (Exception e) {
                    return new PagingSource.LoadResult.Error<>(e);
                }
            }

            @Override
            public Integer getRefreshKey(@NonNull PagingState<Integer, ExerciseModel> pagingState) {
                return null; // Упрощенная реализация, можно добавить логику для обновления
            }
        };
    }

    /**
     * Обновляет состояние упражнения на "finished".
     * @param exerciseId ID упражнения, которое нужно завершить.
     */
    public void markExerciseAsFinished(long exerciseId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AppDataBase.WORKOUT_EXERCISE_STATE, "finished");

        database.update(AppDataBase.WORKOUT_EXERCISE_TABLE,
                values,
                AppDataBase.WORKOUT_EXERCISE_ID + " = ?",
                new String[]{String.valueOf(exerciseId)});
    }

    // Метод getExByState больше не нужен, его функционал заменяет PagingSource
    // Если он используется в других частях, его можно оставить, но лучше заменить на пагинацию


}
