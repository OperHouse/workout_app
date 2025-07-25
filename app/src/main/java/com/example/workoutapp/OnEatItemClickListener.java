package com.example.workoutapp;

import android.content.Context;

import com.example.workoutapp.NutritionModels.FoodModel;

public interface OnEatItemClickListener {
    void onEatItemClick(Context context, FoodModel foodModel);
}
