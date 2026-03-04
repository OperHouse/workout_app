package com.example.workoutapp.Models.NutritionModels;

import com.google.firebase.Timestamp;

public class FoodModel {
    private int food_id;
    private String food_name;
    private double protein;
    private double fat;
    private double carb;
    private double calories;
    private int amount;
    private String measurement_type;
    private String food_uid;
    private boolean deleted;
    private long version;
    private Timestamp updatedAt;


    private boolean isSelected = false;

    public FoodModel() {}


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
    public FoodModel(int food_id, String food_name, double protein, double fat, double carb, double calories, int amount, String measurement_type, String food_uid) {
        this.food_id = food_id;
        this.food_name = food_name;
        this.protein = protein;
        this.fat = fat;
        this.carb = carb;
        this.calories = calories;
        this.amount = amount;
        this.measurement_type = measurement_type;
        this.food_uid = food_uid;
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
        this.food_uid = other.food_uid;
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

    public String getFood_uid() { return food_uid; }
    public void setFood_uid(String food_uid) { this.food_uid = food_uid; }

    @com.google.firebase.firestore.Exclude
    public int getFood_id() { return food_id; }

    public void setMeasurement_type(String measurement_type) {
        this.measurement_type = measurement_type;
    }
    @com.google.firebase.firestore.Exclude
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


    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
