package com.example.workoutapp.Tools;

import android.util.Log;
import com.example.workoutapp.Data.ProfileDao.GeneralGoalDao;
import com.example.workoutapp.Models.ProfileModels.GeneralGoalModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.List;

public class GeneralGoalSync {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String userId = FirebaseAuth.getInstance().getUid();
    private final String TAG = "GeneralGoalSync";

    public void uploadGoal(GeneralGoalModel goal) {
        if (userId == null || goal.getGeneral_goal_uid() == null) return;

        db.collection("users").document(userId)
                .collection("general_goals").document(goal.getGeneral_goal_uid())
                .set(goal, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Общая цель синхронизирована"))
                .addOnFailureListener(e -> Log.e(TAG, "Ошибка: " + e.getMessage()));
    }

    public void pullGoalsFromCloud(GeneralGoalDao goalDao) {
        if (userId == null) return;

        db.collection("users").document(userId).collection("general_goals")
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (DocumentSnapshot doc : snapshots) {
                        GeneralGoalModel cloudGoal = doc.toObject(GeneralGoalModel.class);
                        if (cloudGoal != null && !goalDao.isGoalUidExists(cloudGoal.getGeneral_goal_uid())) {
                            goalDao.insertGoal(cloudGoal);
                        }
                    }
                    Log.d(TAG, "Общие цели загружены из облака");
                });
    }

    public void pushLocalGoalsToCloud(GeneralGoalDao goalDao) {
        if (userId == null) return;
        List<GeneralGoalModel> localGoals = goalDao.getAllGoals();
        for (GeneralGoalModel goal : localGoals) {
            if (goal.getGeneral_goal_uid() == null || goal.getGeneral_goal_uid().isEmpty()) {
                String newUid = "GG_" + java.util.UUID.randomUUID().toString();
                goal.setGeneral_goal_uid(newUid);
                goalDao.updateGoalUid(goal.getGeneral_goal_id(), newUid);
            }
            uploadGoal(goal);
        }
    }
}