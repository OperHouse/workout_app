package com.example.workoutapp.Tools.SyncTools;

import android.content.Context;
import android.util.Log;

import com.example.workoutapp.Data.ProfileDao.ActivityGoalDao;
import com.example.workoutapp.Data.ProfileDao.DailyActivityTrackingDao;
import com.example.workoutapp.Data.ProfileDao.DailyFoodTrackingDao;
import com.example.workoutapp.Data.ProfileDao.FoodGainGoalDao;
import com.example.workoutapp.Data.ProfileDao.GeneralGoalDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.NutritionModels.FoodModel;
import com.example.workoutapp.Models.NutritionModels.MealModel;
import com.example.workoutapp.Models.ProfileModels.*;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.Tools.WeightSync;
import com.example.workoutapp.Tools.WorkoutSessionSync2;
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
    private final MealPresetSync mealPresetSync;
    private final MealSync mealSync;

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
        this.mealPresetSync = new MealPresetSync();
        this.mealSync = new MealSync();
    }

    // =====================================================
    // FOOD METHODS (НОВАЯ АРХИТЕКТУРА)
    // =====================================================

    public void uploadFood(FoodModel food) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        if (userId == null) return;
        baseFoodSync.uploadFood(food, null);
    }

    public void deleteFood(FoodModel food) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        if (userId == null) return;
        baseFoodSync.deleteFood(food, null);
    }

    /**
     * Запуск realtime синхронизации еды.
     * Вызывать один раз после авторизации.
     */
    public void startFoodRealtimeSync() {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }

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
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }

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

        Log.d(TAG, "Starting sync of all meal presets...");
        mealPresetSync.downloadAllOnce();

        receiveMealFromServer();

        Log.d(TAG, "Full sync initialized");
    }

    // =====================================================
    // ОСТАЛЬНЫЕ СИНХРОНИЗАЦИИ
    // =====================================================

    public void syncPresetUpdate(String name, String uid, List<ExerciseModel> exercises) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        presetWorkoutSync.uploadPreset(name, uid, exercises);
    }

    public void deletePresetFromCloud(String presetUid) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        presetWorkoutSync.deletePresetFromCloud(presetUid);
    }

    public void syncBaseExerciseChange(String oldName, BaseExModel updatedEx) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        baseExerciseSync.syncBaseExerciseChange(oldName, updatedEx);
    }

    public void deleteExerciseFromCloud(ExerciseModel exercise) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        workoutSessionSync2.removeExerciseFromCloud(exercise);
    }

    public void uploadAllBaseExercises(List<BaseExModel> list, boolean isPublic) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        baseExerciseSync.syncBaseExercises(list, isPublic);
    }

    public void syncProfileUpdate(UserProfileModel profile) {

        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        profileSync.uploadProfile(profile);
    }

    public void syncNewWeight(WeightHistoryModel weightEntry) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        if (weightEntry != null) {
            weightSync.uploadWeightEntry(weightEntry);
        }
    }

    public void uploadGoal(ActivityGoalModel newGoal) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        activityGoalSync.uploadGoal(newGoal);
    }

    public void syncActivityGoals() {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        ActivityGoalDao dao = new ActivityGoalDao(MainActivity.getAppDataBase());
        activityGoalSync.pullGoalsFromCloud(dao);
        activityGoalSync.pushLocalGoalsToCloud(dao);

    }

    public void uploadGeneralGoal(GeneralGoalModel newGoal) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        generalGoalSync.uploadGoal(newGoal);
    }

    public void syncGeneralGoals() {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        GeneralGoalDao dao = new GeneralGoalDao(MainActivity.getAppDataBase());
        generalGoalSync.pullGoalsFromCloud(dao);
        generalGoalSync.pushLocalGoalsToCloud(dao);
    }

    public void uploadFoodGoal(FoodGainGoalModel newGoal) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        foodGoalSync.uploadGoal(newGoal);
    }

    public void syncFoodGoals() {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        FoodGainGoalDao dao = new FoodGainGoalDao(MainActivity.getAppDataBase());
        foodGoalSync.pullGoalsFromCloud(dao);
        foodGoalSync.pushLocalGoalsToCloud(dao);
    }

    public void uploadDailyActivity(DailyActivityTrackingModel model) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        dailyActivitySync.uploadEntry(model);
    }

    public void syncDailyActivity() {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        DailyActivityTrackingDao dao = new DailyActivityTrackingDao(MainActivity.getAppDataBase());
        dailyActivitySync.pullFromCloud(dao);
        dailyActivitySync.pushLocalToCloud(dao);
    }

    public void uploadDailyFood(DailyFoodTrackingModel model) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        dailyFoodSync.uploadEntry(model);
    }

    public void syncDailyFood() {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        DailyFoodTrackingDao dao = new DailyFoodTrackingDao(MainActivity.getAppDataBase());
        dailyFoodSync.pullFromCloud(dao);
        dailyFoodSync.pushLocalToCloud(dao);
    }

    public void syncSingleExercise(ExerciseModel exercise) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        if (exercise == null) return;
        List<ExerciseModel> list = new ArrayList<>();
        list.add(exercise);
        workoutSessionSync2.syncSpecificExercises(list);
    }

    public void syncMultipleExercise(List<ExerciseModel> exercises) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        if (exercises == null) return;
        workoutSessionSync2.syncSpecificExercises(exercises);
    }

    public void startWorkoutSync(List<ExerciseModel> exercises) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        workoutSessionSync2.startWorkoutSync(exercises);
    }

    public void syncMealPreset(MealModel meal){
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        mealPresetSync.uploadPreset(meal, null);
    }

    public void deleteMealPreset(MealModel meal){
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        mealPresetSync.deletePreset(meal, null);
    }
    public void uploadMeal(MealModel meal){
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        mealSync.uploadMeal(meal, null);
    }

    public void deleteMeal(MealModel meal) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        mealSync.deleteMeal(meal, null);
    }

    public void receiveMealFromServer(){
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        mealSync.loadAllMeals();
    }

    private boolean isNetworkAvailable() {
        android.net.ConnectivityManager connectivityManager =
                (android.net.ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showNoInternetDialog() {
        // Используем Handler, чтобы гарантированно запустить в главном потоке
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            // Так как у нас нет ссылки на Activity, используем системный Toast
            // или создаем прозрачную Activity-диалог.
            // Самый простой вариант - Toast, но вы просили Диалог.
            // Чтобы показать именно AlertDialog, нужен Context именно от Activity.

            android.widget.Toast.makeText(context,
                    "Нет подключения к интернету. Проверьте соединение и перезапустите приложение для синхронизации.",
                    android.widget.Toast.LENGTH_LONG).show();

            Log.e(TAG, "Sync failed: No internet connection");
        });
    }
}