package com.example.workoutapp.Models.NutritionModels;


public class MealNameModel {
    private int meal_name_id;
    private String meal_name;
    private String mealData = "";
    private String meal_uid;

    public MealNameModel(int meal_name_id, String meal_name) {
        this.meal_name_id = meal_name_id;
        this.meal_name = meal_name;
    }
    public MealNameModel(int meal_name_id, String meal_name, String mealData) {
        this.meal_name_id = meal_name_id;
        this.meal_name = meal_name;
        this.mealData = mealData;
    }
    public MealNameModel(int meal_name_id, String meal_name, String mealData, String meal_uid) {
        this.meal_name_id = meal_name_id;
        this.meal_name = meal_name;
        this.mealData = mealData;
        this.meal_uid = meal_uid;
    }
    public MealNameModel(){}

    @com.google.firebase.firestore.Exclude
    public int getMeal_name_id() { return meal_name_id; }

    public String getMeal_name() { return meal_name; }

    public String getMealData() { return mealData; }

    public void setMeal_name_id(int meal_name_id) { this.meal_name_id = meal_name_id; }

    public void setMeal_name(String meal_name) { this.meal_name = meal_name; }

    public void setMealData(String mealData) { this.mealData = mealData; }

    public String getMeal_uid() {
        return meal_uid;
    }

    public void setMeal_uid(String meal_uid) {
        this.meal_uid = meal_uid;
    }
}
