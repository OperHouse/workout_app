package com.example.workoutapp.Tools;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.workoutapp.Models.NutritionModels.MealModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class MealSync {

    private static final String TAG = "MealSync";

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    // =====================================================
    // CALLBACKS
    // =====================================================

    public interface SyncCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface MealsListener {
        void onMealsChanged(List<MealModel> meals);
        void onError(String error);
    }

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public MealSync() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private String getUid() {
        return auth.getCurrentUser() != null
                ? auth.getCurrentUser().getUid()
                : null;
    }

    private CollectionReference getMealsCollection() {
        return db.collection("users")
                .document(getUid())
                .collection("meal_diary");
    }

    // =====================================================
    // CREATE / UPDATE
    // =====================================================

    public void uploadMeal(MealModel meal,
                           @Nullable SyncCallback callback) {

        if (getUid() == null ||
                meal == null ||
                meal.getMealData() == null)
            return;

        if (meal.getMeal_uid() == null) {
            meal.setMeal_uid(UidGenerator.generateMealUid());
        }

        DocumentReference ref =
                getMealsCollection()
                        .document(meal.getMeal_uid());

        db.runTransaction(transaction -> {

            DocumentSnapshot snapshot = transaction.get(ref);

            Long remoteVersion =
                    snapshot.exists()
                            ? snapshot.getLong("version")
                            : null;

            long newVersion =
                    remoteVersion != null
                            ? remoteVersion + 1
                            : 1;

            meal.setVersion(newVersion);

            Map<String, Object> data =
                    new HashMap<>();

            data.put("meal_uid", meal.getMeal_uid());
            data.put("meal_name", meal.getMeal_name());
            data.put("mealData", meal.getMealData());
            data.put("meal_food_list",
                    meal.getMeal_food_list());
            data.put("deleted", false);
            data.put("version", newVersion);
            data.put("updatedAt",
                    FieldValue.serverTimestamp());

            transaction.set(ref,
                    data,
                    SetOptions.merge());

            return null;

        }).addOnSuccessListener(unused -> {

            Log.d(TAG,
                    "Meal uploaded: "
                            + meal.getMeal_uid());

            if (callback != null)
                callback.onSuccess();

        }).addOnFailureListener(e -> {

            Log.e(TAG,
                    "Upload failed: "
                            + e.getMessage());

            if (callback != null)
                callback.onFailure(e.getMessage());
        });
    }

    // =====================================================
    // SOFT DELETE
    // =====================================================

    public void deleteMeal(MealModel meal,
                           @Nullable SyncCallback callback) {

        if (getUid() == null ||
                meal == null ||
                meal.getMeal_uid() == null)
            return;

        DocumentReference ref =
                getMealsCollection()
                        .document(meal.getMeal_uid());

        db.runTransaction(transaction -> {

            DocumentSnapshot snapshot =
                    transaction.get(ref);

            Long remoteVersion =
                    snapshot.exists()
                            ? snapshot.getLong("version")
                            : null;

            long newVersion =
                    remoteVersion != null
                            ? remoteVersion + 1
                            : 1;

            Map<String, Object> updates =
                    new HashMap<>();

            updates.put("deleted", true);
            updates.put("version", newVersion);
            updates.put("updatedAt",
                    FieldValue.serverTimestamp());

            transaction.set(ref,
                    updates,
                    SetOptions.merge());

            return null;

        }).addOnSuccessListener(unused -> {

            Log.d(TAG,
                    "Meal soft deleted: "
                            + meal.getMeal_uid());

            if (callback != null)
                callback.onSuccess();

        }).addOnFailureListener(e -> {

            Log.e(TAG,
                    "Delete failed: "
                            + e.getMessage());

            if (callback != null)
                callback.onFailure(e.getMessage());
        });
    }

    // =====================================================
    // REALTIME LISTENER BY DATE
    // =====================================================

    public ListenerRegistration listenMealsByDate(
            String date,
            MealsListener listener) {

        return getMealsCollection()
                .whereEqualTo("mealData", date)
                .addSnapshotListener((snapshots, e) -> {

                    if (e != null) {
                        listener.onError(e.getMessage());
                        return;
                    }

                    List<MealModel> list =
                            new ArrayList<>();

                    if (snapshots != null) {

                        for (DocumentSnapshot doc
                                : snapshots.getDocuments()) {

                            MealModel meal =
                                    doc.toObject(
                                            MealModel.class);

                            if (meal != null) {
                                list.add(meal);
                            }
                        }
                    }

                    listener.onMealsChanged(list);
                });
    }

    // =====================================================
    // FIRST LOAD (ALL DATA)
    // =====================================================

    public void loadAllMeals(MealsListener listener) {

        if (getUid() == null) return;

        getMealsCollection()
                .get()
                .addOnSuccessListener(snapshots -> {

                    List<MealModel> list =
                            new ArrayList<>();

                    for (DocumentSnapshot doc
                            : snapshots.getDocuments()) {

                        MealModel meal =
                                doc.toObject(
                                        MealModel.class);

                        if (meal != null) {
                            list.add(meal);
                        }
                    }

                    listener.onMealsChanged(list);

                }).addOnFailureListener(e ->
                        listener.onError(e.getMessage()));
    }
}