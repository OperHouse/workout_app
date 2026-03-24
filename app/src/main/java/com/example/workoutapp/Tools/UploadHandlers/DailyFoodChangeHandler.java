package com.example.workoutapp.Tools.UploadHandlers;

import com.example.workoutapp.Data.ChangeElmDao;
import com.example.workoutapp.Data.ProfileDao.DailyFoodTrackingDao;
import com.example.workoutapp.Models.ProfileModels.DailyFoodTrackingModel;
import com.example.workoutapp.Tools.SyncTools.DailyFoodSync;

public class DailyFoodChangeHandler extends BaseSyncHandler<DailyFoodTrackingModel> {

    private final DailyFoodTrackingDao dao;
    private final DailyFoodSync sync;

    public DailyFoodChangeHandler(
            DailyFoodTrackingDao dao,
            DailyFoodSync sync,
            ChangeElmDao changeDao
    ) {
        super(changeDao);
        this.dao = dao;
        this.sync = sync;
    }

    @Override
    protected DailyFoodTrackingModel getModel(String uid) {
        // Получаем модель питания по дате (которая в данном случае выступает как UID)
        return dao.getEntryByDate(uid);
    }

    // Основной рабочий метод для последовательной очереди
    @Override
    protected void executeSync(DailyFoodTrackingModel model, String uid, Runnable onComplete) {
        sync.uploadEntry(model, new DailyFoodSync.SyncCallback() {
            @Override
            public void onSuccess() {
                // Вызываем родительский метод, который удалит задачу из БД
                // и запустит следующую задачу через onComplete
                DailyFoodChangeHandler.this.onSuccess(uid, onComplete);
            }

            @Override
            public void onFailure(String error) {
                // Даже при ошибке пробрасываем onComplete, чтобы очередь не "зависла"
                DailyFoodChangeHandler.this.onFailure(uid, error, onComplete);
            }
        });
    }


}