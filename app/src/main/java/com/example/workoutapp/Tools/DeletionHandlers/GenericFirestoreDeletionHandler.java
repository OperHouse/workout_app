package com.example.workoutapp.Tools.DeletionHandlers;

import com.example.workoutapp.Data.DeletionQueueDao;
import com.example.workoutapp.Models.DeletionTask;

public class GenericFirestoreDeletionHandler extends BaseDeletionHandler {
    private final com.google.firebase.firestore.FirebaseFirestore db;
    private final String userId;
    private final String collectionPath;

    public GenericFirestoreDeletionHandler(DeletionQueueDao queueDao, String userId, String collectionPath) {
        super(queueDao);
        this.db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        this.userId = userId;
        this.collectionPath = collectionPath;
    }

    @Override
    protected void executeDeletion(DeletionTask task, Runnable onComplete) {
        if (userId == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        db.collection("users").document(userId)
                .collection(collectionPath).document(task.uid)
                .delete()
                .addOnSuccessListener(aVoid -> onSuccess(task.uid, onComplete))
                .addOnFailureListener(e -> onFailure(task.uid, e.getMessage(), onComplete));
    }
}
