package com.example.workoutapp;

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
    public static final String EXERCISE_DATE = "data";

    //=========================SetsData===========================//
    public static final String TABLE_SETS = "sets";
    public static final String SET_EXERCISE_ID = "exercise_id";
    public static final String SET_NUMBER = "set_number";
    public static final String SET_WEIGHT = "weight";
    public static final String SET_REPS = "reps";

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
                EXERCISE_DATE + " TEXT NOT NULL);";

        // SQL-запрос для создания таблицы сетов
        String createSetsTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_SETS + " (" +
                SET_EXERCISE_ID + " INTEGER NOT NULL, " +
                SET_NUMBER + " INTEGER NOT NULL, " +
                SET_WEIGHT + " REAL, " +
                SET_REPS + " INTEGER, " +
                "PRIMARY KEY(" + SET_EXERCISE_ID + ", " + SET_NUMBER + "), " +
                "FOREIGN KEY(" + SET_EXERCISE_ID + ") REFERENCES " + TABLE_EXERCISES + "(" + EXERCISE_ID + "));";

        // Выполнение запросов на создание таблиц
        db.execSQL(createExercisesTableQuery);
        db.execSQL(createSetsTableQuery);
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
        String query = "SELECT set_number, weight, reps FROM sets WHERE exercise_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(exerciseId)});

        if (cursor != null) {
            // Перебираем курсор и заполняем список SetsModel
            while (cursor.moveToNext()) {
                @SuppressLint("Range") int setNumber = cursor.getInt(cursor.getColumnIndex("set_number"));
                @SuppressLint("Range") double weight = cursor.getDouble(cursor.getColumnIndex("weight"));
                @SuppressLint("Range") int reps = cursor.getInt(cursor.getColumnIndex("reps"));

                // Создаем объект SetsModel и заполняем его данными
                SetsModel set = new SetsModel();
                set.setSet(setNumber);  // Устанавливаем номер подхода
                set.setWeight(weight);  // Устанавливаем вес
                set.setReps(reps);      // Устанавливаем количество повторений

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
        // Insert the new set into the database
        db.insert("sets", null, values);
        db.close();
    }

    // Метод для добавления нового упражнения в таблицу exercises
    public void addExercise(String exerciseName, String exType) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Получаем текущую дату
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(new Date());  // Текущая дата в формате "yyyy-MM-dd"

        // Подготовка данных для вставки
        ContentValues values = new ContentValues();
        values.put(EXERCISE_NAME, exerciseName);  // Используем константу для имени столбца
        values.put(EXERCISE_TYPE, exType);  // Используем константу для типа упражнения
        values.put(EXERCISE_DATE, currentDate);  // Используем константу для даты

        // Вставка данных в таблицу exercises
        db.insert(TABLE_EXERCISES, null, values);
        db.close();  // Закрытие базы данных
    }

    public ArrayList<TempExModel> getAllExercisesWithSets() {
        ArrayList<TempExModel> exerciseList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // SQL-запрос для получения всех упражнений
        String queryExercises = "SELECT " + EXERCISE_ID + ", " + EXERCISE_NAME + ", " + EXERCISE_TYPE + ", " + EXERCISE_DATE + " FROM " + TABLE_EXERCISES;
        Cursor cursorExercises = db.rawQuery(queryExercises, null);

        if (cursorExercises != null && cursorExercises.moveToFirst()) {
            do {
                // Извлекаем данные упражнения с использованием констант
                @SuppressLint("Range") long exerciseId = cursorExercises.getLong(cursorExercises.getColumnIndex(EXERCISE_ID));
                @SuppressLint("Range") String exerciseName = cursorExercises.getString(cursorExercises.getColumnIndex(EXERCISE_NAME));
                @SuppressLint("Range") String exType = cursorExercises.getString(cursorExercises.getColumnIndex(EXERCISE_TYPE));
                @SuppressLint("Range") String dateAdded = cursorExercises.getString(cursorExercises.getColumnIndex(EXERCISE_DATE));

                // Извлекаем сеты для этого упражнения
                List<SetsModel> setsList = new ArrayList<>();
                String querySets = "SELECT " + SET_NUMBER + ", " + SET_WEIGHT + ", " + SET_REPS + " FROM " + TABLE_SETS + " WHERE " + SET_EXERCISE_ID + " = ?";
                Cursor cursorSets = db.rawQuery(querySets, new String[]{String.valueOf(exerciseId)});

                if (cursorSets != null && cursorSets.moveToFirst()) {
                    do {
                        // Извлекаем данные для каждого сета
                        @SuppressLint("Range") int setNumber = cursorSets.getInt(cursorSets.getColumnIndex(SET_NUMBER));
                        @SuppressLint("Range") double weight = cursorSets.getDouble(cursorSets.getColumnIndex(SET_WEIGHT));
                        @SuppressLint("Range") int reps = cursorSets.getInt(cursorSets.getColumnIndex(SET_REPS));

                        // Создаем объект SetsModel и добавляем его в список
                        SetsModel set = new SetsModel();
                        set.setSet(setNumber);
                        set.setWeight(weight);
                        set.setReps(reps);
                        setsList.add(set);
                    } while (cursorSets.moveToNext());
                    cursorSets.close(); // Закрываем курсор для сетов
                }

                // Создаем объект TempExModel и заполняем его
                TempExModel tempExModel = new TempExModel(exerciseName, setsList);
                tempExModel.setEx_id((int) exerciseId);  // Устанавливаем ID упражнения
                tempExModel.setData(dateAdded);
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

    //===================================================Check-Data===============================//

    public void logAllExercisesAndSets() {
        SQLiteDatabase db = this.getReadableDatabase();

        // Проверка, существует ли таблица упражнений
        if (checkIfTableExists(db)) {
            // SQL-запрос для получения всех упражнений
            String queryExercises = "SELECT " + EXERCISE_ID + ", " + EXERCISE_NAME + ", " + EXERCISE_TYPE + ", " + EXERCISE_DATE + " FROM " + TABLE_EXERCISES;
            Cursor cursorExercises = db.rawQuery(queryExercises, null);

            if (cursorExercises != null && cursorExercises.moveToFirst()) {
                do {
                    // Извлекаем данные упражнения с использованием констант
                    @SuppressLint("Range") long exerciseId = cursorExercises.getLong(cursorExercises.getColumnIndex(EXERCISE_ID));
                    @SuppressLint("Range") String exerciseName = cursorExercises.getString(cursorExercises.getColumnIndex(EXERCISE_NAME));
                    @SuppressLint("Range") String exType = cursorExercises.getString(cursorExercises.getColumnIndex(EXERCISE_TYPE));
                    @SuppressLint("Range") String dateAdded = cursorExercises.getString(cursorExercises.getColumnIndex(EXERCISE_DATE));

                    // Логируем информацию о упражнении
                    Log.d("Exercise", "ID: " + exerciseId + ", Name: " + exerciseName + ", Type: " + exType + ", Date: " + dateAdded);

                    // Извлекаем сеты для этого упражнения
                    String querySets = "SELECT " + SET_NUMBER + ", " + SET_WEIGHT + ", " + SET_REPS + " FROM " + TABLE_SETS + " WHERE " + SET_EXERCISE_ID + " = ?";
                    Cursor cursorSets = db.rawQuery(querySets, new String[]{String.valueOf(exerciseId)});

                    if (cursorSets != null && cursorSets.moveToFirst()) {
                        do {
                            // Извлекаем данные для каждого сета
                            @SuppressLint("Range") int setNumber = cursorSets.getInt(cursorSets.getColumnIndex(SET_NUMBER));
                            @SuppressLint("Range") double weight = cursorSets.getDouble(cursorSets.getColumnIndex(SET_WEIGHT));
                            @SuppressLint("Range") int reps = cursorSets.getInt(cursorSets.getColumnIndex(SET_REPS));

                            // Логируем информацию о подходе
                            Log.d("Set", "Exercise ID: " + exerciseId + ", Set Number: " + setNumber + ", Weight: " + weight + ", Reps: " + reps);
                        } while (cursorSets.moveToNext());
                        cursorSets.close(); // Закрываем курсор для сетов
                    }
                } while (cursorExercises.moveToNext());
                cursorExercises.close(); // Закрываем курсор для упражнений
            }
        }

        db.close(); // Закрываем базу данных
    }

    private boolean checkIfTableExists(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{TempDataBaseEx.TABLE_EXERCISES});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}
