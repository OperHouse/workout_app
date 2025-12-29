package com.example.workoutapp.Models.ProfileModels;



public class GeneralGoalModel {

    private int id;                     // GENERAL_GOAL_ID
    private String goalText;            // GENERAL_GLOBAL_GOAL_TEXT
    private int workoutsWeekly;         // GENERAL_GOAL_WORKOUTS_WEEKLY
    private int foodTrackingWeekly;     // GENERAL_GOAL_FOOD_TRACKING_WEEKLY
    private String date;                // GENERAL_GOAL_DATE (формат yyyy-MM-dd)

    // Конструктор для полной модели
    public GeneralGoalModel(int id, String goalText, int workoutsWeekly, int foodTrackingWeekly, String date) {
        this.id = id;
        this.goalText = goalText;
        this.workoutsWeekly = workoutsWeekly;
        this.foodTrackingWeekly = foodTrackingWeekly;
        this.date = date;
    }

    // Конструктор без ID (для вставки новой записи)
    public GeneralGoalModel(String goalText, int workoutsWeekly, int foodTrackingWeekly, String date) {
        this.goalText = goalText;
        this.workoutsWeekly = workoutsWeekly;
        this.foodTrackingWeekly = foodTrackingWeekly;
        this.date = date;
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGoalText() {
        return goalText;
    }

    public void setGoalText(String goalText) {
        this.goalText = goalText;
    }

    public int getWorkoutsWeekly() {
        return workoutsWeekly;
    }

    public void setWorkoutsWeekly(int workoutsWeekly) {
        this.workoutsWeekly = workoutsWeekly;
    }

    public int getFoodTrackingWeekly() {
        return foodTrackingWeekly;
    }

    public void setFoodTrackingWeekly(int foodTrackingWeekly) {
        this.foodTrackingWeekly = foodTrackingWeekly;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
