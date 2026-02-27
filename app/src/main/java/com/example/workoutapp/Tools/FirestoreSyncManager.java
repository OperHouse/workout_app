package com.example.workoutapp.Tools;

import android.util.Log;

import com.example.workoutapp.Data.WorkoutDao.WORKOUT_PRESET_NAME_TABLE_DAO;
import com.example.workoutapp.MainActivity;
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

    public FirestoreSyncManager() {
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getUid();
        this.baseExerciseSync = new BaseExerciseSync();
        this.workoutSessionSync = new WorkoutSessionSync();
        this.profileSync = new ProfileSync();
        this.weightSync = new WeightSync();
        this.presetWorkoutSync = new PresetWorkoutSync(); // Инициализация
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
}