package com.example.workoutapp.NutritionModels;

import java.util.List;

public class PresetMealModel {
    int presetMealName_id;
    String presetMealName;
    List<EatModel> presetMealEat;


    public PresetMealModel(int presetMealName_id, String presetMealName, List<EatModel> presetMealEat){
        this.presetMealName_id = presetMealName_id;
        this.presetMealName = presetMealName;
        this.presetMealEat = presetMealEat;
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



}
