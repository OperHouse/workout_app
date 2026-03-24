package com.example.workoutapp.Tools.UploadHandlers;

import com.example.workoutapp.Data.ChangeElmDao;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.Tools.WorkoutSessionSync2;

import java.util.ArrayList;
import java.util.List;

public class WorkoutExerciseChangeHandler extends BaseSyncHandler<ExerciseModel> {

    private final WORKOUT_EXERCISE_TABLE_DAO dao;
    private final WorkoutSessionSync2 sync;

    public WorkoutExerciseChangeHandler(
            WORKOUT_EXERCISE_TABLE_DAO dao,
            WorkoutSessionSync2 sync,
            ChangeElmDao changeDao
    ) {
        super(changeDao);
        this.dao = dao;
        this.sync = sync;
    }

    @Override
    protected ExerciseModel getModel(String uid) {
        return dao.getExByUid(uid);
    }

    // Внедряем Runnable onComplete для поддержки последовательной очереди
    @Override
    protected void executeSync(ExerciseModel model, String uid, Runnable onComplete) {
        List<ExerciseModel> list = new ArrayList<>();
        list.add(model);

        // Синхронизируем конкретное упражнение через WorkoutSessionSync2
        sync.syncSpecificExercises(list, new WorkoutSessionSync2.SyncCallback() {
            @Override
            public void onSuccess() {
                // Удаляем задачу из таблицы изменений и запускаем следующую через onComplete
                WorkoutExerciseChangeHandler.this.onSuccess(uid, onComplete);
            }

            @Override
            public void onFailure(String error) {
                // Даже если синхронизация одного упражнения не удалась,
                // даем очереди идти дальше, чтобы не блокировать всё приложение
                WorkoutExerciseChangeHandler.this.onFailure(uid, error, onComplete);
            }
        });
    }
}