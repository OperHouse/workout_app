package com.example.workoutapp.Tools.UploadHandlers;

import com.example.workoutapp.Data.ChangeElmDao;
import com.example.workoutapp.Data.NutritionDao.ConnectingMealDao;
import com.example.workoutapp.Data.NutritionDao.MealFoodDao;
import com.example.workoutapp.Data.NutritionDao.MealNameDao;
import com.example.workoutapp.Models.NutritionModels.MealModel;
import com.example.workoutapp.Tools.SyncTools.MealSync;

public class MealChangeHandler extends BaseSyncHandler<MealModel> {

    private final MealNameDao mealDao;
    private final ConnectingMealDao connDao;
    private final MealFoodDao foodDao;
    private final MealSync mealSync;

    public MealChangeHandler(
            MealNameDao mealDao,
            ConnectingMealDao connDao,
            MealFoodDao foodDao,
            MealSync mealSync,
            ChangeElmDao changeDao
    ) {
        super(changeDao);
        this.mealDao = mealDao;
        this.connDao = connDao;
        this.foodDao = foodDao;
        this.mealSync = mealSync;
    }

    @Override
    protected MealModel getModel(String uid) {
        return mealDao.getMealByUid(uid, connDao, foodDao);
    }

    // Внедряем поддержку Runnable для работы в единой цепочке
    @Override
    protected void executeSync(MealModel model, String uid, Runnable onComplete) {
        mealSync.uploadMeal(model, new MealSync.SyncCallback() {
            @Override
            public void onSuccess() {
                // Вызываем родительский метод с передачей сигнала завершения задачи
                MealChangeHandler.this.onSuccess(uid, onComplete);
            }

            @Override
            public void onFailure(String error) {
                // Даже при ошибке запускаем следующую задачу, чтобы не блокировать очередь
                MealChangeHandler.this.onFailure(uid, error, onComplete);
            }
        });
    }
}