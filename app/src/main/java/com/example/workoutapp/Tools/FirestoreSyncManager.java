package com.example.workoutapp.Tools;

import android.util.Log;

import com.example.workoutapp.Data.ProfileDao.ActivityGoalDao;
import com.example.workoutapp.Data.ProfileDao.DailyActivityTrackingDao;
import com.example.workoutapp.Data.ProfileDao.FoodGainGoalDao;
import com.example.workoutapp.Data.ProfileDao.GeneralGoalDao;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_PRESET_NAME_TABLE_DAO;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.ProfileModels.ActivityGoalModel;
import com.example.workoutapp.Models.ProfileModels.DailyActivityTrackingModel;
import com.example.workoutapp.Models.ProfileModels.FoodGainGoalModel;
import com.example.workoutapp.Models.ProfileModels.GeneralGoalModel;
import com.example.workoutapp.Models.ProfileModels.UserProfileModel;
import com.example.workoutapp.Models.ProfileModels.WeightHistoryModel;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreSyncManager {
    private final BaseExerciseSync baseExerciseSync;
    private final WorkoutSessionSync workoutSessionSync;
    private final ProfileSync profileSync;
    private final PresetWorkoutSync presetWorkoutSync; // Добавлено поле
    private final FirebaseFirestore db;
    private final String userId;
    private final WeightSync weightSync;
    private ActivityGoalSync activityGoalSync;
    private GeneralGoalSync generalGoalSync;
    private FoodGoalSync foodGoalSync;
    private DailyActivitySync dailyActivitySync;

    public FirestoreSyncManager() {
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getUid();
        this.baseExerciseSync = new BaseExerciseSync();
        this.workoutSessionSync = new WorkoutSessionSync();
        this.profileSync = new ProfileSync();
        this.weightSync = new WeightSync();
        this.presetWorkoutSync = new PresetWorkoutSync(); // Инициализация
        this.activityGoalSync = new ActivityGoalSync();
        this.generalGoalSync = new GeneralGoalSync();
        this.foodGoalSync = new FoodGoalSync();
        this.dailyActivitySync = new DailyActivitySync();
    }

    private boolean isSyncing = false;

    public void startFullSynchronization(List<ExerciseModel> localExercises) {
        if (userId == null || isSyncing) return;

        isSyncing = true;
        Log.d("SyncManager", "Запущена полная синхронизация...");

        // 1. Восстановление справочников и профиля (синхронные или быстрые вызовы)
        baseExerciseSync.restoreUserCustomExercises();
        profileSync.syncProfile();
        weightSync.syncWeightHistory();

        syncActivityGoals();
        syncGeneralGoals();
        syncFoodGoals();
        syncDailyActivity();

        // 2. Запрос к коллекции тренировок
        db.collection("users").document(userId).collection("workouts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // ОБЪЯВЛЯЕМ cloudMap ЗДЕСЬ
                    Map<String, DocumentSnapshot> cloudMap = new HashMap<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        cloudMap.put(doc.getId(), doc);
                    }

                    // Передаем cloudMap в синхронизатор сессий
                    workoutSessionSync.startWorkoutSync(localExercises, cloudMap);
                    Log.d("SyncManager", "Синхронизация тренировок выполнена.");

                    // 3. СИНХРОНИЗАЦИЯ ПРЕСЕТОВ
                    // Создаем DAO через MainActivity (убедись, что метод getAppDataBase() доступен)
                    WORKOUT_PRESET_NAME_TABLE_DAO presetDao = new WORKOUT_PRESET_NAME_TABLE_DAO(MainActivity.getAppDataBase());

                    // ШАГ А: Скачиваем новые пресеты из облака
                    presetWorkoutSync.pullPresetsFromCloud(presetDao);

                    // ШАГ Б: Отправляем локальные пресеты, которых нет в облаке
                    presetWorkoutSync.pushLocalPresetsToCloud(presetDao);

                    isSyncing = false;
                    Log.d("SyncManager", "Полная синхронизация (включая пресеты) завершена.");
                })
                .addOnFailureListener(e -> {
                    isSyncing = false;
                    Log.e("SyncManager", "Ошибка синхронизации: " + e.getMessage());
                });
    }

    // --- Методы для работы с ПРЕСЕТАМИ ---

    /**
     * Вызывай этот метод в Activity/Fragment при создании или редактировании пресета
     */
    public void syncPresetUpdate(String name, String uid, List<ExerciseModel> exercises) {
        presetWorkoutSync.uploadPreset(name, uid, exercises);
    }

    /**
     * Вызывай этот метод при удалении пресета пользователем
     */
    public void deletePresetFromCloud(String presetUid) {
        presetWorkoutSync.deletePresetFromCloud(presetUid);
    }

    // --- Существующие методы ---

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

        // 1. Сначала тянем новое из облака
        activityGoalSync.pullGoalsFromCloud(goalDao);

        // 2. Затем выталкиваем то, чего нет в облаке, из локальной базы
        activityGoalSync.pushLocalGoalsToCloud(goalDao);
    }

    public void uploadGeneralGoal(GeneralGoalModel newGoal) {
        generalGoalSync.uploadGoal(newGoal);

    }

    public void syncGeneralGoals() {
        GeneralGoalDao goalDao = new GeneralGoalDao(MainActivity.getAppDataBase());
        // Загружаем из облака
        generalGoalSync.pullGoalsFromCloud(goalDao);
        // Выгружаем локальные
        generalGoalSync.pushLocalGoalsToCloud(goalDao);
    }

    public void uploadFoodGoal(FoodGainGoalModel newGoal) {
        FoodGainGoalDao goalDao = new FoodGainGoalDao(MainActivity.getAppDataBase());
        // 1. Сначала скачиваем (Pull)
        foodGoalSync.pullGoalsFromCloud(goalDao);
        // 2. Затем выгружаем локальные (Push)
        foodGoalSync.pushLocalGoalsToCloud(goalDao);
    }

    public void syncFoodGoals() {
        FoodGainGoalDao goalDao = new FoodGainGoalDao(MainActivity.getAppDataBase());

        // 1. Сначала скачиваем новые данные из облака
        foodGoalSync.pullGoalsFromCloud(goalDao);

        // 2. Затем выгружаем локальные данные, которых нет в облаке
        foodGoalSync.pushLocalGoalsToCloud(goalDao);

        Log.d("SyncManager", "Синхронизация целей питания завершена.");
    }

    public void uploadDailyActivity(DailyActivityTrackingModel model) {
        dailyActivitySync.uploadEntry(model);
    }

    // Добавить метод полной синхронизации:
    public void syncDailyActivity() {
        DailyActivityTrackingDao dao = new DailyActivityTrackingDao(MainActivity.getAppDataBase());
        dailyActivitySync.pullFromCloud(dao);
        dailyActivitySync.pushLocalToCloud(dao);
    }
}