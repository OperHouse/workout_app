package com.example.workoutapp.Tools.SyncTools;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.workoutapp.Data.WorkoutDao.BASE_EXERCISE_TABLE_DAO;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseExerciseSync {
    private final FirebaseFirestore db;
    private final String userId;

    public BaseExerciseSync() {
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getUid();
    }

    public interface SyncCallback {
        void onSuccess();
        void onFailure(String error);
    }

    /**
     * Превращаем модель в карту для отправки, исключая локальный ID и временные состояния (isPressed)
     */
    private Map<String, Object> prepareDataForCloud(BaseExModel ex) {
        Map<String, Object> data = new HashMap<>();
        data.put("base_ex_uid", ex.getBase_ex_uid());
        data.put("base_ex_name", ex.getBase_ex_name());
        data.put("base_ex_type", ex.getBase_ex_type());
        data.put("base_ex_bodyType", ex.getBase_ex_bodyType());
        // id и isPressed НЕ добавляем
        return data;
    }

    /**
     * Синхронизация списка упражнений (массовая загрузка через Batch)
     */
    public void syncBaseExercises(List<BaseExModel> list, boolean isPublic) {
        if (userId == null || list == null || list.isEmpty()) return;

        WriteBatch batch = db.batch();

        for (BaseExModel ex : list) {
            if (ex.getBase_ex_uid() == null || ex.getBase_ex_uid().isEmpty()) continue;

            Map<String, Object> cloudData = prepareDataForCloud(ex);

            // 1. В личную коллекцию пользователя
            batch.set(db.collection("users").document(userId)
                    .collection("custom_exercises").document(ex.getBase_ex_uid()), cloudData, SetOptions.merge());

            // 2. В общую библиотеку, если отмечено
            if (isPublic) {
                cloudData.put("authorId", userId);
                batch.set(db.collection("library").document(ex.getBase_ex_uid()), cloudData, SetOptions.merge());
            }
        }

        batch.commit().addOnSuccessListener(aVoid -> Log.d(TAG, "Все упражнения успешно синхронизированы"))
                .addOnFailureListener(e -> Log.e(TAG, "Ошибка массовой синхронизации: " + e.getMessage()));
    }

    public void deleteBaseExercise(String uid, @Nullable SyncCallback callback) {
        if (userId == null || uid == null) {
            if (callback != null) {
                callback.onFailure("Invalid data or user not logged in");
            }
            return;
        }

        // Удаляем ТОЛЬКО из личной коллекции пользователя
        db.collection("users").document(userId)
                .collection("custom_exercises").document(uid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Упражнение удалено из приватного хранилища: " + uid);
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка удаления из приватного хранилища: " + e.getMessage());
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    public void syncBaseExerciseChange(String oldName, BaseExModel updatedEx, @Nullable SyncCallback callback) {
        if (userId == null) {
            if (callback != null) callback.onFailure("User not logged in");
            return;
        }

        String docId = updatedEx != null ? updatedEx.getBase_ex_uid() : formatId(oldName);

        if (updatedEx == null) {
            // Логика удаления
            db.collection("users").document(userId)
                    .collection("custom_exercises").document(docId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        if (callback != null) callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) callback.onFailure(e.getMessage());
                    });
            return;
        }

        Map<String, Object> cloudData = prepareDataForCloud(updatedEx);

        // Используем WriteBatch, чтобы оба сохранения (в личку и в библиотеку)
        // прошли как одна транзакция. Это надежнее.
        com.google.firebase.firestore.WriteBatch batch = db.batch();

        // 1. Личная коллекция
        DocumentReference userRef = db.collection("users").document(userId)
                .collection("custom_exercises").document(docId);
        batch.set(userRef, cloudData, SetOptions.merge());

        // 2. Общая библиотека
        cloudData.put("authorId", userId);
        DocumentReference libraryRef = db.collection("library").document(docId);
        batch.set(libraryRef, cloudData, SetOptions.merge());

        // Выполняем батч
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Sync", "Base exercise synced successfully: " + docId);
                    if (callback != null) callback.onSuccess(); // СИГНАЛ ДЛЯ УДАЛЕНИЯ ИЗ ОЧЕРЕДИ
                })
                .addOnFailureListener(e -> {
                    Log.e("Sync", "Base exercise sync failed", e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void restoreUserCustomExercises() {
        if (userId == null) return;
        BASE_EXERCISE_TABLE_DAO dao = new BASE_EXERCISE_TABLE_DAO(MainActivity.getAppDataBase());

        db.collection("users").document(userId).collection("custom_exercises").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        BaseExModel cloudEx = doc.toObject(BaseExModel.class);
                        if (cloudEx != null) {
                            // Теперь проверяем существование по UID, а не по имени!
                            if (!dao.isExerciseUidExists(cloudEx.getBase_ex_uid())) {
                                dao.addExercise(cloudEx);
                            }
                        }
                    }
                });
    }

    private String formatId(String name) {
        if (name == null || name.isEmpty()) return "unknown";
        return name.trim().toLowerCase().replaceAll("\\s+", "_");
    }
}