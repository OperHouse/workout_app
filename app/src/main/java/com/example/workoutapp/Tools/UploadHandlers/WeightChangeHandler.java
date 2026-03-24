package com.example.workoutapp.Tools.UploadHandlers;

import com.example.workoutapp.Data.ChangeElmDao;
import com.example.workoutapp.Data.ProfileDao.WeightHistoryDao;
import com.example.workoutapp.Models.ProfileModels.WeightHistoryModel;
import com.example.workoutapp.Tools.SyncTools.WeightSync;

public class WeightChangeHandler extends BaseSyncHandler<WeightHistoryModel> {

    private final WeightHistoryDao dao;
    private final WeightSync sync;

    public WeightChangeHandler(
            WeightHistoryDao dao,
            WeightSync sync,
            ChangeElmDao changeDao
    ) {
        super(changeDao);
        this.dao = dao;
        this.sync = sync;
    }

    @Override
    protected WeightHistoryModel getModel(String uid) {
        return dao.getWeightByUid(uid);
    }

    // Внедряем Runnable onComplete для поддержки рекурсивной очереди
    @Override
    protected void executeSync(WeightHistoryModel model, String uid, Runnable onComplete) {
        sync.uploadWeightEntry(model, new WeightSync.SyncCallback() {
            @Override
            public void onSuccess() {
                // Удаляем задачу из БД и сигнализируем менеджеру о готовности к следующей
                WeightChangeHandler.this.onSuccess(uid, onComplete);
            }

            @Override
            public void onFailure(String error) {
                // В случае ошибки выводим лог, но не блокируем очередь синхронизации
                WeightChangeHandler.this.onFailure(uid, error, onComplete);
            }
        });
    }

}