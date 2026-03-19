package com.example.workoutapp.Tools.SyncTools;

import android.util.Log;

import com.example.workoutapp.Data.ProfileDao.FoodGainGoalDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.ProfileModels.FoodGainGoalModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;

public class FoodGoalSync {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String userId = FirebaseAuth.getInstance().getUid();
    private final String TAG = "FoodGoalSync";

    // Интерфейс для результата отправки
    public interface SyncCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // Интерфейс для результата загрузки
    public interface DownloadCallback {
        void onDownloaded(List<FoodGainGoalModel> goals);
        void onError(String error);
    }

    /**
     * ОТПРАВКА: Синхронизация одной цели питания с поддержкой Callback.
     */
    public void uploadGoal(FoodGainGoalModel goal, SyncCallback callback) {
        if (userId == null || goal == null || goal.getFood_gain_goal_uid() == null) {
            if (callback != null) callback.onFailure("Данные отсутствуют или пользователь не авторизован");
            return;
        }

        db.collection("users").document(userId)
                .collection("food_gain_goals").document(goal.getFood_gain_goal_uid())
                .set(goal, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Цель питания синхронизирована: " + goal.getFood_gain_goal_uid());
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка синхронизации: " + e.getMessage());
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    /**
     * ПОЛУЧЕНИЕ: Выгрузка всех целей питания из облака.
     */
    public void downloadGoals(DownloadCallback callback) {
        if (userId == null) {
            if (callback != null) callback.onError("Пользователь не авторизован");
            return;
        }

        db.collection("users").document(userId).collection("food_gain_goals")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<FoodGainGoalModel> cloudGoals = new ArrayList<>();
                    FoodGainGoalDao goalDao = new FoodGainGoalDao(MainActivity.getAppDataBase());

                    for (DocumentSnapshot doc : snapshots) {
                        FoodGainGoalModel cloudGoal = doc.toObject(FoodGainGoalModel.class);
                        if (cloudGoal != null && cloudGoal.getFood_gain_goal_uid() != null) {
                            if (!goalDao.isGoalUidExists(cloudGoal.getFood_gain_goal_uid())) {
                                goalDao.insertGoal(cloudGoal);
                            }
                            cloudGoals.add(cloudGoal);
                        }
                    }
                    if (callback != null) callback.onDownloaded(cloudGoals);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }


}