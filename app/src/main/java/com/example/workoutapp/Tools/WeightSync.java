package com.example.workoutapp.Tools;

import android.util.Log;
import com.example.workoutapp.Data.ProfileDao.WeightHistoryDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.ProfileModels.WeightHistoryModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeightSync {
    private final FirebaseFirestore db;
    private final String userId;
    private static final String TAG = "WeightSync";

    public WeightSync() {
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getUid();
    }

    /**
     * Создаем карту данных, где ключи СТРОГО совпадают с именами полей в WeightHistoryModel.
     * Это позволит методу doc.toObject() нативно собирать объект обратно.
     */
    private Map<String, Object> prepareWeightMap(WeightHistoryModel weight) {
        Map<String, Object> map = new HashMap<>();
        // Используем в точности те имена, которые у тебя в модели:
        map.put("weight_history_uid", weight.getWeight_history_uid());
        map.put("weight_history_measurementDate", weight.getWeight_history_measurementDate());
        map.put("weight_history_value", weight.getWeight_history_value());
        // weight_history_id НЕ добавляем, так как он локальный (@Exclude)
        return map;
    }

    public void uploadWeightEntry(WeightHistoryModel weightEntry) {
        if (userId == null || weightEntry == null) return;

        if (weightEntry.getWeight_history_uid() == null || weightEntry.getWeight_history_uid().isEmpty()) {
            Log.e(TAG, "Ошибка: Попытка отправить запись без UID!");
            return;
        }

        // Вместо передачи объекта weightEntry, передаем подготовленную Map
        db.collection("users").document(userId)
                .collection("weight_history")
                .document(weightEntry.getWeight_history_uid())
                .set(prepareWeightMap(weightEntry), SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Вес успешно ушел в облако: " + weightEntry.getWeight_history_value()))
                .addOnFailureListener(e -> Log.e(TAG, "Ошибка отправки в облако: " + e.getMessage()));
    }

    public void syncWeightHistory() {
        if (userId == null) return;
        WeightHistoryDao dao = new WeightHistoryDao(MainActivity.getAppDataBase());
        CollectionReference weightRef = db.collection("users").document(userId).collection("weight_history");

        // 1. Отправляем локальные данные в новом формате
        List<WeightHistoryModel> localWeights = dao.getAllWeightHistory();
        for (WeightHistoryModel local : localWeights) {
            uploadWeightEntry(local);
        }

        // 2. Получаем данные из облака
        weightRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                // Теперь, когда ключи в Map совпадают с именами в классе, это сработает без ошибок
                WeightHistoryModel cloudWeight = doc.toObject(WeightHistoryModel.class);
                if (cloudWeight != null && cloudWeight.getWeight_history_uid() != null) {
                    if (!dao.isWeightUidExists(cloudWeight.getWeight_history_uid())) {
                        dao.addWeightEntry(cloudWeight);
                        Log.d(TAG, "Добавлена запись из облака: " + cloudWeight.getWeight_history_value());
                    }
                }
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Ошибка получения истории веса: " + e.getMessage()));
    }
}