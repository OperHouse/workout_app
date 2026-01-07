package com.example.workoutapp.Models.ProfileModels;

public class GoalModel {
    private long goalId;
    private String goalStartDate;
    private String userGoalText;
    private double goalCaloriesGain;
    private double goalProtein;
    private double goalFat;
    private double goalCarb;
    private double goalToBurnCalories;
    private int goalSteps;
    private int goalWorkoutsWeekly;

    // Конструктор
    public GoalModel(long goalId, String goalStartDate, String userGoalText, double goalCaloriesGain, double goalProtein, double goalFat, double goalCarb, double goalToBurnCalories, int goalSteps, int goalWorkoutsWeekly) {
        this.goalId = goalId;
        this.goalStartDate = goalStartDate;
        this.userGoalText = userGoalText;
        this.goalCaloriesGain = goalCaloriesGain;
        this.goalProtein = goalProtein;
        this.goalFat = goalFat;
        this.goalCarb = goalCarb;
        this.goalToBurnCalories = goalToBurnCalories;
        this.goalSteps = goalSteps;
        this.goalWorkoutsWeekly = goalWorkoutsWeekly;
    }

    public double getGoalCaloriesGain() {
        return goalCaloriesGain;
    }

    public void setGoalCaloriesGain(double goalCaloriesGain) {
        this.goalCaloriesGain = goalCaloriesGain;
    }

    public long getGoalId() {
        return goalId;
    }

    public void setGoalId(long goalId) {
        this.goalId = goalId;
    }

    public String getGoalStartDate() {
        return goalStartDate;
    }

    public void setGoalStartDate(String goalStartDate) {
        this.goalStartDate = goalStartDate;
    }

    public String getUserGoalText() {
        return userGoalText;
    }

    public void setUserGoalText(String userGoalText) {
        this.userGoalText = userGoalText;
    }

    public double getGoalProtein() {
        return goalProtein;
    }

    public void setGoalProtein(double goalProtein) {
        this.goalProtein = goalProtein;
    }

    public double getGoalFat() {
        return goalFat;
    }

    public void setGoalFat(double goalFat) {
        this.goalFat = goalFat;
    }

    public double getGoalCarb() {
        return goalCarb;
    }

    public void setGoalCarb(double goalCarb) {
        this.goalCarb = goalCarb;
    }

    public double getGoalToBurnCalories() {
        return goalToBurnCalories;
    }

    public void setGoalToBurnCalories(double goalToBurnCalories) {
        this.goalToBurnCalories = goalToBurnCalories;
    }

    public int getGoalSteps() {
        return goalSteps;
    }

    public void setGoalSteps(int goalSteps) {
        this.goalSteps = goalSteps;
    }

    public int getGoalWorkoutsWeekly() {
        return goalWorkoutsWeekly;
    }

    public void setGoalWorkoutsWeekly(int goalWorkoutsWeekly) {
        this.goalWorkoutsWeekly = goalWorkoutsWeekly;
    }
}
