package com.example.workoutapp.Tools;

import com.example.workoutapp.Data.WorkoutDao.BASE_EXERCISE_TABLE_DAO;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

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

    // Массовая синхронизация списка упражнений
    public void syncBaseExercises(List<BaseExModel> localBaseExercises, boolean isPublicSyncEnabled) {
        if (userId == null) return;
        for (BaseExModel ex : localBaseExercises) {
            syncBaseExerciseChange(null, ex); // null, так как это начальная выгрузка
            // Логика публичной библиотеки (опционально)
            if (isPublicSyncEnabled) {
                db.collection("library").document(formatId(ex.getExName())).set(ex, SetOptions.merge());
            }
        }
    }

    // Одиночное изменение/удаление/добавление
    public void syncBaseExerciseChange(String oldName, BaseExModel updatedEx) {
        if (userId == null) return;

        String oldDocId = formatId(oldName);

        // 1. СЛУЧАЙ УДАЛЕНИЯ
        if (updatedEx == null) {
            if (oldDocId != null) {
                db.collection("users").document(userId).collection("custom_exercises").document(oldDocId).delete();
                // Из библиотеки тоже удаляем, если нужно (или оставляем для других)
                // db.collection("library").document(oldDocId).delete();
            }
            return;
        }

        // 2. СЛУЧАЙ ДОБАВЛЕНИЯ ИЛИ ИЗМЕНЕНИЯ
        String newDocId = formatId(updatedEx.getExName());

        // Если имя изменилось, удаляем старый документ из обеих коллекций
        if (oldDocId != null && !oldDocId.equals(newDocId)) {
            db.collection("users").document(userId).collection("custom_exercises").document(oldDocId).delete();
            db.collection("library").document(oldDocId).delete();
        }

        // --- ЗАПИСЬ В ЛИЧНУЮ КОЛЛЕКЦИЮ (как обычно) ---
        db.collection("users").document(userId)
                .collection("custom_exercises")
                .document(newDocId)
                .set(updatedEx, SetOptions.merge());

        // --- ЗАПИСЬ В ОБЩУЮ БИБЛИОТЕКУ (с добавлением автора) ---
        // Создаем карту (Map), чтобы добавить поле, которого нет в модели
        Map<String, Object> publicData = new HashMap<>();
        publicData.put("exName", updatedEx.getExName());
        publicData.put("exType", updatedEx.getExType());
        publicData.put("bodyType", updatedEx.getBodyType());
        publicData.put("base_ex_id", updatedEx.getBase_ex_id());
        publicData.put("isPressed", updatedEx.getIsPressed());
        publicData.put("authorId", userId); // Добавляем автора!

        db.collection("library")
                .document(newDocId)
                .set(publicData, SetOptions.merge());
    }

    public void restoreUserCustomExercises() {
        if (userId == null) return;
        BASE_EXERCISE_TABLE_DAO dao = new BASE_EXERCISE_TABLE_DAO(MainActivity.getAppDataBase());

        db.collection("users").document(userId).collection("custom_exercises").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        BaseExModel cloudEx = doc.toObject(BaseExModel.class);
                        if (cloudEx != null) {
                            if (!isLocalExerciseExists(dao, cloudEx.getExName())) {
                                dao.addExercise(cloudEx);
                            }
                        }
                    }
                });
    }

    private String formatId(String name) {
        if (name == null || name.isEmpty()) return null;
        return name.trim().toLowerCase().replaceAll("\\s+", "_");
    }

    private boolean isLocalExerciseExists(BASE_EXERCISE_TABLE_DAO dao, String name) {
        List<BaseExModel> all = dao.getAllExercises();
        for (BaseExModel ex : all) {
            if (ex.getExName().equalsIgnoreCase(name)) return true;
        }
        return false;
    }
}