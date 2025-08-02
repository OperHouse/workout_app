package com.example.workoutapp.Models.NutritionModels;


public class MealNameModel {
    private int meal_name_id;
    private String meal_name;
    String mealData = "";

    public MealNameModel(int meal_name_id, String meal_name) {
        this.meal_name_id = meal_name_id;
        this.meal_name = meal_name;
    }
    public MealNameModel(int meal_name_id, String meal_name, String mealData) {
        this.meal_name_id = meal_name_id;
        this.meal_name = meal_name;
        this.mealData = mealData;
    }


    public int getMeal_name_id() { return meal_name_id; }

    public String getMeal_name() { return meal_name; }

    public String getMealData() { return mealData; }

    public void setMeal_name_id(int meal_name_id) { this.meal_name_id = meal_name_id; }

    public void setMeal_name(String meal_name) { this.meal_name = meal_name; }

    public void setMealData(String mealData) { this.mealData = mealData; }
}
