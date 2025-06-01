package com.example.workoutapp.NutritionModels;

public class EatModel {
    int eat_id;
    String eat_name;
    double protein;
    double fat;
    double carb;
    double calories;
    int amount;
    String measurement_type;

    public EatModel(int eat_id, String eat_name, double protein, double fat, double carb, double calories, int amount, String measurement_type) {
        this.eat_id = eat_id;
        this.eat_name = eat_name;
        this.protein = protein;
        this.fat = fat;
        this.carb = carb;
        this.calories = calories;
        this.amount = amount;
        this.measurement_type = measurement_type;
    }

    public int getEat_id() {
        return eat_id;
    }

    public void setEat_id(int eat_id) {
        this.eat_id = eat_id;
    }

    public String getEat_name() {
        return eat_name;
    }

    public void setEat_name(String eat_name) {
        this.eat_name = eat_name;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public double getCarb() {
        return carb;
    }

    public void setCarb(double carb) {
        this.carb = carb;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getMeasurement_type() {
        return measurement_type;
    }

    public void setMeasurement_type(String measurement_type) {
        this.measurement_type = measurement_type;
    }

}
