package com.example.workoutapp.Tools.SyncTools;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.workoutapp.Data.NutritionDao.ConnectingMealPresetDao;
import com.example.workoutapp.Data.NutritionDao.PresetEatDao;
import com.example.workoutapp.Data.NutritionDao.PresetMealNameDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.NutritionModels.FoodModel;
import com.example.workoutapp.Models.NutritionModels.MealModel;
import com.example.workoutapp.Tools.UidGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;

public class MealPresetSync {

    private static final String TAG = "MealPresetSync";
    private final FirebaseFirestore db;

    public interface SyncCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public MealPresetSync() {
        db = FirebaseFirestore.getInstance();
    }

    private String getUid() {
        return FirebaseAuth.getInstance().getUid();
    }

    private CollectionReference getCollection() {
        return db.collection("users")
                .document(getUid())
                .collection("meal_presets");
    }

    // =====================================================
    // CREATE / UPDATE
    // =====================================================

    public void uploadPreset(MealModel meal, @Nullable SyncCallback callback) {

        String uid = getUid();
        if (uid == null || meal == null) {
            if (callback != null) callback.onFailure("User not authorized");
            return;
        }

        if (meal.getMeal_uid() == null) {
            meal.setMeal_uid(UidGenerator.generateMealPresetUid());
        }

        DocumentReference ref = getCollection().document(meal.getMeal_uid());

        db.runTransaction(transaction -> {

            DocumentSnapshot snapshot = transaction.get(ref);

            long newVersion = 1;
            if (snapshot.exists()) {
                Long currentVersion = snapshot.getLong("version");
                if (currentVersion != null) {
                    newVersion = currentVersion + 1;
                }
            }

            meal.setDeleted(false);
            meal.setVersion(newVersion);
            meal.setUpdatedAt(null); // будет заменено серверным временем

            transaction.set(ref, meal, SetOptions.merge());
            transaction.update(ref, "updatedAt", FieldValue.serverTimestamp());

            return null;

        }).addOnSuccessListener(unused -> {
            Log.d(TAG, "Preset uploaded: " + meal.getMeal_uid());
            if (callback != null) callback.onSuccess();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Upload failed: " + e.getMessage());
            if (callback != null) callback.onFailure(e.getMessage());
        });
    }

    // =====================================================
    // SOFT DELETE
    // =====================================================

    public void deletePreset(MealModel meal, @Nullable SyncCallback callback) {

        if (getUid() == null || meal == null || meal.getMeal_uid() == null) {

            Log.e(TAG, "Delete aborted: invalid data");

            if (callback != null)
                callback.onFailure("Invalid data");

            return;
        }

        DocumentReference ref =
                getCollection().document(meal.getMeal_uid());

        db.runTransaction(transaction -> {

            Long currentVersion = null;

            try {
                DocumentSnapshot snapshot = transaction.get(ref);
                currentVersion = snapshot.getLong("version");
            } catch (Exception ignored) {}

            long newVersion = currentVersion != null
                    ? currentVersion + 1
                    : 1;

            transaction.set(ref, new HashMap<String, Object>() {{
                put("deleted", true);
                put("version", newVersion);
                put("updatedAt", FieldValue.serverTimestamp());
            }}, SetOptions.merge());

            return null;

        }).addOnSuccessListener(unused -> {

            Log.d(TAG, "Preset soft-deleted in cloud: "
                    + meal.getMeal_uid());

            if (callback != null) callback.onSuccess();

        }).addOnFailureListener(e -> {

            Log.e(TAG, "Delete failed: " + e.getMessage());

            if (callback != null) callback.onFailure(e.getMessage());
        });
    }

    // =====================================================
    // REALTIME SYNC (Cloud → Local)
    // =====================================================

    public ListenerRegistration startRealtimeSync(
            PresetMealNameDao nameDao,
            PresetEatDao eatDao,
            ConnectingMealPresetDao connectDao) {

        if (getUid() == null) return null;

        return getCollection().addSnapshotListener((snap, error) -> {

            if (error != null || snap == null) {
                Log.e(TAG, "Realtime error: " + (error != null ? error.getMessage() : "null"));
                return;
            }

            for (DocumentChange change : snap.getDocumentChanges()) {

                MealModel cloudMeal =
                        change.getDocument().toObject(MealModel.class);

                if (cloudMeal == null) continue;

                if (cloudMeal.isDeleted()) {

                    nameDao.deleteByUid(cloudMeal.getMeal_uid());
                    connectDao.deleteConnectionsByMealUid(cloudMeal.getMeal_uid());

                    continue;
                }

                // 1️⃣ Сохраняем / обновляем имя
                long mealId = nameDao.insertOrUpdate(cloudMeal);

                // 2️⃣ Чистим старые связи
                connectDao.deleteConnectionsByMealUid(cloudMeal.getMeal_uid());

                // 3️⃣ Пересобираем список продуктов
                List<FoodModel> foods = cloudMeal.getMeal_food_list();
                if (foods != null) {

                    for (FoodModel food : foods) {

                        long foodId = eatDao.insertOrUpdate(food);

                        connectDao.addMealPresetConnection(mealId, foodId);
                    }
                }
            }
        });
    }

    // =====================================================
    // FULL DOWNLOAD (one-time)
    // =====================================================

    public void downloadAllOnce() {

        if (getUid() == null) return;

        // 🔥 Инициализируем DAO внутри
        PresetMealNameDao nameDao =
                new PresetMealNameDao(MainActivity.getAppDataBase());

        PresetEatDao eatDao =
                new PresetEatDao(MainActivity.getAppDataBase());

        ConnectingMealPresetDao connectDao =
                new ConnectingMealPresetDao(MainActivity.getAppDataBase());

        getCollection()
                .get()
                .addOnSuccessListener(snapshot -> {

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {

                        MealModel meal = doc.toObject(MealModel.class);
                        if (meal == null) continue;

                        if (meal.isDeleted()) {

                            nameDao.deleteByUid(meal.getMeal_uid());
                            connectDao.deleteConnectionsByMealUid(meal.getMeal_uid());

                            continue;
                        }

                        // 1️⃣ Сохраняем / обновляем имя
                        long mealId = nameDao.insertOrUpdate(meal);

                        // 2️⃣ Удаляем старые связи
                        connectDao.deleteConnectionsByMealUid(meal.getMeal_uid());

                        // 3️⃣ Пересобираем список продуктов
                        if (meal.getMeal_food_list() != null) {

                            for (FoodModel food : meal.getMeal_food_list()) {

                                long foodId = eatDao.insertOrUpdate(food);

                                connectDao.addMealPresetConnection(mealId, foodId);
                            }
                        }
                    }

                    Log.d(TAG, "Full preset sync completed");
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Full preset sync failed: " + e.getMessage())
                );
    }
}