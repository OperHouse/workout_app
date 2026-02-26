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

import java.util.List;

public class WeightSync {
    private final FirebaseFirestore db;
    private final String userId;
    private static final String TAG = "WeightSync";

    public WeightSync() {
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getUid();
    }

    // Отправка ОДНОЙ записи (для saveProfile)
    public void uploadWeightEntry(WeightHistoryModel weightEntry) {
        if (userId == null || weightEntry == null) return;

        if (weightEntry.getWeightUid() == null || weightEntry.getWeightUid().isEmpty()) {
            Log.e(TAG, "Ошибка: Попытка отправить запись без UID!");
            return;
        }

        db.collection("users").document(userId)
                .collection("weight_history")
                .document(weightEntry.getWeightUid())
                .set(weightEntry, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Вес успешно ушел в облако: " + weightEntry.getWeightValue()))
                .addOnFailureListener(e -> Log.e(TAG, "Ошибка отправки в облако: " + e.getMessage()));
    }

    // Полная синхронизация (для старта приложения)
    public void syncWeightHistory() {
        if (userId == null) return;
        WeightHistoryDao dao = new WeightHistoryDao(MainActivity.getAppDataBase());
        CollectionReference weightRef = db.collection("users").document(userId).collection("weight_history");

        // 1. Пушим локальные данные
        List<WeightHistoryModel> localWeights = dao.getAllWeightHistory();
        for (WeightHistoryModel local : localWeights) {
            uploadWeightEntry(local);
        }

        // 2. Тянем из облака
        weightRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                WeightHistoryModel cloudWeight = doc.toObject(WeightHistoryModel.class);
                if (cloudWeight != null && !dao.isWeightUidExists(cloudWeight.getWeightUid())) {
                    dao.addWeightEntry(cloudWeight);
                    Log.d(TAG, "Добавлена запись из облака: " + cloudWeight.getWeightValue());
                }
            }
        });
    }
}