package com.example.workoutapp.Tools.SyncTools;

import android.util.Log;
import com.example.workoutapp.Data.ProfileDao.FoodGainGoalDao;
import com.example.workoutapp.Models.ProfileModels.FoodGainGoalModel;
import com.example.workoutapp.Tools.UidGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.List;

public class FoodGoalSync {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String userId = FirebaseAuth.getInstance().getUid();
    private final String TAG = "FoodGoalSync";

    public void uploadGoal(FoodGainGoalModel goal) {
        if (userId == null || goal.getFood_gain_goal_uid() == null) return;

        db.collection("users").document(userId)
                .collection("food_gain_goals").document(goal.getFood_gain_goal_uid())
                .set(goal, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Цель питания синхронизирована"))
                .addOnFailureListener(e -> Log.e(TAG, "Ошибка синхронизации: " + e.getMessage()));
    }

    public void pullGoalsFromCloud(FoodGainGoalDao goalDao) {
        if (userId == null) return;

        db.collection("users").document(userId).collection("food_gain_goals")
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (DocumentSnapshot doc : snapshots) {
                        FoodGainGoalModel cloudGoal = doc.toObject(FoodGainGoalModel.class);
                        if (cloudGoal != null && !goalDao.isGoalUidExists(cloudGoal.getFood_gain_goal_uid())) {
                            goalDao.insertGoal(cloudGoal);
                        }
                    }
                    Log.d(TAG, "Цели питания загружены из облака");
                });
    }

    public void pushLocalGoalsToCloud(FoodGainGoalDao goalDao) {
        if (userId == null) return;
        List<FoodGainGoalModel> localGoals = goalDao.getAllGoals();
        for (FoodGainGoalModel goal : localGoals) {
            if (goal.getFood_gain_goal_uid() == null || goal.getFood_gain_goal_uid().isEmpty()) {
                String newUid = UidGenerator.generateFoodGoalUid();
                goal.setFood_gain_goal_uid(newUid);
                goalDao.updateGoalUid(goal.getFood_gain_goal_id(), newUid);
            }
            uploadGoal(goal);
        }
    }
}