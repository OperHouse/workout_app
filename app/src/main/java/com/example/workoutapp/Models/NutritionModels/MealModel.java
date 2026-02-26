package com.example.workoutapp.Models.NutritionModels;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MealModel {
    private int meal_name_id;
    private String meal_name;
    private String meal_uid;
    private String mealData = "";
    private List<FoodModel> meal_food_list;


    public MealModel(int meal_name_id, String meal_name, List<FoodModel> meal_food_list){
        this.meal_name_id = meal_name_id;
        this.meal_name = meal_name;

        // Универсальный способ копирования для всех версий Android
        if (meal_food_list != null) {
            this.meal_food_list = new ArrayList<>();
            for (FoodModel food : meal_food_list) {
                this.meal_food_list.add(new FoodModel(food));
            }
        } else {
            this.meal_food_list = new ArrayList<>();
        }
    };
    public MealModel(int meal_name_id, String meal_name, String mealData, List<FoodModel> meal_food_list){
        this.meal_name_id = meal_name_id;
        this.meal_name = meal_name;
        this.mealData = mealData;

        // Универсальный способ копирования для всех версий Android
        if (meal_food_list != null) {
            this.meal_food_list = new ArrayList<>();
            for (FoodModel food : meal_food_list) {
                this.meal_food_list.add(new FoodModel(food));
            }
        } else {
            this.meal_food_list = new ArrayList<>();
        }
    }
    public MealModel(int meal_name_id, String meal_name, String mealData, List<FoodModel> meal_food_list, String meal_uid){
        this.meal_name_id = meal_name_id;
        this.meal_name = meal_name;
        this.mealData = mealData;
        this.meal_uid = meal_uid;

        // Универсальный способ копирования для всех версий Android
        if (meal_food_list != null) {
            this.meal_food_list = new ArrayList<>();
            for (FoodModel food : meal_food_list) {
                this.meal_food_list.add(new FoodModel(food));
            }
        } else {
            this.meal_food_list = new ArrayList<>();
        }
    }

    public MealModel(MealModel other) {
        this.meal_name_id = other.meal_name_id;
        this.meal_name = other.meal_name;
        this.mealData = other.mealData;

        if (other.meal_food_list != null) {
            this.meal_food_list = new ArrayList<>();
            for (FoodModel food : other.meal_food_list) {
                this.meal_food_list.add(new FoodModel(food));
            }
        } else {
            this.meal_food_list = new ArrayList<>();
        }
    }


    @com.google.firebase.firestore.Exclude
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

    public void removeFoodById(int foodId) {
        if (meal_food_list instanceof ArrayList) {
            meal_food_list.removeIf(food -> food.getFood_id() == foodId);
        } else {
            meal_food_list = new ArrayList<>(meal_food_list);
            meal_food_list.removeIf(food -> food.getFood_id() == foodId);
        }
    }

    public void updateFood(FoodModel updatedFood) {
        // Если список неизменяемый, создаём изменяемую копию
        if (meal_food_list != null) {
            meal_food_list = new ArrayList<>(meal_food_list);
        }

        for (int i = 0; i < Objects.requireNonNull(meal_food_list).size(); i++) {
            if (meal_food_list.get(i).getFood_id() == updatedFood.getFood_id()) {
                meal_food_list.set(i, new FoodModel(updatedFood));
                break;
            }
        }
    }


}
