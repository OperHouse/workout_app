package com.example.workoutapp.Tools;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.workoutapp.Data.ProfileDao.ActivityGoalDao;
import com.example.workoutapp.Data.ProfileDao.DailyActivityTrackingDao;
import com.example.workoutapp.Data.ProfileDao.DailyFoodTrackingDao;
import com.example.workoutapp.Data.ProfileDao.FoodGainGoalDao;
import com.example.workoutapp.Data.ProfileDao.GeneralGoalDao;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_PRESET_NAME_TABLE_DAO;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.ProfileModels.ActivityGoalModel;
import com.example.workoutapp.Models.ProfileModels.DailyActivityTrackingModel;
import com.example.workoutapp.Models.ProfileModels.DailyFoodTrackingModel;
import com.example.workoutapp.Models.ProfileModels.FoodGainGoalModel;
import com.example.workoutapp.Models.ProfileModels.GeneralGoalModel;
import com.example.workoutapp.Models.ProfileModels.UserProfileModel;
import com.example.workoutapp.Models.ProfileModels.WeightHistoryModel;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreSyncManager {
    private final BaseExerciseSync baseExerciseSync;
    private final WorkoutSessionSync workoutSessionSync;
    private final ProfileSync profileSync;
    private final PresetWorkoutSync presetWorkoutSync;
    private final FirebaseFirestore db;
    private final String userId;
    private final WeightSync weightSync;
    private ActivityGoalSync activityGoalSync;
    private GeneralGoalSync generalGoalSync;
    private FoodGoalSync foodGoalSync;
    private DailyActivitySync dailyActivitySync;
    private DailyFoodSync dailyFoodSync;
    private final Context context;

    private boolean isSyncing = false;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 5;

    public FirestoreSyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getUid();
        this.baseExerciseSync = new BaseExerciseSync();
        this.workoutSessionSync = new WorkoutSessionSync();
        this.profileSync = new ProfileSync();
        this.weightSync = new WeightSync();
        this.presetWorkoutSync = new PresetWorkoutSync();
        this.activityGoalSync = new ActivityGoalSync();
        this.generalGoalSync = new GeneralGoalSync();
        this.foodGoalSync = new FoodGoalSync();
        this.dailyActivitySync = new DailyActivitySync();
        this.dailyFoodSync = new DailyFoodSync();
    }

    // Сохранил оригинальное название
    public void startFullSynchronization(List<ExerciseModel> localExercises) {
        if (userId == null || isSyncing) return;

        isSyncing = true;
        Log.d("SyncManager", "Запущена полная синхронизация (попытка " + (retryCount + 1) + ")");
//        if (!isNetworkAvailable()) {
//            Log.d("SyncManager", "Синхронизация отменена: нет интернета");
//            // Можно сразу показать Toast или просто тихо выйти
//            return;
//        }

        // 1. Восстановление справочников
        baseExerciseSync.restoreUserCustomExercises();
        profileSync.syncProfile();
        weightSync.syncWeightHistory();
        syncActivityGoals();
        syncGeneralGoals();
        syncFoodGoals();
        syncDailyActivity();
        syncDailyFood();

        // 2. Запрос к коллекции тренировок
        db.collection("users").document(userId).collection("workouts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    retryCount = 0; // Сброс при успехе

                    Map<String, DocumentSnapshot> cloudMap = new HashMap<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        cloudMap.put(doc.getId(), doc);
                    }

                    workoutSessionSync.startWorkoutSync(localExercises, cloudMap);

                    WORKOUT_PRESET_NAME_TABLE_DAO presetDao = new WORKOUT_PRESET_NAME_TABLE_DAO(MainActivity.getAppDataBase());
                    presetWorkoutSync.pullPresetsFromCloud(presetDao);
                    presetWorkoutSync.pushLocalPresetsToCloud(presetDao);

                    isSyncing = false;
                    Log.d("SyncManager", "Синхронизация завершена успешно.");
                })
                .addOnFailureListener(e -> {
                    isSyncing = false;
                    if (retryCount < MAX_RETRIES) {
                        retryCount++;
                        long delay = (long) Math.pow(2, retryCount) * 1000;
                        Log.w("SyncManager", "Ошибка связи. Повтор через " + delay + "мс");

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            startFullSynchronization(localExercises); // Рекурсивный вызов оригинала
                        }, delay);
                    } else {
                        retryCount = 0;
                        Log.e("SyncManager", "Не удалось синхронизировать данные: " + e.getMessage());

                        // Сообщение пользователю
                        new Handler(Looper.getMainLooper()).post(() ->
                                Toast.makeText(context,
                                        "Нет связи с сервером. Данные не синхронизированы.",
                                        Toast.LENGTH_LONG).show());
                    }
                });
    }

    // --- Остальные методы БЕЗ изменений в названиях ---

    public void syncPresetUpdate(String name, String uid, List<ExerciseModel> exercises) {
        presetWorkoutSync.uploadPreset(name, uid, exercises);
    }

    public void deletePresetFromCloud(String presetUid) {
        presetWorkoutSync.deletePresetFromCloud(presetUid);
    }

    public void syncBaseExerciseChange(String oldName, BaseExModel updatedEx) {
        baseExerciseSync.syncBaseExerciseChange(oldName, updatedEx);
    }

    public void syncAllWorkouts(List<ExerciseModel> allExercises) {
        workoutSessionSync.syncAllWorkouts(allExercises);
    }

    public void deleteExerciseFromCloud(ExerciseModel exercise) {
        workoutSessionSync.removeExerciseFromCloud(exercise);
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
        ActivityGoalDao goalDao = new ActivityGoalDao(MainActivity.getAppDataBase());
        activityGoalSync.pullGoalsFromCloud(goalDao);
        activityGoalSync.pushLocalGoalsToCloud(goalDao);
    }

    public void uploadGeneralGoal(GeneralGoalModel newGoal) {
        generalGoalSync.uploadGoal(newGoal);
    }

    public void syncGeneralGoals() {
        GeneralGoalDao goalDao = new GeneralGoalDao(MainActivity.getAppDataBase());
        generalGoalSync.pullGoalsFromCloud(goalDao);
        generalGoalSync.pushLocalGoalsToCloud(goalDao);
    }

    public void uploadFoodGoal(FoodGainGoalModel newGoal) {
        FoodGainGoalDao goalDao = new FoodGainGoalDao(MainActivity.getAppDataBase());
        foodGoalSync.pullGoalsFromCloud(goalDao);
        foodGoalSync.pushLocalGoalsToCloud(goalDao);
    }

    public void syncFoodGoals() {
        FoodGainGoalDao goalDao = new FoodGainGoalDao(MainActivity.getAppDataBase());
        foodGoalSync.pullGoalsFromCloud(goalDao);
        foodGoalSync.pushLocalGoalsToCloud(goalDao);
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

    public void updateExerciseSets(ExerciseModel exercise) {
        if (workoutSessionSync != null) {
            workoutSessionSync.updateExerciseSetsInCloud(exercise);
        }
    }

    public void uploadWorkoutSession(ExerciseModel exercise) {
        if (workoutSessionSync != null) {
            List<ExerciseModel> singleExList = Collections.singletonList(exercise);
            com.example.workoutapp.Models.Helpers.WorkoutSessionModel session =
                    new com.example.workoutapp.Models.Helpers.WorkoutSessionModel(exercise.getEx_Data(), singleExList);
            workoutSessionSync.uploadWorkoutSession(session);
        }
    }
    private boolean isNetworkAvailable() {
        android.net.ConnectivityManager connectivityManager
                = (android.net.ConnectivityManager) context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}