package com.example.workoutapp.Tools.SyncTools;

import android.util.Log;

import com.example.workoutapp.Data.ChangeElmDao;
import com.example.workoutapp.Data.NutritionDao.ConnectingMealDao;
import com.example.workoutapp.Data.NutritionDao.ConnectingMealPresetDao;
import com.example.workoutapp.Data.NutritionDao.MealFoodDao;
import com.example.workoutapp.Data.NutritionDao.MealNameDao;
import com.example.workoutapp.Data.NutritionDao.PresetEatDao;
import com.example.workoutapp.Data.NutritionDao.PresetMealNameDao;
import com.example.workoutapp.Data.ProfileDao.ActivityGoalDao;
import com.example.workoutapp.Data.ProfileDao.DailyActivityTrackingDao;
import com.example.workoutapp.Data.ProfileDao.DailyFoodTrackingDao;
import com.example.workoutapp.Data.ProfileDao.FoodGainGoalDao;
import com.example.workoutapp.Data.ProfileDao.GeneralGoalDao;
import com.example.workoutapp.Data.ProfileDao.UserProfileDao;
import com.example.workoutapp.Data.ProfileDao.WeightHistoryDao;
import com.example.workoutapp.Data.WorkoutDao.BASE_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_PRESET_NAME_TABLE_DAO;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.NutritionModels.MealModel;
import com.example.workoutapp.Models.ProfileModels.ActivityGoalModel;
import com.example.workoutapp.Models.ProfileModels.DailyActivityTrackingModel;
import com.example.workoutapp.Models.ProfileModels.DailyFoodTrackingModel;
import com.example.workoutapp.Models.ProfileModels.FoodGainGoalModel;
import com.example.workoutapp.Models.ProfileModels.GeneralGoalModel;
import com.example.workoutapp.Models.ProfileModels.UserProfileModel;
import com.example.workoutapp.Models.ProfileModels.WeightHistoryModel;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;

public class ChangeUploader {
    private static final String TAG = "SyncTaskProcessor";
    private final FirestoreSyncManager manager;
    private final ChangeElmDao changeDao;

    public ChangeUploader(FirestoreSyncManager manager) {
        this.manager = manager;
        this.changeDao = new ChangeElmDao(MainActivity.getAppDataBase());
    }

    public void process(ChangeElmDao.ChangeTask task) {
        final String uid = task.uid;
        final String type = task.type != null ? task.type : "";

        switch (type.toLowerCase()) {
            case "meal":
                handleMeal(uid);
                break;

            case "meal_preset":
                handleMealPreset(uid);
                break;

            case "workout_ex":
                handleWorkoutExercise(uid);
                break;

            case "base_exercise":
                handleBaseExercise(uid);
                break;

            case "workout_preset":
                handleWorkoutPreset(uid);
                break;

            case "user_profile":
                handleUserProfile();
                break;

            case "weight_history":
                handleWeightHistory(uid);
                break;

            case "activity_goal":
                handleActivityGoal(uid);
                break;

            case "general_goal":
                handleGeneralGoal(uid);
                break;

            case "food_goal":
                handleFoodGoal(uid);
                break;

            case "daily_food":
                handleDailyFood(uid);
                break;

            case "daily_activity":
                handleDailyActivity(uid);
                break;

            default:
                Log.w(TAG, "Неизвестный тип задачи: " + type);
                break;
        }
    }

    // --- Методы-обработчики ---

    private void handleMeal(String uid) {
        MealNameDao mealNameDao = new MealNameDao(MainActivity.getAppDataBase());
        ConnectingMealDao connDao = new ConnectingMealDao(MainActivity.getAppDataBase());
        MealFoodDao foodDao = new MealFoodDao(MainActivity.getAppDataBase());
        MealModel meal = mealNameDao.getMealByUid(uid, connDao, foodDao);

        if (meal != null) {
            manager.uploadMeal(meal);
        } else {
            changeDao.removeFromQueue(uid);
        }
    }

    private void handleMealPreset(String uid) {
        PresetMealNameDao presetDao = new PresetMealNameDao(MainActivity.getAppDataBase());
        ConnectingMealPresetDao connPresetDao = new ConnectingMealPresetDao(MainActivity.getAppDataBase());
        PresetEatDao presetEatDao = new PresetEatDao(MainActivity.getAppDataBase());
        MealModel preset = presetDao.getPresetMealByUid(uid, connPresetDao, presetEatDao);

        if (preset != null) {
            manager.syncMealPreset(preset);
        } else {
            changeDao.removeFromQueue(uid);
        }
    }

    private void handleWorkoutExercise(String uid) {
        WORKOUT_EXERCISE_TABLE_DAO workoutExDao = new WORKOUT_EXERCISE_TABLE_DAO(MainActivity.getAppDataBase());
        ExerciseModel exercise = workoutExDao.getExByUid(uid);
        if (exercise != null) {
            manager.syncSingleExercise(exercise);
        } else {
            changeDao.removeFromQueue(uid);
        }
    }

    private void handleBaseExercise(String uid) {
        BASE_EXERCISE_TABLE_DAO baseDao = new BASE_EXERCISE_TABLE_DAO(MainActivity.getAppDataBase());
        BaseExModel baseEx = baseDao.getExByUid(uid);
        if (baseEx != null) {
            manager.syncBaseExerciseChange(baseEx.getBase_ex_name(), baseEx);
        } else {
            changeDao.removeFromQueue(uid);
        }
    }
    //жоский кастыль с вызовом manager.presetWorkoutSync.uploadPreset
    private void handleWorkoutPreset(String uid) {
        WORKOUT_PRESET_NAME_TABLE_DAO presetDao = new WORKOUT_PRESET_NAME_TABLE_DAO(MainActivity.getAppDataBase());

        // Получаем данные пресета по UID (включая список упражнений/сетов)
        ExerciseModel presetData = presetDao.getPresetByUid(uid);

        if (presetData != null) {
            // Вызываем upload через менеджер
            manager.presetWorkoutSync.uploadPreset(
                    presetData.getExerciseName(),
                    uid,
                    presetData.getSets(),
                    null
            );
        } else {
            // Если данных нет в БД — удаляем битую ссылку из очереди
            changeDao.removeFromQueue(uid);
        }
    }

    private void handleUserProfile() {
        UserProfileDao userProfileDao = new UserProfileDao(MainActivity.getAppDataBase());
        UserProfileModel p = userProfileDao.getProfile();
        manager.syncProfileUpdate(p);
    }

    private void handleWeightHistory(String uid) {
        WeightHistoryDao weightDao = new WeightHistoryDao(MainActivity.getAppDataBase());
        WeightHistoryModel weightEntry = weightDao.getWeightByUid(uid);
        if (weightEntry != null) {
            manager.syncNewWeight(weightEntry);
        } else {
            changeDao.removeFromQueue(uid);
        }
    }

    private void handleActivityGoal(String uid) {
        ActivityGoalDao goalDao = new ActivityGoalDao(MainActivity.getAppDataBase());
        ActivityGoalModel goal = goalDao.getGoalByUid(uid);
        if (goal != null) {
            manager.uploadActivityGoal(goal);
        } else {
            changeDao.removeFromQueue(uid);
        }
    }

    private void handleGeneralGoal(String uid) {
        GeneralGoalDao goalDao = new GeneralGoalDao(MainActivity.getAppDataBase());
        GeneralGoalModel goal = goalDao.getGoalByUid(uid);
        if (goal != null) {
            manager.uploadGeneralGoal(goal);
        } else {
            changeDao.removeFromQueue(uid);
        }
    }

    private void handleFoodGoal(String uid) {
        FoodGainGoalDao goalDao = new FoodGainGoalDao(MainActivity.getAppDataBase());
        FoodGainGoalModel goal = goalDao.getGoalByUid(uid);
        if (goal != null) {
            manager.uploadFoodGoal(goal);
        } else {
            changeDao.removeFromQueue(uid);
        }
    }

    private void handleDailyFood(String uid) {
        DailyFoodTrackingDao foodDao = new DailyFoodTrackingDao(MainActivity.getAppDataBase());
        DailyFoodTrackingModel foodEntry = foodDao.getEntryByDate(uid);
        if (foodEntry != null) {
            manager.uploadDailyFood(foodEntry);
        } else {
            changeDao.removeFromQueue(uid);
        }
    }

    private void handleDailyActivity(String uid) {
        DailyActivityTrackingDao activityDao = new DailyActivityTrackingDao(MainActivity.getAppDataBase());
        DailyActivityTrackingModel entry = activityDao.getEntryByDate(uid);
        if (entry != null) {
            manager.uploadDailyActivity(entry);
        } else {
            changeDao.removeFromQueue(uid);
        }
    }
}