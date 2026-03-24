package com.example.workoutapp.Tools.UploadHandlers;

import android.util.Log;

import com.example.workoutapp.Data.ChangeElmDao;

public abstract class BaseSyncHandler<T> implements ChangeHandler {
    protected final ChangeElmDao changeDao;

    public BaseSyncHandler(ChangeElmDao changeDao) {
        this.changeDao = changeDao;
    }

    @Override
    public void handle(String uid, Runnable onComplete) {
        T model = getModel(uid);
        if (model == null) {
            changeDao.removeFromQueue(uid);
            if (onComplete != null) onComplete.run();
            return;
        }
        executeSync(model, uid, onComplete);
    }

    protected abstract T getModel(String uid);
    protected abstract void executeSync(T model, String uid, Runnable onComplete);

    protected void onSuccess(String uid, Runnable onComplete) {
        changeDao.removeFromQueue(uid);
        if (onComplete != null) onComplete.run();
    }

    protected void onFailure(String uid, String error, Runnable onComplete) {
        Log.e("SYNC", "Failed for " + uid + ": " + error);
        if (onComplete != null) onComplete.run();
    }
}