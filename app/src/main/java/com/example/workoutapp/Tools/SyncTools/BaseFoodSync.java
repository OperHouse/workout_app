package com.example.workoutapp.Tools.SyncTools;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.workoutapp.Data.NutritionDao.BaseEatDao;
import com.example.workoutapp.Models.NutritionModels.FoodModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import com.google.firebase.Timestamp;
import java.util.UUID;

public class BaseFoodSync {

    private static final String TAG = "BaseFoodSync";
    private final FirebaseFirestore db;

    public interface SyncCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public BaseFoodSync() {
        db = FirebaseFirestore.getInstance();
    }

    private String getUid() {
        return FirebaseAuth.getInstance().getUid();
    }

    private CollectionReference getFoodCollection() {
        return db.collection("users")
                .document(getUid())
                .collection("base_food");
    }

    // =====================================================
    // CREATE / UPDATE
    // =====================================================

    public void uploadFood(FoodModel food, @Nullable SyncCallback callback) {
        String uid = getUid();
        if (uid == null || food == null) {
            if (callback != null) callback.onFailure("User not authorized");
            return;
        }

        if (food.getFood_uid() == null) {
            food.setFood_uid(UUID.randomUUID().toString());
        }

        DocumentReference ref = getFoodCollection().document(food.getFood_uid());

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(ref);

            long newVersion = 1;
            if (snapshot.exists()) {
                Long currentVersion = snapshot.getLong("version");
                if (currentVersion != null) {
                    newVersion = currentVersion + 1;
                }
            }

            // Создаем Timestamp из текущего времени
            Timestamp currentTimestamp = Timestamp.now();

            food.setDeleted(false);
            food.setVersion(newVersion);

            // Теперь типы совпадают: Timestamp -> Timestamp
            food.setUpdatedAt(currentTimestamp);

            // Теперь можно безопасно передавать объект целиком,
            // так как модель соответствует типам Firebase
            transaction.set(ref, food, SetOptions.merge());

            return null;
        }).addOnSuccessListener(unused -> {
            Log.d(TAG, "Food uploaded successfully: " + food.getFood_uid());
            if (callback != null) callback.onSuccess();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Upload failed: " + e.getMessage());
            if (callback != null) callback.onFailure(e.getMessage());
        });
    }

    // =====================================================
    // SOFT DELETE
    // =====================================================

    public void deleteFood(FoodModel food, @Nullable SyncCallback callback) {

        String uid = getUid();
        if (uid == null || food == null || food.getFood_uid() == null) {
            if (callback != null) callback.onFailure("Invalid data");
            return;
        }

        DocumentReference ref = getFoodCollection()
                .document(food.getFood_uid());

        db.runTransaction(transaction -> {

            DocumentSnapshot snapshot = transaction.get(ref);

            if (!snapshot.exists()) return null;

            Long currentVersion = snapshot.getLong("version");
            long newVersion = currentVersion != null ? currentVersion + 1 : 1;

            transaction.update(ref,
                    "deleted", true,
                    "version", newVersion,
                    "updatedAt", FieldValue.serverTimestamp());

            return null;

        }).addOnSuccessListener(unused -> {
            Log.d(TAG, "Food soft-deleted: " + food.getFood_uid());
            if (callback != null) callback.onSuccess();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Delete failed: " + e.getMessage());
            if (callback != null) callback.onFailure(e.getMessage());
        });
    }

    // =====================================================
    // REALTIME SYNC
    // =====================================================

    public ListenerRegistration startRealtimeSync(BaseEatDao dao) {

        String uid = getUid();
        if (uid == null) return null;

        return getFoodCollection()
                .addSnapshotListener((snap, error) -> {

                    if (error != null) {
                        Log.e(TAG, "Realtime sync error: " + error.getMessage());
                        return;
                    }

                    if (snap == null) return;

                    for (DocumentChange change : snap.getDocumentChanges()) {

                        FoodModel cloudFood = change.getDocument()
                                .toObject(FoodModel.class);

                        if (cloudFood == null) continue;

                        switch (change.getType()) {

                            case ADDED:
                            case MODIFIED:
                                if (cloudFood.isDeleted()) {
                                    dao.deleteByUid(cloudFood.getFood_uid());
                                } else {
                                    dao.insertOrUpdate(cloudFood);
                                }
                                break;

                            case REMOVED:
                                dao.deleteByUid(cloudFood.getFood_uid());
                                break;
                        }
                    }
                });
    }

    // =====================================================
    // ONE-TIME FULL SYNC (опционально)
    // =====================================================

    public void downloadAllOnce(BaseEatDao dao) {

        String uid = getUid();
        if (uid == null) return;

        getFoodCollection()
                .get()
                .addOnSuccessListener(snapshot -> {

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {

                        FoodModel cloudFood = doc.toObject(FoodModel.class);
                        if (cloudFood == null) continue;

                        if (cloudFood.isDeleted()) {
                            dao.deleteByUid(cloudFood.getFood_uid());
                        } else {
                            dao.insertOrUpdate(cloudFood);
                        }
                    }

                    Log.d(TAG, "Full sync completed");
                });
    }
}