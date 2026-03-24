package com.example.workoutapp.Tools.DeletionHandlers;

import com.example.workoutapp.Models.DeletionTask;

public interface DeletionHandler {
    void handle(DeletionTask task, Runnable onComplete);
}
