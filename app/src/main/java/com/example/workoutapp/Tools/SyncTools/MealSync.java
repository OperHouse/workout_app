package com.example.workoutapp.Tools.SyncTools;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.workoutapp.Data.NutritionDao.ConnectingMealDao;
import com.example.workoutapp.Data.NutritionDao.MealFoodDao;
import com.example.workoutapp.Data.NutritionDao.MealNameDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.NutritionModels.FoodModel;
import com.example.workoutapp.Models.NutritionModels.MealModel;
import com.example.workoutapp.Tools.UidGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public interface DownloadCallback {
        void onDownloaded(List<MealModel> meals);

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

    public void loadAllMeals() {
        // Инициализация DAO
        MealNameDao nameDao = new MealNameDao(MainActivity.getAppDataBase());
        MealFoodDao foodDao = new MealFoodDao(MainActivity.getAppDataBase());
        ConnectingMealDao connectionDao = new ConnectingMealDao(MainActivity.getAppDataBase());

        if (getUid() == null) return;

        getMealsCollection()
                .whereEqualTo("deleted", false)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots == null) return;

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        MealModel meal = doc.toObject(MealModel.class);

                        if (meal != null) {
                            // 1. Сохраняем прием пищи (Meal Name)
                            // ВАЖНО: Добавь в MealNameDao проверку на существующий UID,
                            // чтобы не создавать дубли при каждой загрузке.
                            long localMealId = nameDao.insertMealName(
                                    meal.getMeal_name(),
                                    meal.getMealData(),
                                    meal.getMeal_uid()
                            );

                            if (localMealId == -1) {
                                Log.e(TAG, "Ошибка: Прием пищи не сохранен (возможно, уже есть)");
                                // Если прием пищи уже есть, нужно получить его существующий ID,
                                // иначе связи не запишутся.
                                continue;
                            }

                            // 2. Сохраняем продукты
                            if (meal.getMeal_food_list() != null) {
                                for (FoodModel food : meal.getMeal_food_list()) {

                                    // Добавляем еду и получаем её ID (здесь сработает твоя проверка на UID)
                                    long fId = foodDao.addSingleFood(food);

                                    // 3. Записываем связь, только если fId валидный
                                    if (fId != -1) {
                                        connectionDao.connectingSingleFood(localMealId, fId);
                                        Log.d(TAG, "Связь создана: Meal " + localMealId + " -> Food " + fId);
                                    } else {
                                        Log.e(TAG, "Не удалось сохранить еду: " + food.getFood_name());
                                    }
                                }
                            }
                        }
                    }
                    Log.d(TAG, "Синхронизация всех приемов пищи завершена");
                })
                .addOnFailureListener(e -> Log.e(TAG, "Ошибка loadAllMeals: " + e.getMessage()));
    }


    /**
     * Загрузка всех приемов пищи из облака с сохранением в локальную БД.
     * Используется при логине пользователя.
     */
    public void downloadAllMeals(@Nullable DownloadCallback callback) {
        if (getUid() == null) {
            if (callback != null) callback.onError("Пользователь не авторизован");
            return;
        }

        // Инициализация DAO
        MealNameDao nameDao = new MealNameDao(MainActivity.getAppDataBase());
        MealFoodDao foodDao = new MealFoodDao(MainActivity.getAppDataBase());
        ConnectingMealDao connectionDao = new ConnectingMealDao(MainActivity.getAppDataBase());

        getMealsCollection()
                .whereEqualTo("deleted", false) // Загружаем только не удаленные
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<MealModel> meals = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            MealModel meal = doc.toObject(MealModel.class);
                            if (meal != null) {
                                meals.add(meal);

                                // Логика сохранения (ваша существующая логика)
                                // 1. Сохраняем заголовок приема пищи
                                long localMealId = nameDao.insertMealName(
                                        meal.getMeal_name(),
                                        meal.getMealData(),
                                        meal.getMeal_uid()
                                );

                                // Если -1, значит либо ошибка, либо (чаще) такой UID уже есть в базе
                                if (localMealId != -1) {
                                    // 2. Сохраняем продукты и связи
                                    if (meal.getMeal_food_list() != null) {
                                        for (FoodModel food : meal.getMeal_food_list()) {
                                            long fId = foodDao.addSingleFood(food);
                                            if (fId != -1) {
                                                connectionDao.connectingSingleFood(localMealId, fId);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Log.d(TAG, "Загрузка приемов пищи завершена: " + meals.size());
                    if (callback != null) callback.onDownloaded(meals);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка загрузки приемов пищи: " + e.getMessage());
                    if (callback != null) callback.onError(e.getMessage());
                });
    }
}