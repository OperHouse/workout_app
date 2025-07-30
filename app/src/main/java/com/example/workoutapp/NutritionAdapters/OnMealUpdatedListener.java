package com.example.workoutapp.NutritionAdapters;

import com.example.workoutapp.NutritionModels.FoodModel;

public interface OnMealUpdatedListener {
    void onFoodUpdated(FoodModel updatedFood);

    void onFoodDeleted(int foodId);
}
