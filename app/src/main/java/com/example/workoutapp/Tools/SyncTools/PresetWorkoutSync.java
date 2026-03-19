package com.example.workoutapp.Tools.SyncTools;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.workoutapp.Data.WorkoutDao.WORKOUT_PRESET_NAME_TABLE_DAO;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PresetWorkoutSync {
    private final FirebaseFirestore db;
    private final String userId;
    private final String TAG = "PresetSync";

    public PresetWorkoutSync() {
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getUid();
    }

    public interface SyncCallback {
        void onSuccess();
        void onFailure(String error);
    }

    /**
     * Отправка или обновление пресета в облаке (ОБНОВЛЕНО: добавлен callback)
     */
    public void uploadPreset(String presetName, String presetUid, List<?> exercises, @Nullable SyncCallback callback) {
        if (userId == null || presetUid == null) {
            if (callback != null) callback.onFailure("User ID or UID is null");
            return;
        }

        DocumentReference docRef = db.collection("users").document(userId)
                .collection("presets").document(presetUid);

        List<Map<String, Object>> lightExercises = new ArrayList<>();

        if (exercises != null) {
            for (Object obj : exercises) {
                Map<String, Object> map = new HashMap<>();
                if (obj instanceof ExerciseModel) {
                    ExerciseModel ex = (ExerciseModel) obj;
                    map.put("exercise_uid", ex.getExercise_uid());
                    map.put("exerciseName", ex.getExerciseName());
                    map.put("exerciseType", ex.getExerciseType());
                    map.put("exerciseBodyType", ex.getExerciseBodyType());
                } else if (obj instanceof BaseExModel) {
                    BaseExModel be = (BaseExModel) obj;
                    map.put("exerciseName", be.getBase_ex_name());
                    map.put("exerciseType", be.getBase_ex_type());
                    map.put("exerciseBodyType", be.getBase_ex_bodyType());
                    map.put("exercise_uid", be.getBase_ex_uid());
                }
                if (!map.isEmpty()) lightExercises.add(map);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("preset_uid", presetUid);
        data.put("presetName", presetName);
        data.put("exercises_list", lightExercises);

        docRef.set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Пресет синхронизирован: " + presetName);
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка синхронизации: " + e.getMessage());
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    /**
     * Загрузка всех пресетов из облака (Pull)
     */
    public void pullPresetsFromCloud(WORKOUT_PRESET_NAME_TABLE_DAO presetDao) {
        if (userId == null || presetDao == null) return;

        db.collection("users").document(userId).collection("presets")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Найдено пресетов в облаке: " + queryDocumentSnapshots.size());
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        parseAndSavePreset(doc, presetDao);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Ошибка при загрузке пресетов: " + e.getMessage()));
    }

    private void parseAndSavePreset(DocumentSnapshot doc, WORKOUT_PRESET_NAME_TABLE_DAO presetDao) {
        String uid = doc.getString("preset_uid");
        String name = doc.getString("presetName");

        // Получаем список упражнений из документа
        List<Map<String, Object>> rawList = (List<Map<String, Object>>) doc.get("exercises_list");

        if (uid == null || name == null) return;

        List<ExerciseModel> exercises = new ArrayList<>();
        if (rawList != null) {
            for (Map<String, Object> item : rawList) {
                ExerciseModel ex = new ExerciseModel();
                ex.setExercise_uid((String) item.get("exercise_uid"));
                ex.setExerciseName((String) item.get("exerciseName"));
                ex.setExerciseType((String) item.get("exerciseType"));
                ex.setExerciseBodyType((String) item.get("exerciseBodyType"));
                // Сеты для пресета всегда остаются пустыми
                exercises.add(ex);
            }
        }

        // Вызываем комплексный метод сохранения в DAO, который мы обсуждали ранее
        presetDao.savePresetFromCloud(name, uid, exercises);
    }

    /**
     * Удаление пресета из облака (ОБНОВЛЕНО: добавлен callback)
     */
    public void deletePresetFromCloud(String presetUid, @Nullable SyncCallback callback) {
        if (userId == null || presetUid == null) {
            if (callback != null) callback.onFailure("Invalid data");
            return;
        }

        db.collection("users").document(userId)
                .collection("presets")
                .document(presetUid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Пресет удален из облака");
                    if (callback != null) callback.onSuccess(); // СИГНАЛ ДЛЯ ОЧИСТКИ ОЧЕРЕДИ УДАЛЕНИЯ
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка удаления пресета: " + e.getMessage());
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }
    /**
     * Проходит по всем локальным пресетам и отправляет их в облако
     */
//    public void pushLocalPresetsToCloud(WORKOUT_PRESET_NAME_TABLE_DAO presetDao) {
//        if (userId == null || presetDao == null) return;
//
//        // Получаем список всех пресетов из SQLite
//        List<ExerciseModel> localPresets = presetDao.getAllPresets();
//
//        for (ExerciseModel preset : localPresets) {
//            String uid = preset.getExercise_uid();
//
//            // Отправляем только те, у которых есть UID
//            if (uid != null && !uid.isEmpty()) {
//                // В ExerciseModel пресета в поле sets обычно лежат BaseExModel
//                // Нам нужно привести их к списку для отправки
//                uploadPreset(preset.getExerciseName(), uid, (List<ExerciseModel>)(Object)preset.getSets());
//            }
//        }
//    }
}