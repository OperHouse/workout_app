package com.example.workoutapp.Models;

public class SetsModel {
    int set_id;
    int reps;



    boolean isSelected;

    int weight;
    // Конструктор, принимающий параметры для всех полей
    public SetsModel(int set_id, int weight, int reps) {
        this.set_id = set_id;
        this.weight = weight;
        this.reps = reps;
    }
    public SetsModel() {
    }

    public int getSet_id() {
        return set_id;
    }

    public void setSet_id(int set_id) {
        this.set_id = set_id;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean selected) {
        isSelected = selected;
    }



}
