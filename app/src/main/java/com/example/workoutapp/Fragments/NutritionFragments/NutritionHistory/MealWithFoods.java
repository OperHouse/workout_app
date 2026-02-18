package com.example.workoutapp.Fragments.NutritionFragments.NutritionHistory;

import com.example.workoutapp.Models.NutritionModels.FoodModel;
import com.example.workoutapp.Models.NutritionModels.MealNameModel;

import java.util.List;

public class MealWithFoods {
    private MealNameModel mealName;
    private List<FoodModel> foods;

    public MealWithFoods(MealNameModel mealName, List<FoodModel> foods) {
        this.mealName = mealName;
        this.foods = foods;
    }

    public MealNameModel getMealName() { return mealName; }
    public List<FoodModel> getFoods() { return foods; }
}
