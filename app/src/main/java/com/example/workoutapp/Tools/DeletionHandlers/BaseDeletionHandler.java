package com.example.workoutapp.Tools.DeletionHandlers;

import android.util.Log;

import com.example.workoutapp.Data.DeletionQueueDao;
import com.example.workoutapp.Models.DeletionTask;

public abstract class BaseDeletionHandler implements DeletionHandler {
    protected final DeletionQueueDao queueDao;

    public BaseDeletionHandler(DeletionQueueDao queueDao) {
        this.queueDao = queueDao;
    }

    @Override
    public void handle(DeletionTask task, Runnable onComplete) {
        if (task == null || task.uid == null) {
            if (onComplete != null) onComplete.run();
            return;
        }
        executeDeletion(task, onComplete);
    }

    protected abstract void executeDeletion(DeletionTask task, Runnable onComplete);

    public void onSuccess(String uid, Runnable onComplete) {
        queueDao.removeFromQueue(uid);
        if (onComplete != null) onComplete.run();
    }

    public void onFailure(String uid, String error, Runnable onComplete) {
        Log.e("DELETION", "Failed: " + error);
        if (onComplete != null) onComplete.run();
    }
}
