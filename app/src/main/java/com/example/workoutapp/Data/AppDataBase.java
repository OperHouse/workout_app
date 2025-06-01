package com.example.workoutapp.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppDataBase extends SQLiteOpenHelper {

    private static final String DB_NAME = "WorkoutApp.db";
    private static final int DB_VERSION = 1;

    private static AppDataBase instance;


    //=========================EXERCISE_TABLE============================//
    public static final String EXERCISE_TABLE = "exercise_table";
    public static final String EXERCISE_NAME = "exercise_name";
    public static final String EXERCISE_TYPE = "exercise_type";
    public static final String EXERCISE_BODY_TYPE = "exercise_body_type";

    //=========================PRESET_TABLE=======================//
    public static final String PRESET_TABLE = "preset_table";
    public static final String PRESET_NAME = "preset_name";
    public static final String PRESET_EXERCISE_NAME = "preset_exercise_name";
    public static final String PRESET_EXERCISE_TYPE = "preset_exercise_type";
    public static final String PRESET_EXERCISE_BODY_TYPE = "preset_exercise_body_type";

    //=========================TEMP_WORKOUT_EXERCISE_TABLE============================//
    public static final String TEMP_WORKOUT_EXERCISE_TABLE = "temp_workout_exercise_table";
    public static final String TEMP_WORKOUT_EXERCISE_ID = "temp_workout_exercise_id";
    public static final String TEMP_WORKOUT_EXERCISE_NAME = "temp_workout_exercise_name";
    public static final String TEMP_WORKOUT_EXERCISE_TYPE = "temp_workout_exercise_type";
    public static final String TEMP_WORKOUT_EXERCISE_BODY_TYPE = "temp_workout_exercise_body_type";
    public static final String TEMP_WORKOUT_DATE = "temp_workout_data";

    //=========================TEMP_WORKOUT_SET_TABLE===========================//
    public static final String TEMP_WORKOUT_SET_TABLE = "temp_workout_set_table";
    public static final String TEMP_WORKOUT_SET_EXERCISE_ID = "temp_workout_set_exercise_id";
    public static final String TEMP_WORKOUT_SET_NUMBER = "temp_workout_set_number";
    public static final String TEMP_WORKOUT_SET_WEIGHT = "temp_workout_set_weight";
    public static final String TEMP_WORKOUT_SET_REP = "temp_workout_set_rep";
    public static final String TEMP_WORKOUT_SET_IS_SELECTED = "temp_workout_set_is_selected";

    //=========================COMPLETED_WORKOUT_TABLE============================//
    public static final String COMPLETED_WORKOUT_EXERCISE_TABLE = "completed_workout_exercise_table";
    public static final String COMPLETED_WORKOUT_EXERCISE_ID = "completed_workout_exercise_id";
    public static final String COMPLETED_WORKOUT_EXERCISE_NAME = "completed_workout_exercise_name";
    public static final String COMPLETED_WORKOUT_EXERCISE_TYPE = "completed_workout_exercise_type";
    public static final String COMPLETED_WORKOUT_EXERCISE_BODY_TYPE = "completed_workout_exercise_body_type";
    public static final String COMPLETED_WORKOUT_DATE = "completed_workout_data";

    //=========================COMPLETED_WORKOUT_SET_TABLE===============================//
    public static final String COMPLETED_WORKOUT_SET_TABLE = "completed_workout_set_table";
    public static final String COMPLETED_WORKOUT_SET_EXERCISE_ID = "completed_workout_set_exercise_id";
    public static final String COMPLETED_WORKOUT_SET_NUMBER = "completed_workout_set_number";
    public static final String COMPLETED_WORKOUT_SET_WEIGHT = "completed_workout_set_weight";
    public static final String COMPLETED_WORKOUT_SET_REP = "completed_workout_set_rep";
    public static final String COMPLETED_WORKOUT_SET_IS_SELECTED = "completed_workout_set_is_selected";

    //=========================EAT_TABLE============================//
    public static final String EAT_TABLE = "eat_table";
    public static final String EAT_ID = "eat_id";
    public static final String EAT_NAME = "eat_name";
    public static final String EAT_PROTEIN = "eat_protein";
    public static final String EAT_FAT = "eat_fat";
    public static final String EAT_CARB = "eat_carb";
    public static final String EAT_CALORIES = "eat_calories";
    public static final String EAT_AMOUNT = "eat_amount";
    public static final String EAT_MEASUREMENT_TYPE = "eat_measurement_type";






    public AppDataBase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static synchronized AppDataBase getInstance(Context context) {
        if (instance == null) {
            instance = new AppDataBase(context.getApplicationContext());
        }
        return instance;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String createExerciseTableQuery = "CREATE TABLE IF NOT EXISTS " + EXERCISE_TABLE + " ("
                + EXERCISE_NAME + " TEXT, "
                + EXERCISE_TYPE + " TEXT, "
                + EXERCISE_BODY_TYPE + " TEXT)";

        String createPresetTableQuery = "CREATE TABLE IF NOT EXISTS " + PRESET_TABLE + " ("
                + PRESET_NAME + " TEXT, "
                + PRESET_EXERCISE_NAME + " TEXT, "
                + PRESET_EXERCISE_TYPE + " TEXT, "
                + PRESET_EXERCISE_BODY_TYPE + " TEXT)";


        // SQL-запрос для создания временной таблицы упражнений
        String createTempWorkoutExerciseTableQuery = "CREATE TABLE IF NOT EXISTS " + TEMP_WORKOUT_EXERCISE_TABLE + " (" +
                TEMP_WORKOUT_EXERCISE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TEMP_WORKOUT_EXERCISE_NAME + " TEXT NOT NULL, " +
                TEMP_WORKOUT_EXERCISE_TYPE + " TEXT NOT NULL, " +
                TEMP_WORKOUT_EXERCISE_BODY_TYPE + " TEXT NOT NULL, " +
                TEMP_WORKOUT_DATE + " TEXT NOT NULL);";

        // SQL-запрос для создания временной таблицы сетов
        String createTempWorkoutSetTableQuery = "CREATE TABLE IF NOT EXISTS " + TEMP_WORKOUT_SET_TABLE + " (" +
                TEMP_WORKOUT_SET_EXERCISE_ID + " INTEGER NOT NULL, " +
                TEMP_WORKOUT_SET_NUMBER + " INTEGER NOT NULL, " +
                TEMP_WORKOUT_SET_WEIGHT + " INTEGER, " +
                TEMP_WORKOUT_SET_REP + " INTEGER, " +
                TEMP_WORKOUT_SET_IS_SELECTED + " INTEGER NOT NULL DEFAULT 0, " +
                "PRIMARY KEY(" + TEMP_WORKOUT_SET_EXERCISE_ID + ", " + TEMP_WORKOUT_SET_NUMBER + "), " +
                "FOREIGN KEY(" + TEMP_WORKOUT_SET_EXERCISE_ID + ") REFERENCES " + TEMP_WORKOUT_EXERCISE_TABLE + "(" + TEMP_WORKOUT_EXERCISE_ID + "));";

        // SQL-запрос для создания таблицы завершённых тренировок
        String createCompletedWorkoutExerciseTableQuery = "CREATE TABLE IF NOT EXISTS " + COMPLETED_WORKOUT_EXERCISE_TABLE + " (" +
                COMPLETED_WORKOUT_EXERCISE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COMPLETED_WORKOUT_EXERCISE_NAME + " TEXT NOT NULL, " +
                COMPLETED_WORKOUT_EXERCISE_TYPE + " TEXT NOT NULL, " +
                COMPLETED_WORKOUT_EXERCISE_BODY_TYPE + " TEXT NOT NULL, " +
                COMPLETED_WORKOUT_DATE + " TEXT NOT NULL);";

        // SQL-запрос для создания таблицы сетов завершённых тренировок
        String createCompletedWorkoutSetTableQuery = "CREATE TABLE IF NOT EXISTS " + COMPLETED_WORKOUT_SET_TABLE + " (" +
                COMPLETED_WORKOUT_SET_EXERCISE_ID + " INTEGER NOT NULL, " +
                COMPLETED_WORKOUT_SET_NUMBER + " INTEGER NOT NULL, " +
                COMPLETED_WORKOUT_SET_WEIGHT + " INTEGER, " +
                COMPLETED_WORKOUT_SET_REP + " INTEGER, " +
                COMPLETED_WORKOUT_SET_IS_SELECTED + " INTEGER NOT NULL DEFAULT 0, " +
                "PRIMARY KEY(" + COMPLETED_WORKOUT_SET_EXERCISE_ID + ", " + COMPLETED_WORKOUT_SET_NUMBER + "), " +
                "FOREIGN KEY(" + COMPLETED_WORKOUT_SET_EXERCISE_ID + ") REFERENCES " + COMPLETED_WORKOUT_EXERCISE_TABLE + "(" + COMPLETED_WORKOUT_EXERCISE_ID + "));";

        // SQL-запрос для создания таблицы еды
        String createEatTableQuery = "CREATE TABLE IF NOT EXISTS " + EAT_TABLE + " (" +
                EAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EAT_NAME + " TEXT NOT NULL, " +
                EAT_PROTEIN + " REAL NOT NULL, " +
                EAT_FAT + " REAL NOT NULL, " +
                EAT_CARB + " REAL NOT NULL, " +
                EAT_CALORIES + " REAL NOT NULL, " +
                EAT_AMOUNT + " INTEGER NOT NULL, " +
                EAT_MEASUREMENT_TYPE + " TEXT NOT NULL);";

        //Exercise_Table's
        db.execSQL(createExerciseTableQuery);
        db.execSQL(createPresetTableQuery);
        //Temp_Workout_Table's
        db.execSQL(createTempWorkoutExerciseTableQuery);
        db.execSQL(createTempWorkoutSetTableQuery);
        //Complete_Workout_Table's
        db.execSQL(createCompletedWorkoutExerciseTableQuery);
        db.execSQL(createCompletedWorkoutSetTableQuery);

        //Eat_table
        db.execSQL(createEatTableQuery);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Удаляем существующие таблицы
        db.execSQL("DROP TABLE IF EXISTS " + EXERCISE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + PRESET_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TEMP_WORKOUT_EXERCISE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TEMP_WORKOUT_SET_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + COMPLETED_WORKOUT_EXERCISE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + COMPLETED_WORKOUT_SET_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + EAT_TABLE);

        // Пересоздаём таблицы
        onCreate(db);
    }


}
