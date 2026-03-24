package com.example.workoutapp.Tools.UploadHandlers;

import com.example.workoutapp.Data.ChangeElmDao;
import com.example.workoutapp.Data.ProfileDao.ActivityGoalDao;
import com.example.workoutapp.Models.ProfileModels.ActivityGoalModel;
import com.example.workoutapp.Tools.SyncTools.ActivityGoalSync;

public class ActivityGoalChangeHandler extends BaseSyncHandler<ActivityGoalModel> {

    private final ActivityGoalDao dao;
    private final ActivityGoalSync sync;

    public ActivityGoalChangeHandler(
            ActivityGoalDao dao,
            ActivityGoalSync sync,
            ChangeElmDao changeDao
    ) {
        super(changeDao);
        this.dao = dao;
        this.sync = sync;
    }

    @Override
    protected ActivityGoalModel getModel(String uid) {
        return dao.getGoalByUid(uid);
    }

    // Переименовываем метод в executeSync (как в обновленном BaseSyncHandler)
    // и добавляем Runnable onComplete
    @Override
    protected void executeSync(ActivityGoalModel model, String uid, Runnable onComplete) {
        sync.uploadGoal(model, new ActivityGoalSync.SyncCallback() {
            @Override
            public void onSuccess() {
                // Передаем onComplete в родительский метод onSuccess
                ActivityGoalChangeHandler.this.onSuccess(uid, onComplete);
            }

            @Override
            public void onFailure(String error) {
                // Передаем onComplete в родительский метод onFailure
                ActivityGoalChangeHandler.this.onFailure(uid, error, onComplete);
            }
        });
    }


}