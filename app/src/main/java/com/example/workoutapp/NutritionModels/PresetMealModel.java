package com.example.workoutapp.NutritionModels;

import android.os.Build;

import java.util.List;

public class PresetMealModel {
    int presetMealName_id;
    String presetMealName;



    String mealData = "";
    List<EatModel> presetMealEat;


    public PresetMealModel(int presetMealName_id, String presetMealName, List<EatModel> presetMealEat){
        this.presetMealName_id = presetMealName_id;
        this.presetMealName = presetMealName;

        // Копируем список EatModel через копирующий конструктор каждого элемента
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            this.presetMealEat = presetMealEat.stream()
                    .map(EatModel::new) // требуется копирующий конструктор в EatModel
                    .toList(); // можно заменить на collect(Collectors.toList()) для совместимости с Java <16
        }
    };
    public PresetMealModel(PresetMealModel other) {
        this.presetMealName_id = other.presetMealName_id;
        this.presetMealName = other.presetMealName;
        this.mealData = other.mealData;

        // Копируем список EatModel через копирующий конструктор каждого элемента
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            this.presetMealEat = other.presetMealEat.stream()
                    .map(EatModel::new) // требуется копирующий конструктор в EatModel
                    .toList(); // можно заменить на collect(Collectors.toList()) для совместимости с Java <16
        }
    }

    public PresetMealModel(int presetMealName_id, String presetMealName,String mealData, List<EatModel> presetMealEat){
        this.presetMealName_id = presetMealName_id;
        this.presetMealName = presetMealName;
        this.mealData = mealData;

        // Копируем список EatModel через копирующий конструктор каждого элемента
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            this.presetMealEat = presetMealEat.stream()
                    .map(EatModel::new) // требуется копирующий конструктор в EatModel
                    .toList(); // можно заменить на collect(Collectors.toList()) для совместимости с Java <16
        }
    };






    public int getPresetMealName_id() {
        return presetMealName_id;
    }

    public void setPresetMealName_id(int presetMealName_id) {
        this.presetMealName_id = presetMealName_id;
    }

    public String getPresetMealName() {
        return presetMealName;
    }

    public void setPresetMealName(String presetMealName) {
        this.presetMealName = presetMealName;
    }

    public List<EatModel> getPresetMealEat() {
        return presetMealEat;
    }

    public void setPresetMealEat(List<EatModel> presetMealEat) {
        this.presetMealEat = presetMealEat;
    }
    public String getMealData() {
        return mealData;
    }

    public void setMealData(String mealData) {
        this.mealData = mealData;
    }


}
