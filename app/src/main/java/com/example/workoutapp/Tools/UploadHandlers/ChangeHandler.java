package com.example.workoutapp.Tools.UploadHandlers;

public interface ChangeHandler {
    void handle(String uid,Runnable onComplete);
}
