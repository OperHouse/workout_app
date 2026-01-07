package com.example.workoutapp.Models.ProfileModels;

public class DailyFoodTrackingModel {
    private int id;
    private int calories;   // tracking_calories
    private float protein;  // tracking_protein
    private float fat;      // tracking_fat
    private float carb;     // tracking_carb
    private String date;    // tracking_activity_date (твоё поле, хотя логичнее tracking_food_date)

    public DailyFoodTrackingModel(int id, int calories, float protein, float fat, float carb, String date) {
        this.id = id;
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carb = carb;
        this.date = date;
    }

    public DailyFoodTrackingModel() {
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getCalories() {
        return calories;
    }

    public float getProtein() {
        return protein;
    }

    public float getFat() {
        return fat;
    }

    public float getCarb() {
        return carb;
    }

    public String getDate() {
        return date;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public void setProtein(float protein) {
        this.protein = protein;
    }

    public void setFat(float fat) {
        this.fat = fat;
    }

    public void setCarb(float carb) {
        this.carb = carb;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
