package com.example.workoutapp.Tools.UploadHandlers;

import com.example.workoutapp.Data.ChangeElmDao;
import com.example.workoutapp.Data.ProfileDao.DailyActivityTrackingDao;
import com.example.workoutapp.Models.ProfileModels.DailyActivityTrackingModel;
import com.example.workoutapp.Tools.SyncTools.DailyActivitySync;

public class DailyActivityChangeHandler extends BaseSyncHandler<DailyActivityTrackingModel> {

    private final DailyActivityTrackingDao dao;
    private final DailyActivitySync sync;

    public DailyActivityChangeHandler(
            DailyActivityTrackingDao dao,
            DailyActivitySync sync,
            ChangeElmDao changeDao
    ) {
        super(changeDao);
        this.dao = dao;
        this.sync = sync;
    }

    @Override
    protected DailyActivityTrackingModel getModel(String uid) {
        // Предполагается, что в DailyActivity uid часто совпадает с датой
        return dao.getEntryByDate(uid);
    }

    // Основной метод для последовательной работы очереди
    @Override
    protected void executeSync(DailyActivityTrackingModel model, String uid, Runnable onComplete) {
        sync.uploadEntry(model, new DailyActivitySync.SyncCallback() {
            @Override
            public void onSuccess() {
                // Передаем onComplete, чтобы менеджер запустил следующую задачу
                DailyActivityChangeHandler.this.onSuccess(uid, onComplete);
            }

            @Override
            public void onFailure(String error) {
                // В случае ошибки тоже вызываем onComplete (чтобы очередь не встала намертво)
                DailyActivityChangeHandler.this.onFailure(uid, error, onComplete);
            }
        });
    }


}