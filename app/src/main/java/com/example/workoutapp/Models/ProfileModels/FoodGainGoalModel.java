package com.example.workoutapp.Models.ProfileModels;

public class FoodGainGoalModel {
    private int id;
    private int caloriesGoal; // food_gain_goal_calories
    private float proteinGoal; // food_gain_goal_protein
    private float fatGoal;     // food_gain_goal_fat
    private float carbGoal;    // food_gain_goal_carb
    private String date;       // food_gain_goal_date

    public FoodGainGoalModel(int id, int caloriesGoal, float proteinGoal, float fatGoal, float carbGoal, String date) {
        this.id = id;
        this.caloriesGoal = caloriesGoal;
        this.proteinGoal = proteinGoal;
        this.fatGoal = fatGoal;
        this.carbGoal = carbGoal;
        this.date = date;
    }

    public FoodGainGoalModel() {
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getCaloriesGoal() {
        return caloriesGoal;
    }

    public float getProteinGoal() {
        return proteinGoal;
    }

    public float getFatGoal() {
        return fatGoal;
    }

    public float getCarbGoal() {
        return carbGoal;
    }

    public String getDate() {
        return date;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setCaloriesGoal(int caloriesGoal) {
        this.caloriesGoal = caloriesGoal;
    }

    public void setProteinGoal(float proteinGoal) {
        this.proteinGoal = proteinGoal;
    }

    public void setFatGoal(float fatGoal) {
        this.fatGoal = fatGoal;
    }

    public void setCarbGoal(float carbGoal) {
        this.carbGoal = carbGoal;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
