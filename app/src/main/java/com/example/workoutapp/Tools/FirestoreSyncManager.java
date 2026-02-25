package com.example.workoutapp.Tools;

import com.example.workoutapp.Models.ProfileModels.UserProfileModel;
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

    public FirestoreSyncManager() {
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getUid();
        this.baseExerciseSync = new BaseExerciseSync();
        this.workoutSessionSync = new WorkoutSessionSync();
        this.profileSync = new ProfileSync();
    }

    public void startFullSynchronization(List<ExerciseModel> localExercises) {
        if (userId == null) return;

        // Восстановление справочника
        baseExerciseSync.restoreUserCustomExercises();
        profileSync.syncProfile();

        // Синхронизация тренировок (2 аргумента)
        db.collection("users").document(userId).collection("workouts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, DocumentSnapshot> cloudMap = new HashMap<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        cloudMap.put(doc.getId(), doc);
                    }
                    workoutSessionSync.startWorkoutSync(localExercises, cloudMap);
                });
    }

    public void syncBaseExerciseChange(String oldName, BaseExModel updatedEx) {
        baseExerciseSync.syncBaseExerciseChange(oldName, updatedEx);
    }

    public void syncAllWorkouts(List<ExerciseModel> allExercises) {
        // Теперь метод найден в workoutSessionSync
        workoutSessionSync.syncAllWorkouts(allExercises);
    }

    public void uploadAllBaseExercises(List<BaseExModel> list, boolean isPublic) {
        baseExerciseSync.syncBaseExercises(list, isPublic);
    }

    public void syncProfileUpdate(UserProfileModel profile) {
        profileSync.uploadProfile(profile);
    }
}