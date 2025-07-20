package com.example.workoutapp.NutritionModels;


public class MealNameModel {
    private int id;
    private String name;



    String mealData = "";

    public MealNameModel(int id, String name) {
        this.id = id;
        this.name = name;
    }
    public MealNameModel(int id, String name, String mealData) {
        this.id = id;
        this.name = name;
        this.mealData = mealData;
    }


    public int getId() { return id; }

    public String getName() { return name; }

    public String getMealData() { return mealData; }

    public void setId(int id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    public void setMealData(String mealData) { this.mealData = mealData; }
}
