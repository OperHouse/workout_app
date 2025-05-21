package com.example.workoutapp.Data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.workoutapp.Models.SetsModel;
import com.example.workoutapp.Models.TempExModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TempDataBaseEx extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "exerciseDB";
    private static final int DATABASE_VERSION = 1;

    //=========================ExData============================//
    public static final String TABLE_EXERCISES = "exercises";
    public static final String EXERCISE_ID = "id";
    public static final String EXERCISE_NAME = "nameEx";
    public static final String EXERCISE_TYPE = "exType";
    public static final String BODY_TYPE = "bodyType";
    public static final String EXERCISE_DATE = "data";

    //=========================SetsData===========================//
    public static final String TABLE_SETS = "sets";
    public static final String SET_EXERCISE_ID = "exercise_id";
    public static final String SET_NUMBER = "set_number";
    public static final String SET_WEIGHT = "weight";
    public static final String SET_REPS = "reps";
    public static final String SET_IS_SELECTED = "is_selected";

    public TempDataBaseEx(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL-запрос для создания таблицы упражнений
        String createExercisesTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_EXERCISES + " (" +
                EXERCISE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EXERCISE_NAME + " TEXT NOT NULL, " +
                EXERCISE_TYPE + " TEXT NOT NULL, " +
                BODY_TYPE + " TEXT NOT NULL, " +
                EXERCISE_DATE + " TEXT NOT NULL);";

        // SQL-запрос для создания таблицы сетов
        String createSetsTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_SETS + " (" +
                SET_EXERCISE_ID + " INTEGER NOT NULL, " +
                SET_NUMBER + " INTEGER NOT NULL, " +
                SET_WEIGHT + " INTEGER, " +
                SET_REPS + " INTEGER, " +
                SET_IS_SELECTED +" INTEGER NOT NULL DEFAULT 0, " +
                "PRIMARY KEY(" + SET_EXERCISE_ID + ", " + SET_NUMBER + "), " +
                "FOREIGN KEY(" + SET_EXERCISE_ID + ") REFERENCES " + TABLE_EXERCISES + "(" + EXERCISE_ID + "));";

        // Таблица завершённых тренировок
        String createCompletedWorkoutTable = "CREATE TABLE IF NOT EXISTS workouts_offline (" +
                "workout_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nameEx TEXT NOT NULL, " +
                "exType TEXT NOT NULL, " +
                "bodyType TEXT NOT NULL, " +
                "date TEXT NOT NULL);";

// Таблица сетов для завершённых тренировок
        String createCompletedSetsTable = "CREATE TABLE IF NOT EXISTS sets_offline (" +
                "workout_id INTEGER NOT NULL," +
                "set_number INTEGER NOT NULL," +
                "weight INTEGER," +
                "reps INTEGER," +
                "is_selected INTEGER DEFAULT 0," +
                "PRIMARY KEY(workout_id, set_number)," +
                "FOREIGN KEY(workout_id) REFERENCES workouts_offline(workout_id));";

        // Выполнение запросов на создание таблиц
        db.execSQL(createExercisesTableQuery);
        db.execSQL(createSetsTableQuery);

        // Выполняем создание
        db.execSQL(createCompletedWorkoutTable);
        db.execSQL(createCompletedSetsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Удаляем старые таблицы, если они существуют, и создаем новые
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISES);
        onCreate(db);  // Воссоздаем таблицы с актуальной схемой
    }

    //============================================================================================//

    // Метод для получения всех подходов для конкретного упражнения
    public ArrayList<SetsModel> getExerciseSets(long exerciseId) {
        ArrayList<SetsModel> setsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // SQL-запрос для получения подходов для конкретного упражнения
        String query = "SELECT set_number, weight, reps, is_selected FROM sets WHERE exercise_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(exerciseId)});

        if (cursor != null) {
            // Перебираем курсор и заполняем список SetsModel
            while (cursor.moveToNext()) {
                @SuppressLint("Range") int setNumber = cursor.getInt(cursor.getColumnIndex("set_number"));
                @SuppressLint("Range") int weight = cursor.getInt(cursor.getColumnIndex("weight"));
                @SuppressLint("Range") int reps = cursor.getInt(cursor.getColumnIndex("reps"));
                @SuppressLint("Range") int isSelected = cursor.getInt(cursor.getColumnIndex("is_selected"));

                // Создаем объект SetsModel и заполняем его данными
                SetsModel set = new SetsModel();
                set.setSet_id(setNumber);  // Устанавливаем номер подхода
                set.setWeight(weight);  // Устанавливаем вес
                set.setReps(reps);      // Устанавливаем количество повторений
                set.setIsSelected(isSelected == 1);
                // Добавляем объект в список
                setsList.add(set);
            }
            cursor.close(); // Закрываем курсор
        }

        db.close(); // Закрываем базу данных
        return setsList;  // Возвращаем список объектов SetsModel
    }

    // Метод для добавления нового подхода в таблицу sets
    public void addSet(int exerciseId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // First, get the highest set_number for the given exercise_id
        String query = "SELECT MAX(set_number) FROM sets WHERE exercise_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(exerciseId)});

        int setNumber = 1; // Default set number is 1 if no sets are found for the exercise

        if (cursor != null && cursor.moveToFirst()) {
            // Get the highest set_number and increment it by 1
            setNumber = cursor.getInt(0) + 1;
            cursor.close();
        }

        // Prepare the data to insert into the 'sets' table
        ContentValues values = new ContentValues();
        values.put("exercise_id", exerciseId);
        values.put("set_number", setNumber);
        values.putNull("weight");  // Use putNull() to insert NULL for weight
        values.putNull("reps");
        values.put("is_selected", 0);
        // Insert the new set into the database
        db.insert("sets", null, values);
        db.close();
    }

    public void updateIsSelected(int exerciseId, int setNumber, boolean isSelected) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            // Подготовка данных для обновления
            ContentValues values = new ContentValues();
            values.put(SET_IS_SELECTED, isSelected ? 1 : 0);  // Преобразуем boolean в 1 или 0

            // Выполнение обновления
            int rowsUpdated = db.update(TABLE_SETS, values,
                    SET_EXERCISE_ID + " = ? AND " + SET_NUMBER + " = ?",
                    new String[]{String.valueOf(exerciseId), String.valueOf(setNumber)});

            // Логируем количество обновленных строк
            Log.d("DB_UPDATE", "Rows updated: " + rowsUpdated);
        } catch (Exception e) {
            Log.e("DB_UPDATE", "Error updating isSelected", e);
        } finally {
            db.close();
        }
    }

    // Метод для добавления нового упражнения в таблицу exercises
    public void addExercise(String exerciseName, String exType, String bodyType) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Получаем текущую дату
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(new Date());  // Текущая дата в формате "yyyy-MM-dd"

        // Подготовка данных для вставки
        ContentValues values = new ContentValues();
        values.put(EXERCISE_NAME, exerciseName);  // Используем константу для имени столбца
        values.put(EXERCISE_TYPE, exType);  // Используем константу для типа упражнения
        values.put(BODY_TYPE, bodyType);
        values.put(EXERCISE_DATE, currentDate);  // Используем константу для даты

        // Вставка данных в таблицу exercises
        db.insert(TABLE_EXERCISES, null, values);
        db.close();  // Закрытие базы данных
    }

    public ArrayList<TempExModel> getAllExercisesWithSets() {
        ArrayList<TempExModel> exerciseList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // SQL-запрос для получения всех упражнений
        String queryExercises = "SELECT " + EXERCISE_ID + ", " + EXERCISE_NAME + ", " + EXERCISE_TYPE + ", " + BODY_TYPE + ", " + EXERCISE_DATE + " FROM " + TABLE_EXERCISES;
        Cursor cursorExercises = db.rawQuery(queryExercises, null);

        if (cursorExercises != null && cursorExercises.moveToFirst()) {
            do {
                // Извлекаем данные упражнения с использованием констант
                @SuppressLint("Range") long exerciseId = cursorExercises.getLong(cursorExercises.getColumnIndex(EXERCISE_ID));
                @SuppressLint("Range") String exerciseName = cursorExercises.getString(cursorExercises.getColumnIndex(EXERCISE_NAME));
                @SuppressLint("Range") String exType = cursorExercises.getString(cursorExercises.getColumnIndex(EXERCISE_TYPE));
                @SuppressLint("Range") String bodyType = cursorExercises.getString(cursorExercises.getColumnIndex(BODY_TYPE));
                @SuppressLint("Range") String dateAdded = cursorExercises.getString(cursorExercises.getColumnIndex(EXERCISE_DATE));

                // Извлекаем сеты для этого упражнения
                List<SetsModel> setsList = new ArrayList<>();
                String querySets = "SELECT " + SET_NUMBER + ", " + SET_WEIGHT + ", " + SET_REPS + ", " + SET_IS_SELECTED + " FROM " + TABLE_SETS + " WHERE " + SET_EXERCISE_ID + " = ?";
                Cursor cursorSets = db.rawQuery(querySets, new String[]{String.valueOf(exerciseId)});

                if (cursorSets != null && cursorSets.moveToFirst()) {
                    do {
                        // Извлекаем данные для каждого сета
                        @SuppressLint("Range") int setNumber = cursorSets.getInt(cursorSets.getColumnIndex(SET_NUMBER));
                        @SuppressLint("Range") int weight = cursorSets.getInt(cursorSets.getColumnIndex(SET_WEIGHT));
                        @SuppressLint("Range") int reps = cursorSets.getInt(cursorSets.getColumnIndex(SET_REPS));
                        @SuppressLint("Range") int isSelected = cursorSets.getInt(cursorSets.getColumnIndex(SET_IS_SELECTED));

                        // Создаем объект SetsModel и добавляем его в список
                        SetsModel set = new SetsModel();
                        set.setSet_id(setNumber);
                        set.setWeight(weight);
                        set.setReps(reps);
                        set.setIsSelected(isSelected == 1);
                        setsList.add(set);
                    } while (cursorSets.moveToNext());
                    cursorSets.close(); // Закрываем курсор для сетов
                }

                // Создаем объект TempExModel и заполняем его
                TempExModel tempExModel = new TempExModel(exerciseName, setsList);
                tempExModel.setEx_id((int) exerciseId);  // Устанавливаем ID упражнения
                tempExModel.setData(dateAdded);
                tempExModel.setBodyType(bodyType);
                tempExModel.setTypeEx(exType);

                // Добавляем объект TempExModel в список
                exerciseList.add(tempExModel);
            } while (cursorExercises.moveToNext());
            cursorExercises.close(); // Закрываем курсор для упражнений
        }

        db.close(); // Закрываем базу данных
        return exerciseList; // Возвращаем список упражнений с их подходами
    }

    public boolean checkIfExerciseExists(String exerciseName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM exercises WHERE nameEx = ?";
        Cursor cursor = db.rawQuery(query, new String[]{exerciseName});

        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();

        return exists;
    }

    public void deleteSetAndRearrangeNumbers(int exerciseId, int setId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 1. Удаляем конкретный сет по ID упражнения и номеру сета
        db.delete(TABLE_SETS, "exercise_id=? AND set_number=?", new String[]{String.valueOf(exerciseId), String.valueOf(setId)});

        // 2. Перенумеровываем все оставшиеся сеты с номерами больше удаленного
        String updateQuery = "UPDATE " + TABLE_SETS +
                " SET set_number = set_number - 1" +
                " WHERE exercise_id = ? AND set_number > ?";

        // Выполняем обновление всех сетов, чьи номера больше удаленного
        db.execSQL(updateQuery, new String[]{String.valueOf(exerciseId), String.valueOf(setId)});

        db.close();
    }

    public void updateOrInsertSet(SetsModel set, int exerciseId) {
        try {
            try (SQLiteDatabase db = this.getWritableDatabase()) {
                Log.d("DB_UPDATE", "Start updating or inserting set");


                // Проверка, существует ли уже запись с таким упражнением и номером сета
                String query = "SELECT * FROM " + TempDataBaseEx.TABLE_SETS +
                        " WHERE " + TempDataBaseEx.SET_EXERCISE_ID + " = ?" +
                        " AND " + TempDataBaseEx.SET_NUMBER + " = ?";
                Cursor cursor = db.rawQuery(query, new String[]{
                        String.valueOf(exerciseId), String.valueOf(set.getSet_id())
                });

                if (cursor.moveToFirst()) {
                    Log.d("DB_UPDATE", "Record exists...");

                    Log.d("DB_UPDATE", "Updating record...");
                    ContentValues values = new ContentValues();
                    values.put(TempDataBaseEx.SET_WEIGHT, set.getWeight());
                    values.put(TempDataBaseEx.SET_REPS, set.getReps());
                    db.update(TempDataBaseEx.TABLE_SETS, values,
                            TempDataBaseEx.SET_EXERCISE_ID + " = ? AND " + TempDataBaseEx.SET_NUMBER + " = ?",
                            new String[]{String.valueOf(exerciseId), String.valueOf(set.getSet_id())});
                } else {
                    Log.d("DB_UPDATE", "Record not found, inserting...");
                    ContentValues values = new ContentValues();
                    values.put(TempDataBaseEx.SET_EXERCISE_ID, exerciseId);
                    values.put(TempDataBaseEx.SET_NUMBER, set.getSet_id());
                    values.put(TempDataBaseEx.SET_WEIGHT, set.getWeight());
                    values.put(TempDataBaseEx.SET_REPS, set.getReps());
                    db.insert(TempDataBaseEx.TABLE_SETS, null, values);

                }

                cursor.close();
            }
        } catch (Exception e) {
            Log.e("DB_UPDATE", "Error updating or inserting set", e);
        }
    }

    // Удалить упражнение и все его сеты
    public void deleteExerciseWithSets(int exId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SETS, SET_EXERCISE_ID + "=?", new String[]{String.valueOf(exId)});
        db.delete(TABLE_EXERCISES, EXERCISE_ID + "=?", new String[]{String.valueOf(exId)});
        db.close();
    }

    // Обновить ID упражнения
    public void updateExerciseId(int oldId, int newId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(EXERCISE_ID, newId);
        db.update(TABLE_EXERCISES, cv, EXERCISE_ID + "=?", new String[]{String.valueOf(oldId)});
        db.close();
    }

    // Обновить ID у всех сетов, связанных с упражнением
    public void updateSetsExerciseId(int oldExId, int newExId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(SET_EXERCISE_ID, newExId);
        db.update(TABLE_SETS, cv, SET_EXERCISE_ID + "=?", new String[]{String.valueOf(oldExId)});
        db.close();
    }

    public void moveTempToOfflineAndClear() {
        List<TempExModel> tempList = getAllExercisesWithSets();
        SQLiteDatabase db = this.getWritableDatabase();

        for (TempExModel ex : tempList) {

            // Фильтруем сеты с валидными значениями
            List<SetsModel> filteredSets = new ArrayList<>();
            for (SetsModel set : ex.getSetsList()) {
                if(set.getIsSelected() || (set.getReps() > 0 && set.getWeight() > 0)) {
                    filteredSets.add(set);
                }
            }

            if (!filteredSets.isEmpty()) {
                // Вставляем упражнение в offline-таблицу
                ContentValues workoutValues = new ContentValues();
                workoutValues.put("nameEx", ex.getExName());
                workoutValues.put("exType", ex.getTypeEx());
                workoutValues.put("bodyType", ex.getBodyType());
                workoutValues.put("date", ex.getData());

                long workoutId = db.insert("workouts_offline", null, workoutValues);

                // Вставляем сеты
                for (SetsModel set : filteredSets) {
                    ContentValues setValues = new ContentValues();
                    setValues.put("workout_id", workoutId);
                    setValues.put("set_number", set.getSet_id());
                    setValues.put("weight", set.getWeight());
                    setValues.put("reps", set.getReps());
                    setValues.put("is_selected", set.getIsSelected() ? 1 : 0);
                    db.insert("sets_offline", null, setValues);
                }
            }
        }
        // Чистим временные таблицы
        db.delete(TABLE_SETS, null, null);
        db.delete(TABLE_EXERCISES, null, null);
        resetExerciseIdSequenceIfEmpty();
        //resetSetsOfflineIdSequenceIfEmpty();
        db.close();
    }



    public void resetExerciseIdSequenceIfEmpty() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Проверяем, пустая ли таблица упражнений
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_EXERCISES, null);
        if (cursor != null && cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            cursor.close();

            if (count == 0) {
                // Сбрасываем sequence (начнёт с 1)
                db.execSQL("DELETE FROM sqlite_sequence WHERE name='" + TABLE_EXERCISES + "'");
                Log.d("DB_RESET", "AUTOINCREMENT для exercises сброшен");
            }
        }


    }

    public void clearOfflineWorkouts() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("sets_offline", null, null);
        db.delete("workouts_offline", null, null);
        db.close();
    }
    /*public void resetSetsOfflineIdSequenceIfEmpty() {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SETS, null);
        if (cursor != null && cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            cursor.close();

            if (count == 0) {
                db.execSQL("DELETE FROM sqlite_sequence WHERE name='" + TABLE_SETS + "'");
                Log.d("DB_RESET", "AUTOINCREMENT для sets сброшен");
            }
        }
    }*/ //Лишняя логика получается. Надо будет удалить, но пока оставил, мб пригодится

    //===================================================Check-Data===============================//

    public void logAllExercisesAndSets() {
        SQLiteDatabase db = this.getReadableDatabase();

        // Проверка, существует ли таблица упражнений
        if (checkIfTableExists(db)) {
            // SQL-запрос для получения всех упражнений
            //String queryExercises = "SELECT " + SET_NUMBER + ", " + SET_WEIGHT + ", " + SET_REPS + ", " + SET_IS_SELECTED + " FROM " + TABLE_SETS + " WHERE " + SET_EXERCISE_ID + " = ?";
            String queryExercises = "SELECT * FROM " + TABLE_EXERCISES;
            Cursor cursorExercises = db.rawQuery(queryExercises, null);

            if (cursorExercises != null && cursorExercises.moveToFirst()) {
                do {
                    // Извлекаем данные упражнения с использованием констант
                    @SuppressLint("Range") long exerciseId = cursorExercises.getLong(cursorExercises.getColumnIndex(EXERCISE_ID));
                    @SuppressLint("Range") String exerciseName = cursorExercises.getString(cursorExercises.getColumnIndex(EXERCISE_NAME));
                    @SuppressLint("Range") String exType = cursorExercises.getString(cursorExercises.getColumnIndex(EXERCISE_TYPE));
                    @SuppressLint("Range") String bodyType = cursorExercises.getString(cursorExercises.getColumnIndex(BODY_TYPE));
                    @SuppressLint("Range") String dateAdded = cursorExercises.getString(cursorExercises.getColumnIndex(EXERCISE_DATE));

                    // Логируем информацию о упражнении
                    Log.d("Exercise", "ID: " + exerciseId + ", Name: " + exerciseName + ", ExType: " + exType +  ", BodyType: " + bodyType + ", Date: " + dateAdded);

                    // Извлекаем сеты для этого упражнения
                    //String querySets = "SELECT " + SET_NUMBER + ", " + SET_WEIGHT + ", " + SET_REPS + ", " + SET_IS_SELECTED + " FROM " + TABLE_SETS + " WHERE " + SET_EXERCISE_ID + " = ?";
                    String querySets = "SELECT * FROM " + TABLE_SETS + " WHERE " + SET_EXERCISE_ID + " = ?";
                    Cursor cursorSets = db.rawQuery(querySets, new String[]{String.valueOf(exerciseId)});

                    if (cursorSets != null && cursorSets.moveToFirst()) {
                        do {
                            // Извлекаем данные для каждого сета
                            @SuppressLint("Range") int setNumber = cursorSets.getInt(cursorSets.getColumnIndex(SET_NUMBER));
                            @SuppressLint("Range") int weight = cursorSets.getInt(cursorSets.getColumnIndex(SET_WEIGHT));
                            @SuppressLint("Range") int reps = cursorSets.getInt(cursorSets.getColumnIndex(SET_REPS));
                            @SuppressLint("Range") int isSelected = cursorSets.getInt(cursorSets.getColumnIndex(SET_IS_SELECTED));

                            // Логируем информацию о подходе
                            Log.d("Set", "Exercise ID: " + exerciseId + ", Set Number: " + setNumber + ", Weight: " + weight + ", Reps: " + reps + ", IsSelected: " + isSelected);
                        } while (cursorSets.moveToNext());
                        cursorSets.close(); // Закрываем курсор для сетов
                    }
                } while (cursorExercises.moveToNext());
                cursorExercises.close(); // Закрываем курсор для упражнений
            }
        }

        db.close(); // Закрываем базу данных
    }

    public void logAllOfflineWorkoutsAndSets() {
        SQLiteDatabase db = this.getReadableDatabase();

        String queryWorkouts = "SELECT * FROM workouts_offline";
        Cursor cursorWorkouts = db.rawQuery(queryWorkouts, null);

        if (cursorWorkouts != null && cursorWorkouts.moveToFirst()) {
            do {
                @SuppressLint("Range") int workoutId = cursorWorkouts.getInt(cursorWorkouts.getColumnIndex("workout_id"));
                @SuppressLint("Range") String exerciseName = cursorWorkouts.getString(cursorWorkouts.getColumnIndex("nameEx"));
                @SuppressLint("Range") String exType = cursorWorkouts.getString(cursorWorkouts.getColumnIndex("exType"));
                @SuppressLint("Range") String bodyType = cursorWorkouts.getString(cursorWorkouts.getColumnIndex("bodyType"));
                @SuppressLint("Range") String date = cursorWorkouts.getString(cursorWorkouts.getColumnIndex("date"));

                Log.d("OFFLINE_WORKOUT", "ID: " + workoutId + ", Name: " + exerciseName + ", ExType: " + exType + ", BodyType: " + bodyType + ", Date: " + date);

                // Получаем сеты для этой тренировки
                String querySets = "SELECT * FROM sets_offline WHERE workout_id = ?";
                Cursor cursorSets = db.rawQuery(querySets, new String[]{String.valueOf(workoutId)});

                if (cursorSets != null && cursorSets.moveToFirst()) {
                    do {
                        @SuppressLint("Range") int setNumber = cursorSets.getInt(cursorSets.getColumnIndex("set_number"));
                        @SuppressLint("Range") int weight = cursorSets.getInt(cursorSets.getColumnIndex("weight"));
                        @SuppressLint("Range") int reps = cursorSets.getInt(cursorSets.getColumnIndex("reps"));
                        @SuppressLint("Range") int isSelected = cursorSets.getInt(cursorSets.getColumnIndex("is_selected"));

                        Log.d("OFFLINE_SET", "Workout ID: " + workoutId + ", Set Number: " + setNumber + ", Weight: " + weight + ", Reps: " + reps + ", IsSelected: " + isSelected);
                    } while (cursorSets.moveToNext());
                    cursorSets.close();
                }

            } while (cursorWorkouts.moveToNext());
            cursorWorkouts.close();
        } else {
            Log.d("OFFLINE_LOG", "Таблица workouts_offline пуста");
        }

        db.close();
    }


    private boolean checkIfTableExists(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{TempDataBaseEx.TABLE_EXERCISES});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}
