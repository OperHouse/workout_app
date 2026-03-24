package com.example.workoutapp.Tools.UploadHandlers;

import com.example.workoutapp.Data.ChangeElmDao;
import com.example.workoutapp.Data.WorkoutDao.BASE_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;
import com.example.workoutapp.Tools.SyncTools.BaseExerciseSync;

public class BaseExerciseChangeHandler extends BaseSyncHandler<BaseExModel> {

    private final BASE_EXERCISE_TABLE_DAO dao;
    private final BaseExerciseSync sync;

    public BaseExerciseChangeHandler(
            BASE_EXERCISE_TABLE_DAO dao,
            BaseExerciseSync sync,
            ChangeElmDao changeDao
    ) {
        super(changeDao);
        this.dao = dao;
        this.sync = sync;
    }

    @Override
    protected BaseExModel getModel(String uid) {
        return dao.getExByUid(uid);
    }

    // Основной метод для последовательной синхронизации
    @Override
    protected void executeSync(BaseExModel model, String uid, Runnable onComplete) {
        sync.syncBaseExerciseChange(
                model.getBase_ex_name(),
                model,
                new BaseExerciseSync.SyncCallback() {
                    @Override
                    public void onSuccess() {
                        // Вызываем onSuccess родителя с передачей onComplete
                        BaseExerciseChangeHandler.this.onSuccess(uid, onComplete);
                    }

                    @Override
                    public void onFailure(String error) {
                        // Вызываем onFailure родителя с передачей onComplete
                        BaseExerciseChangeHandler.this.onFailure(uid, error, onComplete);
                    }
                }
        );
    }


}