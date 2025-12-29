package com.example.workoutapp.Models.ProfileModels;

public class ActivityGoalModel {
    private int id;
    private String date;          // activity_goal_date
    private int stepsGoal;        // activity_goal_steps
    private int caloriesToBurn;   // activity_goal_calories_to_burn

    public ActivityGoalModel(int id, String date, int stepsGoal, int caloriesToBurn) {
        this.id = id;
        this.date = date;
        this.stepsGoal = stepsGoal;
        this.caloriesToBurn = caloriesToBurn;
    }

    public ActivityGoalModel() {
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public int getStepsGoal() {
        return stepsGoal;
    }

    public int getCaloriesToBurn() {
        return caloriesToBurn;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setStepsGoal(int stepsGoal) {
        this.stepsGoal = stepsGoal;
    }

    public void setCaloriesToBurn(int caloriesToBurn) {
        this.caloriesToBurn = caloriesToBurn;
    }
}
