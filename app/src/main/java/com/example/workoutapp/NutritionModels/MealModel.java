package com.example.workoutapp.NutritionModels;

import android.os.Build;

import java.util.List;

public class MealModel {
    int meal_name_id;
    String meal_name;
    String mealData = "";
    List<FoodModel> meal_food_list;


    public MealModel(int meal_name_id, String meal_name, List<FoodModel> meal_food_list){
        this.meal_name_id = meal_name_id;
        this.meal_name = meal_name;

        // Копируем список FoodModel через копирующий конструктор каждого элемента
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            this.meal_food_list = meal_food_list.stream()
                    .map(FoodModel::new) // требуется копирующий конструктор в FoodModel
                    .toList(); // можно заменить на collect(Collectors.toList()) для совместимости с Java <16
        }
    };
    public MealModel(MealModel other) {
        this.meal_name_id = other.meal_name_id;
        this.meal_name = other.meal_name;
        this.mealData = other.mealData;

        // Копируем список FoodModel через копирующий конструктор каждого элемента
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            this.meal_food_list = other.meal_food_list.stream()
                    .map(FoodModel::new) // требуется копирующий конструктор в FoodModel
                    .toList(); // можно заменить на collect(Collectors.toList()) для совместимости с Java <16
        }
    }

    public MealModel(int meal_name_id, String meal_name, String mealData, List<FoodModel> meal_food_list){
        this.meal_name_id = meal_name_id;
        this.meal_name = meal_name;
        this.mealData = mealData;

        // Копируем список FoodModel через копирующий конструктор каждого элемента
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            this.meal_food_list = meal_food_list.stream()
                    .map(FoodModel::new) // требуется копирующий конструктор в FoodModel
                    .toList(); // можно заменить на collect(Collectors.toList()) для совместимости с Java <16
        }
    };






    public int getMeal_name_id() {
        return meal_name_id;
    }

    public void setMeal_name_id(int meal_name_id) {
        this.meal_name_id = meal_name_id;
    }

    public String getMeal_name() {
        return meal_name;
    }

    public void setMeal_name(String meal_name) {
        this.meal_name = meal_name;
    }

    public List<FoodModel> getMeal_food_list() {
        return meal_food_list;
    }

    public void setMeal_food_list(List<FoodModel> meal_food_list) {
        this.meal_food_list = meal_food_list;
    }
    public String getMealData() {
        return mealData;
    }

    public void setMealData(String mealData) {
        this.mealData = mealData;
    }


}
