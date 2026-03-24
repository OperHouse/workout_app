package com.example.workoutapp.Tools.UploadHandlers;

import com.example.workoutapp.Data.ChangeElmDao;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_PRESET_NAME_TABLE_DAO;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.Tools.SyncTools.PresetWorkoutSync;

public class WorkoutPresetChangeHandler extends BaseSyncHandler<ExerciseModel> {

    private final WORKOUT_PRESET_NAME_TABLE_DAO dao;
    private final PresetWorkoutSync sync;

    public WorkoutPresetChangeHandler(
            WORKOUT_PRESET_NAME_TABLE_DAO dao,
            PresetWorkoutSync sync,
            ChangeElmDao changeDao
    ) {
        super(changeDao);
        this.dao = dao;
        this.sync = sync;
    }

    @Override
    protected ExerciseModel getModel(String uid) {
        return dao.getPresetByUid(uid);
    }

    // Внедряем метод executeSync для поддержки цепочки с Runnable
    @Override
    protected void executeSync(ExerciseModel model, String uid, Runnable onComplete) {
        sync.uploadPreset(
                model.getExerciseName(),
                uid,
                model.getSets(),
                new PresetWorkoutSync.SyncCallback() {
                    @Override
                    public void onSuccess() {
                        // Сообщаем родителю об успехе и передаем сигнал onComplete
                        WorkoutPresetChangeHandler.this.onSuccess(uid, onComplete);
                    }

                    @Override
                    public void onFailure(String error) {
                        // Пробрасываем сигнал дальше даже при ошибке выгрузки пресета
                        WorkoutPresetChangeHandler.this.onFailure(uid, error, onComplete);
                    }
                }
        );
    }
}