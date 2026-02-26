package com.example.workoutapp.Models.ProfileModels;

public class DailyFoodTrackingModel {
    private int daily_food_tracking_id;
    private int daily_food_tracking_calories;
    private float daily_food_tracking_protein;
    private float daily_food_tracking_fat;
    private float daily_food_tracking_carb;
    private String daily_food_tracking_date;
    private String daily_food_tracking_uid;

    public DailyFoodTrackingModel(int daily_food_tracking_id, int daily_food_tracking_calories, float daily_food_tracking_protein, float daily_food_tracking_fat, float daily_food_tracking_carb, String daily_food_tracking_date) {
        this.daily_food_tracking_id = daily_food_tracking_id;
        this.daily_food_tracking_calories = daily_food_tracking_calories;
        this.daily_food_tracking_protein = daily_food_tracking_protein;
        this.daily_food_tracking_fat = daily_food_tracking_fat;
        this.daily_food_tracking_carb = daily_food_tracking_carb;
        this.daily_food_tracking_date = daily_food_tracking_date;
    }

    public DailyFoodTrackingModel(int daily_food_tracking_id, int daily_food_tracking_calories, float daily_food_tracking_protein, float daily_food_tracking_fat, float daily_food_tracking_carb, String daily_food_tracking_date, String daily_food_tracking_uid) {
        this.daily_food_tracking_id = daily_food_tracking_id;
        this.daily_food_tracking_uid = daily_food_tracking_uid;
        this.daily_food_tracking_calories = daily_food_tracking_calories;
        this.daily_food_tracking_protein = daily_food_tracking_protein;
        this.daily_food_tracking_fat = daily_food_tracking_fat;
        this.daily_food_tracking_carb = daily_food_tracking_carb;
        this.daily_food_tracking_date = daily_food_tracking_date;
    }

    public DailyFoodTrackingModel() {
    }

    // Getters
    @com.google.firebase.firestore.Exclude
    public int getDaily_food_tracking_id() {
        return daily_food_tracking_id;
    }

    public int getDaily_food_tracking_calories() {
        return daily_food_tracking_calories;
    }

    public float getDaily_food_tracking_protein() {
        return daily_food_tracking_protein;
    }

    public float getDaily_food_tracking_fat() {
        return daily_food_tracking_fat;
    }

    public float getDaily_food_tracking_carb() {
        return daily_food_tracking_carb;
    }

    public String getDaily_food_tracking_date() {
        return daily_food_tracking_date;
    }

    // Setters
    public void setDaily_food_tracking_id(int daily_food_tracking_id) {
        this.daily_food_tracking_id = daily_food_tracking_id;
    }

    public void setDaily_food_tracking_calories(int daily_food_tracking_calories) {
        this.daily_food_tracking_calories = daily_food_tracking_calories;
    }

    public void setDaily_food_tracking_protein(float daily_food_tracking_protein) {
        this.daily_food_tracking_protein = daily_food_tracking_protein;
    }

    public void setDaily_food_tracking_fat(float daily_food_tracking_fat) {
        this.daily_food_tracking_fat = daily_food_tracking_fat;
    }

    public void setDaily_food_tracking_carb(float daily_food_tracking_carb) {
        this.daily_food_tracking_carb = daily_food_tracking_carb;
    }

    public void setDaily_food_tracking_date(String daily_food_tracking_date) {
        this.daily_food_tracking_date = daily_food_tracking_date;
    }

    public String getDaily_food_tracking_uid() {
        return daily_food_tracking_uid;
    }

    public void setDaily_food_tracking_uid(String daily_food_tracking_uid) {
        this.daily_food_tracking_uid = daily_food_tracking_uid;
    }
}
