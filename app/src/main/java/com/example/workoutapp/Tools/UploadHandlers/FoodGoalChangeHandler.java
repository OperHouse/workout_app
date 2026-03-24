package com.example.workoutapp.Tools.UploadHandlers;

import com.example.workoutapp.Data.ChangeElmDao;
import com.example.workoutapp.Data.ProfileDao.FoodGainGoalDao;
import com.example.workoutapp.Models.ProfileModels.FoodGainGoalModel;
import com.example.workoutapp.Tools.SyncTools.FoodGoalSync;

public class FoodGoalChangeHandler extends BaseSyncHandler<FoodGainGoalModel> {

    private final FoodGainGoalDao dao;
    private final FoodGoalSync sync;

    public FoodGoalChangeHandler(
            FoodGainGoalDao dao,
            FoodGoalSync sync,
            ChangeElmDao changeDao
    ) {
        super(changeDao);
        this.dao = dao;
        this.sync = sync;
    }

    @Override
    protected FoodGainGoalModel getModel(String uid) {
        return dao.getGoalByUid(uid);
    }

    // Внедряем Runnable для поддержки цепочки в 15 секунд
    @Override
    protected void executeSync(FoodGainGoalModel model, String uid, Runnable onComplete) {
        sync.uploadGoal(model, new FoodGoalSync.SyncCallback() {
            @Override
            public void onSuccess() {
                // Вызываем родительский метод с передачей сигнала onComplete
                FoodGoalChangeHandler.this.onSuccess(uid, onComplete);
            }

            @Override
            public void onFailure(String error) {
                // Пробрасываем ошибку, но даем очереди идти дальше
                FoodGoalChangeHandler.this.onFailure(uid, error, onComplete);
            }
        });
    }

}