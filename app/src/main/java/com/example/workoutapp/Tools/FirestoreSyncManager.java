package com.example.workoutapp.Tools;

import android.content.Context;
import android.util.Log;

import com.example.workoutapp.Data.ProfileDao.ActivityGoalDao;
import com.example.workoutapp.Data.ProfileDao.DailyActivityTrackingDao;
import com.example.workoutapp.Data.ProfileDao.DailyFoodTrackingDao;
import com.example.workoutapp.Data.ProfileDao.FoodGainGoalDao;
import com.example.workoutapp.Data.ProfileDao.GeneralGoalDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.NutritionModels.FoodModel;
import com.example.workoutapp.Models.ProfileModels.*;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class FirestoreSyncManager {

    private static final String TAG = "FirestoreSyncManager";

    private final FirebaseFirestore db;
    private final String userId;

    private final BaseExerciseSync baseExerciseSync;
    private final ProfileSync profileSync;
    private final PresetWorkoutSync presetWorkoutSync;
    private final WeightSync weightSync;
    private final ActivityGoalSync activityGoalSync;
    private final GeneralGoalSync generalGoalSync;
    private final FoodGoalSync foodGoalSync;
    private final DailyActivitySync dailyActivitySync;
    private final DailyFoodSync dailyFoodSync;
    private final WorkoutSessionSync2 workoutSessionSync2;
    private final BaseFoodSync baseFoodSync;

    private ListenerRegistration foodListener;

    private final Context context;

    public FirestoreSyncManager(Context context) {

        this.context = context.getApplicationContext();
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getUid();

        this.baseExerciseSync = new BaseExerciseSync();
        this.profileSync = new ProfileSync();
        this.weightSync = new WeightSync();
        this.presetWorkoutSync = new PresetWorkoutSync();
        this.activityGoalSync = new ActivityGoalSync();
        this.generalGoalSync = new GeneralGoalSync();
        this.foodGoalSync = new FoodGoalSync();
        this.dailyActivitySync = new DailyActivitySync();
        this.dailyFoodSync = new DailyFoodSync();
        this.workoutSessionSync2 = new WorkoutSessionSync2();
        this.baseFoodSync = new BaseFoodSync();
    }

    // =====================================================
    // FOOD METHODS (НОВАЯ АРХИТЕКТУРА)
    // =====================================================

    public void uploadFood(FoodModel food) {
        if (userId == null) return;
        baseFoodSync.uploadFood(food, null);
    }

    public void deleteFood(FoodModel food) {
        if (userId == null) return;
        baseFoodSync.deleteFood(food, null);
    }

    /**
     * Запуск realtime синхронизации еды.
     * Вызывать один раз после авторизации.
     */
    public void startFoodRealtimeSync() {

        if (userId == null) return;

        if (foodListener != null) {
            foodListener.remove();
        }

        foodListener = baseFoodSync.startRealtimeSync(
                new com.example.workoutapp.Data.NutritionDao.BaseEatDao(
                        MainActivity.getAppDataBase()
                )
        );

        Log.d(TAG, "Food realtime sync started");
    }

    public void stopFoodRealtimeSync() {
        if (foodListener != null) {
            foodListener.remove();
            foodListener = null;
        }
    }

    // =====================================================
    // ПОЛНАЯ СИНХРОНИЗАЦИЯ (БЕЗ enableNetwork)
    // =====================================================

    public void startFullSynchronization(List<ExerciseModel> localExercises) {

        if (userId == null) return;

        Log.d(TAG, "Starting full synchronization...");

        baseExerciseSync.restoreUserCustomExercises();
        profileSync.syncProfile();
        weightSync.syncWeightHistory();

        syncActivityGoals();
        syncGeneralGoals();
        syncFoodGoals();
        syncDailyActivity();
        syncDailyFood();

        startWorkoutSync(localExercises);

        // ЕДА теперь синкается через realtime listener
        startFoodRealtimeSync();

        Log.d(TAG, "Full sync initialized");
    }

    // =====================================================
    // ОСТАЛЬНЫЕ СИНХРОНИЗАЦИИ
    // =====================================================

    public void syncPresetUpdate(String name, String uid, List<ExerciseModel> exercises) {
        presetWorkoutSync.uploadPreset(name, uid, exercises);
    }

    public void deletePresetFromCloud(String presetUid) {
        presetWorkoutSync.deletePresetFromCloud(presetUid);
    }

    public void syncBaseExerciseChange(String oldName, BaseExModel updatedEx) {
        baseExerciseSync.syncBaseExerciseChange(oldName, updatedEx);
    }

    public void deleteExerciseFromCloud(ExerciseModel exercise) {
        workoutSessionSync2.removeExerciseFromCloud(exercise);
    }

    public void uploadAllBaseExercises(List<BaseExModel> list, boolean isPublic) {
        baseExerciseSync.syncBaseExercises(list, isPublic);
    }

    public void syncProfileUpdate(UserProfileModel profile) {
        profileSync.uploadProfile(profile);
    }

    public void syncNewWeight(WeightHistoryModel weightEntry) {
        if (weightEntry != null) {
            weightSync.uploadWeightEntry(weightEntry);
        }
    }

    public void uploadGoal(ActivityGoalModel newGoal) {
        activityGoalSync.uploadGoal(newGoal);
    }

    public void syncActivityGoals() {
        ActivityGoalDao dao = new ActivityGoalDao(MainActivity.getAppDataBase());
        activityGoalSync.pullGoalsFromCloud(dao);
        activityGoalSync.pushLocalGoalsToCloud(dao);
    }

    public void uploadGeneralGoal(GeneralGoalModel newGoal) {
        generalGoalSync.uploadGoal(newGoal);
    }

    public void syncGeneralGoals() {
        GeneralGoalDao dao = new GeneralGoalDao(MainActivity.getAppDataBase());
        generalGoalSync.pullGoalsFromCloud(dao);
        generalGoalSync.pushLocalGoalsToCloud(dao);
    }

    public void uploadFoodGoal(FoodGainGoalModel newGoal) {
        foodGoalSync.uploadGoal(newGoal);
    }

    public void syncFoodGoals() {
        FoodGainGoalDao dao = new FoodGainGoalDao(MainActivity.getAppDataBase());
        foodGoalSync.pullGoalsFromCloud(dao);
        foodGoalSync.pushLocalGoalsToCloud(dao);
    }

    public void uploadDailyActivity(DailyActivityTrackingModel model) {
        dailyActivitySync.uploadEntry(model);
    }

    public void syncDailyActivity() {
        DailyActivityTrackingDao dao = new DailyActivityTrackingDao(MainActivity.getAppDataBase());
        dailyActivitySync.pullFromCloud(dao);
        dailyActivitySync.pushLocalToCloud(dao);
    }

    public void uploadDailyFood(DailyFoodTrackingModel model) {
        dailyFoodSync.uploadEntry(model);
    }

    public void syncDailyFood() {
        DailyFoodTrackingDao dao = new DailyFoodTrackingDao(MainActivity.getAppDataBase());
        dailyFoodSync.pullFromCloud(dao);
        dailyFoodSync.pushLocalToCloud(dao);
    }

    public void syncSingleExercise(ExerciseModel exercise) {
        if (exercise == null) return;
        List<ExerciseModel> list = new ArrayList<>();
        list.add(exercise);
        workoutSessionSync2.syncSpecificExercises(list);
    }

    public void syncMultipleExercise(List<ExerciseModel> exercises) {
        if (exercises == null) return;
        workoutSessionSync2.syncSpecificExercises(exercises);
    }

    public void startWorkoutSync(List<ExerciseModel> exercises) {
        workoutSessionSync2.startWorkoutSync(exercises);
    }
}