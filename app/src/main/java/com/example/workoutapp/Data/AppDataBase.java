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

    //=========================BASE_EAT_TABLE============================//
    public static final String BASE_EAT_TABLE = "base_eat_table";
    public static final String BASE_EAT_ID = "base_eat_id";
    public static final String BASE_EAT_NAME = "base_eat_name";
    public static final String BASE_EAT_PROTEIN = "base_eat_protein";
    public static final String BASE_EAT_FAT = "base_eat_fat";
    public static final String BASE_EAT_CARB = "base_eat_carb";
    public static final String BASE_EAT_CALORIES = "base_eat_calories";
    public static final String BASE_EAT_AMOUNT = "base_eat_amount";
    public static final String BASE_EAT_MEASUREMENT_TYPE = "base_eat_measurement_type";

    //=========================PRESET_EAT_TABLE============================//
    public static final String PRESET_EAT_TABLE = "preset_eat_table";
    public static final String PRESET_EAT_ID = "preset_eat_id";
    public static final String PRESET_EAT_NAME = "preset_eat_name";
    public static final String PRESET_EAT_PROTEIN = "preset_eat_protein";
    public static final String PRESET_EAT_FAT = "preset_eat_fat";
    public static final String PRESET_EAT_CARB = "preset_eat_carb";
    public static final String PRESET_EAT_CALORIES = "preset_eat_calories";
    public static final String PRESET_EAT_AMOUNT = "preset_eat_amount";
    public static final String PRESET_EAT_MEASUREMENT_TYPE = "preset_eat_measurement_type";

    //=========================MEAL_PRESET_NAME_TABLE============================//
    public static final String MEAL_PRESET_NAME_TABLE = "meal_preset_name_table";
    public static final String MEAL_PRESET_NAME_ID = "meal_preset_name_id";
    public static final String MEAL_PRESET_NAME = "meal_preset_name";

    //=========================CONNECTING_MEAL_PRESET_TABLE============================//
    public static final String CONNECTING_MEAL_PRESET_TABLE = "connecting_meal_preset_table";
    public static final String CONNECTING_MEAL_PRESET_NAME_ID = "connecting_meal_preset_name_id";
    public static final String CONNECTING_MEAL_PRESET_EAT_ID = "connecting_meal_preset_eat_id";

    //=========================MEAL_NAME_TABLE============================//
    public static final String MEAL_NAME_TABLE = "meal_name_table";
    public static final String MEAL_DATA = "meal_data";
    public static final String MEAL_NAME_ID = "meal_name_id";
    public static final String MEAL_NAME = "meal_name";

    //=========================MEAL_EAT_TABLE============================//
    public static final String MEAL_EAT_TABLE = "meal_eat_table";
    public static final String MEAL_EAT_ID = "meal_eat_id";
    public static final String MEAL_EAT_NAME = "meal_eat_name";
    public static final String MEAL_EAT_PROTEIN = "meal_eat_protein";
    public static final String MEAL_EAT_FAT = "meal_eat_fat";
    public static final String MEAL_EAT_CARB = "meal_eat_carb";
    public static final String MEAL_EAT_CALORIES = "meal_eat_calories";
    public static final String MEAL_EAT_AMOUNT = "meal_eat_amount";
    public static final String MEAL_EAT_MEASUREMENT_TYPE = "meal_eat_measurement_type";

    //=========================CONNECTING_MEAL_TABLE============================//

    public static final String CONNECTING_MEAL_TABLE = "connecting_meal_table";
    public static final String CONNECTING_MEAL_NAME_ID = "connecting_meal_name_id";
    public static final String CONNECTING_MEAL_EAT_ID = "connecting_meal_eat_id";

    //=============================================================================//




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
        String createEatTableQuery = "CREATE TABLE IF NOT EXISTS " + BASE_EAT_TABLE + " (" +
                BASE_EAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BASE_EAT_NAME + " TEXT NOT NULL, " +
                BASE_EAT_PROTEIN + " REAL NOT NULL, " +
                BASE_EAT_FAT + " REAL NOT NULL, " +
                BASE_EAT_CARB + " REAL NOT NULL, " +
                BASE_EAT_CALORIES + " REAL NOT NULL, " +
                BASE_EAT_AMOUNT + " INTEGER NOT NULL, " +
                BASE_EAT_MEASUREMENT_TYPE + " TEXT NOT NULL);";

        // SQL-запрос для создания таблицы еды для пресета
        String createPresetEatTableQuery = "CREATE TABLE IF NOT EXISTS " + PRESET_EAT_TABLE + " (" +
                PRESET_EAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PRESET_EAT_NAME + " TEXT NOT NULL, " +
                PRESET_EAT_PROTEIN + " REAL NOT NULL, " +
                PRESET_EAT_FAT + " REAL NOT NULL, " +
                PRESET_EAT_CARB + " REAL NOT NULL, " +
                PRESET_EAT_CALORIES + " REAL NOT NULL, " +
                PRESET_EAT_AMOUNT + " INTEGER NOT NULL, " +
                PRESET_EAT_MEASUREMENT_TYPE + " TEXT NOT NULL);";

        // SQL-запрос для создания таблицы имени пресета
        String createMealPresetNameTableQuery = "CREATE TABLE IF NOT EXISTS " + MEAL_PRESET_NAME_TABLE + " (" +
                MEAL_PRESET_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MEAL_PRESET_NAME + " TEXT NOT NULL);";

        // SQL-запрос для создания связывающей таблици для пресетов приемов пищи
        String createConnectingMealPresetTableQuery = "CREATE TABLE IF NOT EXISTS " + CONNECTING_MEAL_PRESET_TABLE + " (" +
                CONNECTING_MEAL_PRESET_NAME_ID + " INTEGER NOT NULL, " +
                CONNECTING_MEAL_PRESET_EAT_ID + " INTEGER NOT NULL, " +
                "PRIMARY KEY (" + CONNECTING_MEAL_PRESET_NAME_ID + ", " + CONNECTING_MEAL_PRESET_EAT_ID + "), " +
                "FOREIGN KEY (" + CONNECTING_MEAL_PRESET_NAME_ID + ") REFERENCES " + MEAL_PRESET_NAME_TABLE + "(" + MEAL_PRESET_NAME_ID + "), " +
                "FOREIGN KEY (" + CONNECTING_MEAL_PRESET_EAT_ID + ") REFERENCES " + PRESET_EAT_TABLE + "(" + PRESET_EAT_ID + "));";

        // SQL-запрос для создания таблицы названий приёмов пищи
        String createMealNameTableQuery = "CREATE TABLE IF NOT EXISTS " + MEAL_NAME_TABLE + " (" +
                MEAL_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MEAL_NAME + " TEXT NOT NULL, " +
                MEAL_DATA + " TEXT NOT NULL);";

        // SQL-запрос для создания таблицы еды в приёмах пищи
        String createMealEatTableQuery = "CREATE TABLE IF NOT EXISTS " + MEAL_EAT_TABLE + " (" +
                MEAL_EAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MEAL_EAT_NAME + " TEXT NOT NULL, " +
                MEAL_EAT_PROTEIN + " REAL NOT NULL, " +
                MEAL_EAT_FAT + " REAL NOT NULL, " +
                MEAL_EAT_CARB + " REAL NOT NULL, " +
                MEAL_EAT_CALORIES + " REAL NOT NULL, " +
                MEAL_EAT_AMOUNT + " INTEGER NOT NULL, " +
                MEAL_EAT_MEASUREMENT_TYPE + " TEXT NOT NULL);";

        // SQL-запрос для создания связывающей таблицы для еды и приёмов пищи
        String createConnectingMealTableQuery = "CREATE TABLE IF NOT EXISTS " + CONNECTING_MEAL_TABLE + " (" +
                CONNECTING_MEAL_NAME_ID + " INTEGER NOT NULL, " +
                CONNECTING_MEAL_EAT_ID + " INTEGER NOT NULL, " +
                "PRIMARY KEY(" + CONNECTING_MEAL_NAME_ID + ", " + CONNECTING_MEAL_EAT_ID + "), " +
                "FOREIGN KEY(" + CONNECTING_MEAL_NAME_ID + ") REFERENCES " + MEAL_NAME_TABLE + "(" + MEAL_NAME_ID + "), " +
                "FOREIGN KEY(" + CONNECTING_MEAL_EAT_ID + ") REFERENCES " + MEAL_EAT_TABLE + "(" + MEAL_EAT_ID + "));";


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
        //Preset_Eat_Table
        db.execSQL(createPresetEatTableQuery);
        //Meal_Preset_Name_Table
        db.execSQL(createMealPresetNameTableQuery);
        //Connecting_Table for Meal_Preset
        db.execSQL(createConnectingMealPresetTableQuery);

        //Meal tables
        db.execSQL(createMealNameTableQuery);
        db.execSQL(createMealEatTableQuery);
        db.execSQL(createConnectingMealTableQuery);

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
        db.execSQL("DROP TABLE IF EXISTS " + BASE_EAT_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + PRESET_EAT_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + MEAL_PRESET_NAME_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CONNECTING_MEAL_PRESET_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + MEAL_NAME_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + MEAL_EAT_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CONNECTING_MEAL_TABLE);

        // Пересоздаём таблицы
        onCreate(db);
    }


}
