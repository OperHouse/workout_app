package com.example.workoutapp.Models.ProfileModels;

public class FoodGainGoalModel {
    private int food_gain_goal_id;
    private int food_gain_goal_calories;
    private float food_gain_goal_protein;
    private float food_gain_goal_fat;
    private float food_gain_goal_carb;
    private String food_gain_goal_date;
    private String food_gain_goal_uid;

    public FoodGainGoalModel(int food_gain_goal_id, int food_gain_goal_calories, float food_gain_goal_protein, float food_gain_goal_fat, float food_gain_goal_carb, String food_gain_goal_date) {
        this.food_gain_goal_id = food_gain_goal_id;
        this.food_gain_goal_calories = food_gain_goal_calories;
        this.food_gain_goal_protein = food_gain_goal_protein;
        this.food_gain_goal_fat = food_gain_goal_fat;
        this.food_gain_goal_carb = food_gain_goal_carb;
        this.food_gain_goal_date = food_gain_goal_date;
    }
    public FoodGainGoalModel(int food_gain_goal_id, int food_gain_goal_calories, float food_gain_goal_protein, float food_gain_goal_fat, float food_gain_goal_carb, String food_gain_goal_date, String food_gain_goal_uid) {
        this.food_gain_goal_id = food_gain_goal_id;
        this.food_gain_goal_calories = food_gain_goal_calories;
        this.food_gain_goal_protein = food_gain_goal_protein;
        this.food_gain_goal_fat = food_gain_goal_fat;
        this.food_gain_goal_carb = food_gain_goal_carb;
        this.food_gain_goal_date = food_gain_goal_date;
        this.food_gain_goal_uid = food_gain_goal_uid;
    }

    public FoodGainGoalModel() {
    }

    // Getters
    @com.google.firebase.firestore.Exclude
    public int getFood_gain_goal_id() {
        return food_gain_goal_id;
    }

    public int getFood_gain_goal_calories() {
        return food_gain_goal_calories;
    }

    public float getFood_gain_goal_protein() {
        return food_gain_goal_protein;
    }

    public float getFood_gain_goal_fat() {
        return food_gain_goal_fat;
    }

    public float getFood_gain_goal_carb() {
        return food_gain_goal_carb;
    }

    public String getFood_gain_goal_date() {
        return food_gain_goal_date;
    }

    // Setters
    public void setFood_gain_goal_id(int food_gain_goal_id) {
        this.food_gain_goal_id = food_gain_goal_id;
    }

    public void setFood_gain_goal_calories(int food_gain_goal_calories) {
        this.food_gain_goal_calories = food_gain_goal_calories;
    }

    public void setFood_gain_goal_protein(float food_gain_goal_protein) {
        this.food_gain_goal_protein = food_gain_goal_protein;
    }

    public void setFood_gain_goal_fat(float food_gain_goal_fat) {
        this.food_gain_goal_fat = food_gain_goal_fat;
    }

    public void setFood_gain_goal_carb(float food_gain_goal_carb) {
        this.food_gain_goal_carb = food_gain_goal_carb;
    }

    public void setFood_gain_goal_date(String food_gain_goal_date) {
        this.food_gain_goal_date = food_gain_goal_date;
    }

    public String getFood_gain_goal_uid() {
        return food_gain_goal_uid;
    }

    public void setFood_gain_goal_uid(String food_gain_goal_uid) {
        this.food_gain_goal_uid = food_gain_goal_uid;
    }
}
