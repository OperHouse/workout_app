package com.example.workoutapp.Tools.UploadHandlers;

import com.example.workoutapp.Data.ChangeElmDao;
import com.example.workoutapp.Data.NutritionDao.ConnectingMealPresetDao;
import com.example.workoutapp.Data.NutritionDao.PresetEatDao;
import com.example.workoutapp.Data.NutritionDao.PresetMealNameDao;
import com.example.workoutapp.Models.NutritionModels.MealModel;
import com.example.workoutapp.Tools.SyncTools.MealPresetSync;

public class MealPresetChangeHandler extends BaseSyncHandler<MealModel> {

    private final PresetMealNameDao presetDao;
    private final ConnectingMealPresetDao connDao;
    private final PresetEatDao eatDao;
    private final MealPresetSync sync;

    public MealPresetChangeHandler(
            PresetMealNameDao presetDao,
            ConnectingMealPresetDao connDao,
            PresetEatDao eatDao,
            MealPresetSync sync,
            ChangeElmDao changeDao
    ) {
        super(changeDao);
        this.presetDao = presetDao;
        this.connDao = connDao;
        this.eatDao = eatDao;
        this.sync = sync;
    }

    @Override
    protected MealModel getModel(String uid) {
        return presetDao.getPresetMealByUid(uid, connDao, eatDao);
    }

    // Внедряем Runnable onComplete для поддержки 15-секундного таймаута
    @Override
    protected void executeSync(MealModel model, String uid, Runnable onComplete) {
        sync.uploadPreset(model, new MealPresetSync.SyncCallback() {
            @Override
            public void onSuccess() {
                // Вызываем родительский метод с сигналом завершения
                MealPresetChangeHandler.this.onSuccess(uid, onComplete);
            }

            @Override
            public void onFailure(String error) {
                // Пропускаем очередь дальше даже при ошибке выгрузки пресета
                MealPresetChangeHandler.this.onFailure(uid, error, onComplete);
            }
        });
    }
}