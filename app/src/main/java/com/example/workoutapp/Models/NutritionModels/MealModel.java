package com.example.workoutapp.Models.NutritionModels;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;

public class MealModel {

    private int meal_name_id;
    private String meal_name;
    private String meal_uid;
    private String mealData;

    private List<FoodModel> meal_food_list = new ArrayList<>();

    private boolean deleted = false;
    private long version = 0;
    private Timestamp updatedAt;

    // Пустой конструктор для Firestore
    public MealModel() {}

    // Основной конструктор
    public MealModel(int meal_name_id,
                     String meal_name,
                     String meal_uid,
                     String mealData,
                     List<FoodModel> meal_food_list) {

        this.meal_name_id = meal_name_id;
        this.meal_name = meal_name;
        this.meal_uid = meal_uid;
        this.mealData = mealData;

        if (meal_food_list != null) {
            this.meal_food_list = new ArrayList<>();
            for (FoodModel food : meal_food_list) {
                this.meal_food_list.add(new FoodModel(food));
            }
        }
    }

    // Копирующий конструктор
    public MealModel(MealModel other) {

        this.meal_name_id = other.meal_name_id;
        this.meal_name = other.meal_name;
        this.meal_uid = other.meal_uid;
        this.mealData = other.mealData;

        this.deleted = other.deleted;
        this.version = other.version;
        this.updatedAt = other.updatedAt;

        if (other.meal_food_list != null) {
            this.meal_food_list = new ArrayList<>();
            for (FoodModel food : other.meal_food_list) {
                this.meal_food_list.add(new FoodModel(food));
            }
        }
    }

    // ================= FOOD OPERATIONS =================

    public void removeFoodById(int foodId) {

        if (meal_food_list == null) return;

        meal_food_list = new ArrayList<>(meal_food_list);

        meal_food_list.removeIf(food -> food.getFood_id() == foodId);
    }

    public void updateFood(FoodModel updatedFood) {

        if (meal_food_list == null) return;

        meal_food_list = new ArrayList<>(meal_food_list);

        for (int i = 0; i < meal_food_list.size(); i++) {

            if (meal_food_list.get(i).getFood_id() == updatedFood.getFood_id()) {

                meal_food_list.set(i, new FoodModel(updatedFood));
                break;

            }
        }
    }

    // ================= GETTERS =================

    @Exclude
    public int getMeal_name_id() {
        return meal_name_id;
    }

    public String getMeal_name() {
        return meal_name;
    }

    public String getMeal_uid() {
        return meal_uid;
    }

    public String getMealData() {
        return mealData;
    }

    public List<FoodModel> getMeal_food_list() {
        return meal_food_list;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public long getVersion() {
        return version;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    // ================= SETTERS =================

    public void setMeal_name_id(int meal_name_id) {
        this.meal_name_id = meal_name_id;
    }

    public void setMeal_name(String meal_name) {
        this.meal_name = meal_name;
    }

    public void setMeal_uid(String meal_uid) {
        this.meal_uid = meal_uid;
    }

    public void setMealData(String mealData) {
        this.mealData = mealData;
    }

    public void setMeal_food_list(List<FoodModel> meal_food_list) {
        this.meal_food_list = meal_food_list;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}