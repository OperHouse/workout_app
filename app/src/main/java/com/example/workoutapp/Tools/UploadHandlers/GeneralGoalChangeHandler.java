package com.example.workoutapp.Tools.UploadHandlers;

import com.example.workoutapp.Data.ChangeElmDao;
import com.example.workoutapp.Data.ProfileDao.GeneralGoalDao;
import com.example.workoutapp.Models.ProfileModels.GeneralGoalModel;
import com.example.workoutapp.Tools.SyncTools.GeneralGoalSync;

public class GeneralGoalChangeHandler extends BaseSyncHandler<GeneralGoalModel> {

    private final GeneralGoalDao dao;
    private final GeneralGoalSync sync;

    public GeneralGoalChangeHandler(
            GeneralGoalDao dao,
            GeneralGoalSync sync,
            ChangeElmDao changeDao
    ) {
        super(changeDao);
        this.dao = dao;
        this.sync = sync;
    }

    @Override
    protected GeneralGoalModel getModel(String uid) {
        return dao.getGoalByUid(uid);
    }

    // Внедряем Runnable для обеспечения работы рекурсивной очереди
    @Override
    protected void executeSync(GeneralGoalModel model, String uid, Runnable onComplete) {
        sync.uploadGoal(model, new GeneralGoalSync.SyncCallback() {
            @Override
            public void onSuccess() {
                // Вызываем родительский метод с передачей сигнала завершения задачи
                GeneralGoalChangeHandler.this.onSuccess(uid, onComplete);
            }

            @Override
            public void onFailure(String error) {
                // Пробрасываем сигнал дальше даже при ошибке, чтобы синхронизация не зависла
                GeneralGoalChangeHandler.this.onFailure(uid, error, onComplete);
            }
        });
    }
}