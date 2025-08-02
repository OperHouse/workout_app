package com.example.workoutapp.Tools;

import com.example.workoutapp.Models.NutritionModels.FoodModel;

public interface OnMealUpdatedListener {
    void onFoodUpdated(FoodModel updatedFood);

    void onFoodDeleted(int foodId);
}
