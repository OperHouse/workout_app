package com.example.workoutapp.Tools;

import android.content.Context;

import com.example.workoutapp.Models.NutritionModels.FoodModel;

public interface OnEatItemClickListener {
    void onEatItemClick(Context context, FoodModel foodModel);
}
