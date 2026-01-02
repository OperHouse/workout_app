package com.example.workoutapp.Data.Tables;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppDataBase extends SQLiteOpenHelper {

    private static final String DB_NAME = "WorkoutApp.db";
    private static final int DB_VERSION = 3;

    private static AppDataBase instance;


    //=========================BASE_EXERCISE_TABLE============================//
    public static final String BASE_EXERCISE_TABLE = "base_exercise_table";
    public static final String BASE_EXERCISE_ID = "base_exercise_id";
    public static final String BASE_EXERCISE_NAME = "base_exercise_name";
    public static final String BASE_EXERCISE_TYPE = "base_exercise_type";
    public static final String BASE_EXERCISE_BODY_TYPE = "base_exercise_body_type";

    //=========================WORKOUT_PRESET_NAME_TABLE=======================//
    public static final String WORKOUT_PRESET_NAME_TABLE = "workout_preset_table";
    public static final String WORKOUT_PRESET_NAME_ID = "workout_preset_name_id";
    public static final String WORKOUT_PRESET_NAME = "workout_preset_name";

    //=========================CONNECTING_WORKOUT_PRESET_TABLE============================//
    public static final String CONNECTING_WORKOUT_PRESET_TABLE = "connecting_workout_preset_table";
    public static final String CONNECTING_WORKOUT_PRESET_NAME_ID = "connecting_workout_preset_name_id";
    public static final String CONNECTING_WORKOUT_PRESET_BASE_EXERCISE_ID = "connecting_workout_preset_base_exercise_id";

    //=========================WORKOUT_EXERCISE_TABLE============================//
    public static final String WORKOUT_EXERCISE_TABLE = "workout_exercise_table";
    public static final String WORKOUT_EXERCISE_ID = "workout_exercise_id"; // PRIMARY KEY
    public static final String WORKOUT_EXERCISE_NAME = "workout_exercise_name";
    public static final String WORKOUT_EXERCISE_TYPE = "workout_exercise_type";
    public static final String WORKOUT_EXERCISE_BODY_TYPE = "workout_exercise_body_type";
    public static final String WORKOUT_EXERCISE_DATE = "workout_exercise_data";
    public static final String WORKOUT_EXERCISE_STATE = "workout_exercise_state";

    //=========================STRENGTH_SET_DETAILS_TABLE============================//
    public static final String STRENGTH_SET_DETAILS_TABLE = "strength_set_details_table";
    public static final String STRENGTH_SET_ID = "strength_set_id"; // PRIMARY KEY
    public static final String STRENGTH_SET_WEIGHT = "strength_set_weight";
    public static final String STRENGTH_SET_REP = "strength_set_rep";
    public static final String STRENGTH_SET_STATE = "strength_set_state";
    public static final String STRENGTH_SET_ORDER = "strength_set_order";
    public static final String STRENGTH_SET_FOREIGN_KEY_EXERCISE = "strength_set_foreign_key_exercise"; // FOREIGN KEY -> WORKOUT_EXERCISE_ID

    //=========================CARDIO_SET_DETAILS_TABLE============================//
    public static final String CARDIO_SET_DETAILS_TABLE = "cardio_set_details_table";
    public static final String CARDIO_SET_ID = "cardio_set_id"; // PRIMARY
    public static final String CARDIO_SET_TEMP = "cardio_set_temp";
    public static final String CARDIO_SET_TIME = "cardio_set_time";
    public static final String CARDIO_SET_DISTANCE = "cardio_set_distance";
    public static final String CARDIO_SET_STATE = "cardio_set_state";
    public static final String CARDIO_SET_ORDER = "cardio_set_order";
    public static final String CARDIO_SET_FOREIGN_KEY_EXERCISE = "cardio_set_foreign_key_exercise"; // FOREIGN KEY -> WORKOUT_EXERCISE_ID

    //=========================BASE_EAT_TABLE============================//
    public static final String BASE_FOOD_TABLE = "base_food_table";
    public static final String BASE_FOOD_ID = "base_food_id";
    public static final String BASE_FOOD_NAME = "base_food_name";
    public static final String BASE_FOOD_PROTEIN = "base_food_protein";
    public static final String BASE_FOOD_FAT = "base_food_fat";
    public static final String BASE_FOOD_CARB = "base_food_carb";
    public static final String BASE_FOOD_CALORIES = "base_food_calories";
    public static final String BASE_FOOD_AMOUNT = "base_food_amount";
    public static final String BASE_FOOD_MEASUREMENT_TYPE = "base_food_measurement_type";

    //=========================PRESET_FOOD_TABLE============================//
    public static final String PRESET_FOOD_TABLE = "preset_food_table";
    public static final String PRESET_FOOD_ID = "preset_food_id";
    public static final String PRESET_FOOD_NAME = "preset_food_name";
    public static final String PRESET_FOOD_PROTEIN = "preset_food_protein";
    public static final String PRESET_FOOD_FAT = "preset_food_fat";
    public static final String PRESET_FOOD_CARB = "preset_food_carb";
    public static final String PRESET_FOOD_CALORIES = "preset_food_calories";
    public static final String PRESET_FOOD_AMOUNT = "preset_food_amount";
    public static final String PRESET_FOOD_MEASUREMENT_TYPE = "preset_food_measurement_type";

    //=========================MEAL_PRESET_NAME_TABLE============================//
    public static final String MEAL_PRESET_NAME_TABLE = "meal_preset_name_table";
    public static final String MEAL_PRESET_NAME_ID = "meal_preset_name_id";
    public static final String MEAL_PRESET_NAME = "meal_preset_name";

    //=========================CONNECTING_MEAL_PRESET_TABLE============================//
    public static final String CONNECTING_MEAL_PRESET_TABLE = "connecting_meal_preset_table";
    public static final String CONNECTING_MEAL_PRESET_NAME_ID = "connecting_meal_preset_name_id";
    public static final String CONNECTING_MEAL_PRESET_FOOD_ID = "connecting_meal_preset_food_id";

    //=========================MEAL_NAME_TABLE============================//
    public static final String MEAL_NAME_TABLE = "meal_name_table";
    public static final String MEAL_DATA = "meal_data";
    public static final String MEAL_NAME_ID = "meal_name_id";
    public static final String MEAL_NAME = "meal_name";

    //=========================MEAL_FOOD_TABLE============================//
    public static final String MEAL_FOOD_TABLE = "meal_food_table";
    public static final String MEAL_FOOD_ID = "meal_food_id";
    public static final String MEAL_FOOD_NAME = "meal_food_name";
    public static final String MEAL_FOOD_PROTEIN = "meal_food_protein";
    public static final String MEAL_FOOD_FAT = "meal_food_fat";
    public static final String MEAL_FOOD_CARB = "meal_food_carb";
    public static final String MEAL_FOOD_CALORIES = "meal_food_calories";
    public static final String MEAL_FOOD_AMOUNT = "meal_food_amount";
    public static final String MEAL_FOOD_MEASUREMENT_TYPE = "meal_food_measurement_type";

    //=========================CONNECTING_MEAL_TABLE============================//

    public static final String CONNECTING_MEAL_TABLE = "connecting_meal_table";
    public static final String CONNECTING_MEAL_NAME_ID = "connecting_meal_name_id";
    public static final String CONNECTING_MEAL_FOOD_ID = "connecting_meal_food_id";


    // =========================USER_PROFILE_TABLE============================//
    //Общие пользовательские данные
    //(Имя, рост, возраст)
    //(Обновляется не каждый день, а только тогда, когда пользователь обновляет один из параметров)
    public static final String USER_PROFILE_TABLE = "user_profile_table";
    public static final String USER_ID = "user_id";
    public static final String USER_NAME = "user_name";
    public static final String USER_HEIGHT = "user_height";
    public static final String USER_AGE = "user_age";
    public static final String USER_IMAGE_PATH = "user_image_path";

    // =========================WEIGHT_HISTORY_TABLE============================//
    //Данные веса пользователя
    //(Обновляется не каждый день, а тогда, когда пользователь обновляет вес или параметр веса берется с сторонних приборов(весы))
    public static final String WEIGHT_HISTORY_TABLE = "weight_history_table";
    public static final String WEIGHT_ID = "weight_id";
    public static final String WEIGHT_MEASUREMENT_DATE = "weight_measurement_date";
    public static final String WEIGHT_VALUE = "weight_value";

    // =========================DAILY_ACTIVITY_TRACKING_TABLE============================//
    //Какая у пользователя была активность за день(шаги, километраж, количество сожженных калорий)
    //(Обновляется каждый день)
    public static final String DAILY_ACTIVITY_TRACKING_TABLE = "daily_activity_tracking_table";
    public static final String DAILY_ACTIVITY_TRACKING_ACTIVITY_ID = "daily_activity_tracking_id";
    public static final String DAILY_ACTIVITY_TRACKING_ACTIVITY_DATE = "daily_activity_tracking_date";
    public static final String DAILY_ACTIVITY_TRACKING_ACTIVITY_STEPS = "daily_activity_tracking_steps";
    public static final String DAILY_ACTIVITY_RACKING_ACTIVITY_DISTANCE = "daily_activity_tracking_distance";
    public static final String DAILY_ACTIVITY_TRACKING_CALORIES_BURN = "daily_activity_tracking_calories_burn";

    // =========================GENERAL_GOAL_TABLE============================//
    //Общие цели пользователя
    //(текст глобальной цели, количество тренировок в неделю, количество отслеживания питания в неделю)
    //(Обновляется не каждый день, а только тогда, когда пользователь обновляет один из параметров)
    public static final String GENERAL_GOAL_TABLE = "general_goal_table";
    public static final String GENERAL_GOAL_ID = "general_goal_id";
    public static final String GENERAL_GLOBAL_GOAL_TEXT = "general_global_goal_text";
    public static final String GENERAL_GOAL_WORKOUTS_WEEKLY = "general_goal_workouts_weekly";
    public static final String GENERAL_GOAL_FOOD_TRACKING_WEEKLY = "general_goal_food_tracking_weekly";
    public static final String GENERAL_GOAL_DATE = "general_goal_date";

    // =========================GENERAL_ACTIVITY_TRACKING_GOAL_TABLE============================//
    //Цели активности пользователя
    //(количество калорий которые пользователь хотел бы сжигать в день, количество цагов которые пользователь хотел бы пройти за день)
    //(Обновляется не каждый день, а только тогда, когда пользователь обновляет один из параметров)
    public static final String ACTIVITY_GOAL_TABLE = "activity_goal_table";
    public static final String ACTIVITY_GOAL_ID = "activity_goal_id";
    public static final String ACTIVITY_GOAL_DATE = "activity_goal_date";
    public static final String ACTIVITY_GOAL_STEPS = "activity_goal_steps";
    public static final String ACTIVITY_CALORIES_TO_BURN = "activity_goal_calories_to_burn";

    // =========================FOOD_GAIN_GOAL_TABLE============================//
    //Сколько пользователь хотел бы принимать пищи
    //(Количество калорий, белков, жиров, углеводов)
    //(Обновляется не каждый день, а только тогда, когда пользователь обновляет один из параметров)
    public static final String FOOD_GAIN_GOAL_TABLE = "food_gain_goal_table";
    public static final String FOOD_GAIN_GOAL_ID = "food_gain_goal_id";
    public static final String FOOD_GAIN_GOAL_CALORIES = "food_gain_goal_calories";
    public static final String FOOD_GAIN_GOAL_PROTEIN = "food_gain_goal_protein";
    public static final String FOOD_GAIN_GOAL_FAT = "food_gain_goal_fat";
    public static final String FOOD_GAIN_GOAL_CARB = "food_gain_goal_carb";
    public static final String FOOD_GAIN_GOAL_DATE = "food_gain_goal_date";


    // =========================DAILY_FOOD_TRACKING_TABLE============================//
    //Сколько калорий и бжу съел пользователь за день
    //(Обновляется каждый день)
    public static final String DAILY_FOOD_TRACKING_TABLE = "daily_food_tracking_table";
    public static final String TRACKING_FOOD_ID = "tracking_food_id";
    public static final String TRACKING_CALORIES = "tracking_calories";
    public static final String TRACKING_PROTEIN = "tracking_protein";
    public static final String TRACKING_FAT = "tracking_fat";
    public static final String TRACKING_CARB = "tracking_carb";
    public static final String DAILY_FOOD_TRACKING_DATE = "tracking_activity_date";
    //=============================================================================//




    public AppDataBase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static synchronized AppDataBase getInstance(Context context) {
        if (instance == null) {
            instance = new AppDataBase(context.getApplicationContext());
        } else {
            // Попытка получить доступ к базе, если она закрыта
            try {
                SQLiteDatabase db = instance.getReadableDatabase();
            } catch (IllegalStateException e) {
                // Если база закрыта — пересоздаём
                instance = new AppDataBase(context.getApplicationContext());
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Включаем поддержку внешних ключей
        db.execSQL("PRAGMA foreign_keys = ON;");

        // --- Таблицы для базовых упражнений и пресетов ---
        String createBaseExerciseTableQuery = "CREATE TABLE IF NOT EXISTS " + BASE_EXERCISE_TABLE + " ("
                + BASE_EXERCISE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BASE_EXERCISE_NAME + " TEXT NOT NULL, "
                + BASE_EXERCISE_TYPE + " TEXT NOT NULL, "
                + BASE_EXERCISE_BODY_TYPE + " TEXT NOT NULL);";

        String createWorkoutPresetNameTableQuery = "CREATE TABLE IF NOT EXISTS " + WORKOUT_PRESET_NAME_TABLE + " ("
                + WORKOUT_PRESET_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + WORKOUT_PRESET_NAME + " TEXT NOT NULL UNIQUE);"; // UNIQUE для имен пресетов

        String createConnectingWorkoutPresetTableQuery = "CREATE TABLE IF NOT EXISTS " + CONNECTING_WORKOUT_PRESET_TABLE + " ("
                + CONNECTING_WORKOUT_PRESET_NAME_ID + " INTEGER NOT NULL, "
                + CONNECTING_WORKOUT_PRESET_BASE_EXERCISE_ID + " INTEGER NOT NULL, "
                + "PRIMARY KEY (" + CONNECTING_WORKOUT_PRESET_NAME_ID + ", " + CONNECTING_WORKOUT_PRESET_BASE_EXERCISE_ID + "), "
                + "FOREIGN KEY (" + CONNECTING_WORKOUT_PRESET_NAME_ID + ") REFERENCES " + WORKOUT_PRESET_NAME_TABLE + "(" + WORKOUT_PRESET_NAME_ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY (" + CONNECTING_WORKOUT_PRESET_BASE_EXERCISE_ID + ") REFERENCES " + BASE_EXERCISE_TABLE + "(" + BASE_EXERCISE_ID + ") ON DELETE CASCADE);";


        // --- Таблицы для журнала выполненных упражнений ---
        String createWorkoutExerciseTableQuery = "CREATE TABLE IF NOT EXISTS " + WORKOUT_EXERCISE_TABLE + " ("
                + WORKOUT_EXERCISE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + WORKOUT_EXERCISE_NAME + " TEXT NOT NULL, "
                + WORKOUT_EXERCISE_TYPE + " TEXT NOT NULL, "
                + WORKOUT_EXERCISE_BODY_TYPE + " TEXT NOT NULL, "
                + WORKOUT_EXERCISE_DATE + " TEXT NOT NULL, "
                + WORKOUT_EXERCISE_STATE + " TEXT NOT NULL);";

        String createStrengthSetDetailsTableQuery = "CREATE TABLE IF NOT EXISTS " + STRENGTH_SET_DETAILS_TABLE + " ("
                + STRENGTH_SET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " // Уникальный ID для каждого сета
                + STRENGTH_SET_FOREIGN_KEY_EXERCISE + " INTEGER NOT NULL, " // FK к WORKOUT_EXERCISE_TABLE
                + STRENGTH_SET_ORDER + " INTEGER NOT NULL, " // Порядок сета в упражнении
                + STRENGTH_SET_WEIGHT + " REAL NOT NULL, "
                + STRENGTH_SET_REP + " INTEGER NOT NULL, "
                + STRENGTH_SET_STATE + " TEXT NOT NULL, "
                + "FOREIGN KEY (" + STRENGTH_SET_FOREIGN_KEY_EXERCISE + ") REFERENCES " + WORKOUT_EXERCISE_TABLE + "(" + WORKOUT_EXERCISE_ID + ") ON DELETE CASCADE);";

        String createCardioSetDetailsTableQuery = "CREATE TABLE IF NOT EXISTS " + CARDIO_SET_DETAILS_TABLE + " ("
                + CARDIO_SET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " // Уникальный ID для каждого сета
                + CARDIO_SET_FOREIGN_KEY_EXERCISE + " INTEGER NOT NULL, " // FK к WORKOUT_EXERCISE_TABLE
                + CARDIO_SET_ORDER + " INTEGER NOT NULL, " // Порядок сета в упражнении
                + CARDIO_SET_TEMP + " REAL NOT NULL, "
                + CARDIO_SET_TIME + " INTEGER NOT NULL, " // Предполагаем секунды или минуты
                + CARDIO_SET_DISTANCE + " REAL NOT NULL, "
                + CARDIO_SET_STATE + " TEXT NOT NULL, "
                + "FOREIGN KEY (" + CARDIO_SET_FOREIGN_KEY_EXERCISE + ") REFERENCES " + WORKOUT_EXERCISE_TABLE + "(" + WORKOUT_EXERCISE_ID + ") ON DELETE CASCADE);";


        // --- Таблицы для питания ---
        String createBaseFoodTableQuery = "CREATE TABLE IF NOT EXISTS " + BASE_FOOD_TABLE + " (" +
                BASE_FOOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BASE_FOOD_NAME + " TEXT NOT NULL UNIQUE, " +
                BASE_FOOD_PROTEIN + " REAL NOT NULL, " +
                BASE_FOOD_FAT + " REAL NOT NULL, " +
                BASE_FOOD_CARB + " REAL NOT NULL, " +
                BASE_FOOD_CALORIES + " REAL NOT NULL, " +
                BASE_FOOD_AMOUNT + " INTEGER NOT NULL, " +
                BASE_FOOD_MEASUREMENT_TYPE + " TEXT NOT NULL);";

        String createPresetFoodTableQuery = "CREATE TABLE IF NOT EXISTS " + PRESET_FOOD_TABLE + " (" +
                PRESET_FOOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PRESET_FOOD_NAME + " TEXT NOT NULL, "
                + PRESET_FOOD_PROTEIN + " REAL NOT NULL, "
                + PRESET_FOOD_FAT + " REAL NOT NULL, "
                + PRESET_FOOD_CARB + " REAL NOT NULL, "
                + PRESET_FOOD_CALORIES + " REAL NOT NULL, "
                + PRESET_FOOD_AMOUNT + " INTEGER NOT NULL, "
                + PRESET_FOOD_MEASUREMENT_TYPE + " TEXT NOT NULL);";

        String createMealPresetNameTableQuery = "CREATE TABLE IF NOT EXISTS " + MEAL_PRESET_NAME_TABLE + " (" +
                MEAL_PRESET_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MEAL_PRESET_NAME + " TEXT NOT NULL UNIQUE);";

        String createConnectingMealPresetTableQuery = "CREATE TABLE IF NOT EXISTS " + CONNECTING_MEAL_PRESET_TABLE + " (" +
                CONNECTING_MEAL_PRESET_NAME_ID + " INTEGER NOT NULL, " +
                CONNECTING_MEAL_PRESET_FOOD_ID + " INTEGER NOT NULL, " +
                "PRIMARY KEY (" + CONNECTING_MEAL_PRESET_NAME_ID + ", " + CONNECTING_MEAL_PRESET_FOOD_ID + "), " +
                "FOREIGN KEY (" + CONNECTING_MEAL_PRESET_NAME_ID + ") REFERENCES " + MEAL_PRESET_NAME_TABLE + "(" + MEAL_PRESET_NAME_ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY (" + CONNECTING_MEAL_PRESET_FOOD_ID + ") REFERENCES " + PRESET_FOOD_TABLE + "(" + PRESET_FOOD_ID + ") ON DELETE CASCADE);";

        String createMealNameTableQuery = "CREATE TABLE IF NOT EXISTS " + MEAL_NAME_TABLE + " (" +
                MEAL_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MEAL_NAME + " TEXT NOT NULL, " +
                MEAL_DATA + " TEXT NOT NULL);";

        String createMealEatTableQuery = "CREATE TABLE IF NOT EXISTS " + MEAL_FOOD_TABLE + " (" +
                MEAL_FOOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MEAL_FOOD_NAME + " TEXT NOT NULL, "
                + MEAL_FOOD_PROTEIN + " REAL NOT NULL, "
                + MEAL_FOOD_FAT + " REAL NOT NULL, "
                + MEAL_FOOD_CARB + " REAL NOT NULL, "
                + MEAL_FOOD_CALORIES + " REAL NOT NULL, "
                + MEAL_FOOD_AMOUNT + " INTEGER NOT NULL, "
                + MEAL_FOOD_MEASUREMENT_TYPE + " TEXT NOT NULL);";

        String createConnectingMealTableQuery = "CREATE TABLE IF NOT EXISTS " + CONNECTING_MEAL_TABLE + " (" +
                CONNECTING_MEAL_NAME_ID + " INTEGER NOT NULL, " +
                CONNECTING_MEAL_FOOD_ID + " INTEGER NOT NULL, " +
                "PRIMARY KEY(" + CONNECTING_MEAL_NAME_ID + ", " + CONNECTING_MEAL_FOOD_ID + "), " +
                "FOREIGN KEY(" + CONNECTING_MEAL_NAME_ID + ") REFERENCES " + MEAL_NAME_TABLE + "(" + MEAL_NAME_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY(" + CONNECTING_MEAL_FOOD_ID + ") REFERENCES " + MEAL_FOOD_TABLE + "(" + MEAL_FOOD_ID + ") ON DELETE CASCADE);";

        String createUserProfileTableQuery = "CREATE TABLE IF NOT EXISTS " + USER_PROFILE_TABLE + " ("
                + USER_ID + " INTEGER PRIMARY KEY, "
                + USER_NAME + " TEXT NOT NULL, "
                + USER_HEIGHT + " REAL, "
                + USER_AGE + " INTEGER, "
                + USER_IMAGE_PATH + " TEXT);";

        String createWeightHistoryTableQuery = "CREATE TABLE IF NOT EXISTS " + WEIGHT_HISTORY_TABLE + " ("
                + WEIGHT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " // Вся декларация в одной строке
                + WEIGHT_MEASUREMENT_DATE + " TEXT NOT NULL, "
                + WEIGHT_VALUE + " REAL NOT NULL);";

        String createDailyActivityTrackingTableQuery = "CREATE TABLE IF NOT EXISTS " + DAILY_ACTIVITY_TRACKING_TABLE + " ("
                + DAILY_ACTIVITY_TRACKING_ACTIVITY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DAILY_ACTIVITY_TRACKING_ACTIVITY_DATE + " TEXT NOT NULL, "
                + DAILY_ACTIVITY_TRACKING_ACTIVITY_STEPS + " INTEGER, "
                + DAILY_ACTIVITY_RACKING_ACTIVITY_DISTANCE + " REAL, "
                + DAILY_ACTIVITY_TRACKING_CALORIES_BURN + " REAL);";


        // Общие цели пользователя (текст, кол-во тренировок в неделю, питание)
        String createGeneralGoalTableQuery = "CREATE TABLE IF NOT EXISTS " + GENERAL_GOAL_TABLE + " (" +
                GENERAL_GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                GENERAL_GLOBAL_GOAL_TEXT + " TEXT, " +
                GENERAL_GOAL_WORKOUTS_WEEKLY + " INTEGER, " +
                GENERAL_GOAL_FOOD_TRACKING_WEEKLY + " INTEGER, " +
                GENERAL_GOAL_DATE + " TEXT NOT NULL);";

        // Цели по активности (кол-во шагов и калорий в день)
        String createActivityGoalTableQuery = "CREATE TABLE IF NOT EXISTS " + ACTIVITY_GOAL_TABLE + " (" +
                ACTIVITY_GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ACTIVITY_GOAL_DATE + " TEXT NOT NULL, " +
                ACTIVITY_GOAL_STEPS + " INTEGER, " +
                ACTIVITY_CALORIES_TO_BURN + " REAL);";

        // Цели по питанию (сколько калорий и БЖУ в день)
        String createFoodGainGoalTableQuery = "CREATE TABLE IF NOT EXISTS " + FOOD_GAIN_GOAL_TABLE + " (" +
                FOOD_GAIN_GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FOOD_GAIN_GOAL_CALORIES + " REAL, " +
                FOOD_GAIN_GOAL_PROTEIN + " REAL, " +
                FOOD_GAIN_GOAL_FAT + " REAL, " +
                FOOD_GAIN_GOAL_CARB + " REAL, " +
                FOOD_GAIN_GOAL_DATE + " TEXT NOT NULL);";

        String createDailyFoodTrackingTableQuery = "CREATE TABLE IF NOT EXISTS " + DAILY_FOOD_TRACKING_TABLE + " ("
                + TRACKING_FOOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DAILY_FOOD_TRACKING_DATE + " TEXT NOT NULL, "
                + TRACKING_CALORIES + " INTEGER NOT NULL, "
                + TRACKING_PROTEIN + " REAL, "
                + TRACKING_FAT + " REAL, "
                + TRACKING_CARB + " REAL);";







        // --- Выполнение запросов на создание таблиц ---
        db.execSQL(createBaseExerciseTableQuery);
        db.execSQL(createWorkoutPresetNameTableQuery);
        db.execSQL(createConnectingWorkoutPresetTableQuery);

        db.execSQL(createWorkoutExerciseTableQuery);
        db.execSQL(createStrengthSetDetailsTableQuery);
        db.execSQL(createCardioSetDetailsTableQuery);

        db.execSQL(createBaseFoodTableQuery);
        db.execSQL(createPresetFoodTableQuery);
        db.execSQL(createMealPresetNameTableQuery);
        db.execSQL(createConnectingMealPresetTableQuery);
        db.execSQL(createMealNameTableQuery);
        db.execSQL(createMealEatTableQuery);
        db.execSQL(createConnectingMealTableQuery);

        db.execSQL(createUserProfileTableQuery);
        db.execSQL(createWeightHistoryTableQuery);
        db.execSQL(createDailyActivityTrackingTableQuery);
        db.execSQL(createDailyFoodTrackingTableQuery);
        db.execSQL(createGeneralGoalTableQuery);
        db.execSQL(createActivityGoalTableQuery);
        db.execSQL(createFoodGainGoalTableQuery);



    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Удаляем таблицы в правильном порядке (сначала зависимости)
        db.execSQL("DROP TABLE IF EXISTS " + CONNECTING_MEAL_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + MEAL_FOOD_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + MEAL_NAME_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CONNECTING_MEAL_PRESET_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + MEAL_PRESET_NAME_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + PRESET_FOOD_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + BASE_FOOD_TABLE);

        db.execSQL("DROP TABLE IF EXISTS " + CARDIO_SET_DETAILS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + STRENGTH_SET_DETAILS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + WORKOUT_EXERCISE_TABLE);

        db.execSQL("DROP TABLE IF EXISTS " + CONNECTING_WORKOUT_PRESET_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + WORKOUT_PRESET_NAME_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + BASE_EXERCISE_TABLE);

        db.execSQL("DROP TABLE IF EXISTS " + USER_PROFILE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + WEIGHT_HISTORY_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DAILY_ACTIVITY_TRACKING_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + GENERAL_GOAL_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ACTIVITY_GOAL_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + FOOD_GAIN_GOAL_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DAILY_FOOD_TRACKING_TABLE);

        // Пересоздаём таблицы
        onCreate(db);
    }




}
