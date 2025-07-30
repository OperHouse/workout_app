package com.example.workoutapp.NutritionModels;

import java.util.ArrayList;

public class FoodModel {
    int food_id;
    String food_name;
    double protein;
    double fat;
    double carb;
    double calories;
    int amount;
    String measurement_type;


    boolean isSelected = false;


    public FoodModel(int food_id, String food_name, double protein, double fat, double carb, double calories, int amount, String measurement_type) {
        this.food_id = food_id;
        this.food_name = food_name;
        this.protein = protein;
        this.fat = fat;
        this.carb = carb;
        this.calories = calories;
        this.amount = amount;
        this.measurement_type = measurement_type;
    }
    public FoodModel(int food_id, String food_name, double protein, double fat, double carb, double calories, int amount, String measurement_type, boolean isSelected) {
        this.food_id = food_id;
        this.food_name = food_name;
        this.protein = protein;
        this.fat = fat;
        this.carb = carb;
        this.calories = calories;
        this.amount = amount;
        this.measurement_type = measurement_type;
        this.isSelected = isSelected;
    }

    public void copyFrom(FoodModel other) {
        this.food_id = other.food_id;
        this.food_name = other.food_name;
        this.protein = other.protein;
        this.fat = other.fat;
        this.carb = other.carb;
        this.calories = other.calories;
        this.amount = other.amount;
        this.measurement_type = other.measurement_type;
        this.isSelected = other.isSelected;
    }

    public FoodModel(FoodModel other) {
        this.food_id = other.food_id;
        this.food_name = other.food_name;
        this.protein = other.protein;
        this.fat = other.fat;
        this.carb = other.carb;
        this.calories = other.calories;
        this.amount = other.amount;
        this.measurement_type = other.measurement_type;
        this.isSelected = other.isSelected;
    }

    public int getFood_id() {
        return food_id;
    }

    public void setFood_id(int food_id) {
        this.food_id = food_id;
    }

    public String getFood_name() {
        return food_name;
    }

    public void setFood_name(String food_name) {
        this.food_name = food_name;
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
    public boolean getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        FoodModel eat = (FoodModel) obj;
        return this.food_id == eat.food_id; // сравнение по ID, или по нужным полям
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(food_id);
    }


}
