package com.example.workoutapp.Fragments.NutritionFragments.NutritionHistory;

import com.example.workoutapp.Models.NutritionModels.FoodModel;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

public class NutritionSessionModel implements Serializable {
    private String date;
    private List<MealWithFoods> meals; // Список приемов пищи (завтрак, обед и т.д.)
    private double totalCalories, totalProtein, totalFat, totalCarbs;

    private void calculateDailyTotals() {
        this.totalCalories = 0;
        this.totalProtein = 0;
        this.totalFat = 0;
        this.totalCarbs = 0;

        if (meals != null) {
            for (MealWithFoods meal : meals) {
                if (meal.getFoods() != null) {
                    for (FoodModel food : meal.getFoods()) {
                        this.totalCalories += food.getCalories();
                        this.totalProtein += food.getProtein();
                        this.totalFat += food.getFat();
                        this.totalCarbs += food.getCarb();
                    }
                }
            }
        }
    }

    // Обязательно вызовите этот метод в конструкторе!
    public NutritionSessionModel(String date, List<MealWithFoods> meals) {
        this.date = date;
        this.meals = meals;
        calculateDailyTotals();
    }

    // Геттеры
    public String getDate() { return date; }
    public List<MealWithFoods> getMeals() { return meals; }
    public double getTotalCalories() { return totalCalories; }
    public String getMacrosSummary() {
        return String.format(Locale.getDefault(), "Б: %.1f  Ж: %.1f  У: %.1f", totalProtein, totalFat, totalCarbs);
    }

    public double getTotalProtein() {
        return totalProtein;
    }

    public double getTotalFat() {
        return totalFat;
    }

    public double getTotalCarbs() {
        return totalCarbs;
    }

    public int getMealsCount() {
        return (meals != null) ? meals.size() : 0;
    }
}

