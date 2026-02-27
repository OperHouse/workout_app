package com.example.workoutapp.Tools;

import android.util.Log;

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
    }

    private boolean isSyncing = false; // Добавь флаг, чтобы не запускать две синхронизации сразу

    public void startFullSynchronization(List<ExerciseModel> localExercises) {
        if (userId == null || isSyncing) return; // Если уже синхронизируем — выходим

        isSyncing = true;
        Log.d("SyncManager", "Запущена полная синхронизация...");

        // Восстановление справочника и профиля
        baseExerciseSync.restoreUserCustomExercises();
        profileSync.syncProfile();
        weightSync.syncWeightHistory();

        // Синхронизация тренировок
        db.collection("users").document(userId).collection("workouts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, DocumentSnapshot> cloudMap = new HashMap<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        cloudMap.put(doc.getId(), doc);
                    }
                    workoutSessionSync.startWorkoutSync(localExercises, cloudMap);
                    isSyncing = false; // Освобождаем менеджер
                    Log.d("SyncManager", "Синхронизация тренировок завершена.");
                })
                .addOnFailureListener(e -> {
                    isSyncing = false;
                    Log.e("SyncManager", "Ошибка синхронизации: " + e.getMessage());
                });
    }

    public void syncBaseExerciseChange(String oldName, BaseExModel updatedEx) {
        baseExerciseSync.syncBaseExerciseChange(oldName, updatedEx);
    }

    public void syncAllWorkouts(List<ExerciseModel> allExercises) {
        // Теперь метод найден в workoutSessionSync
        workoutSessionSync.syncAllWorkouts(allExercises);
    }

    public void deleteExerciseFromCloud(ExerciseModel exercise) {
        // workoutSessionSync — это экземпляр WorkoutSessionSync внутри менеджера
        workoutSessionSync.removeExerciseFromCloud(exercise);
    }

    public void uploadAllBaseExercises(List<BaseExModel> list, boolean isPublic) {
        baseExerciseSync.syncBaseExercises(list, isPublic);
    }

    public void syncProfileUpdate(UserProfileModel profile) {
        profileSync.uploadProfile(profile);
    }

    /**
     * Метод для мгновенной отправки нового замера веса
     */
    public void syncNewWeight(WeightHistoryModel weightEntry) {
        if (weightEntry != null) {
            weightSync.uploadWeightEntry(weightEntry);
        }
    }

}