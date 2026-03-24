package com.example.workoutapp.Tools.DeletionHandlers;

import com.example.workoutapp.Data.DeletionQueueDao;
import com.example.workoutapp.Models.DeletionTask;
import com.example.workoutapp.Tools.SyncTools.BaseExerciseSync;

public class BaseExerciseDeletionHandler extends BaseDeletionHandler {
    private final BaseExerciseSync sync;

    public BaseExerciseDeletionHandler(DeletionQueueDao queueDao, BaseExerciseSync sync) {
        super(queueDao);
        this.sync = sync;
    }

    @Override
    protected void executeDeletion(DeletionTask task, Runnable onComplete) {
        sync.deleteBaseExercise(task.uid, new BaseExerciseSync.SyncCallback() {
            @Override
            public void onSuccess() {
                // Передаем onComplete в родительский метод onSuccess
                BaseExerciseDeletionHandler.this.onSuccess(task.uid, onComplete);
            }

            @Override
            public void onFailure(String error) {
                // Передаем onComplete в родительский метод onFailure
                BaseExerciseDeletionHandler.this.onFailure(task.uid, error, onComplete);
            }
        });
    }
}